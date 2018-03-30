package com.github.hboy.web.service;


import com.github.hboy.center.monitor.api.FailureMessage;
import com.github.hboy.center.monitor.api.Statistics;
import com.github.hboy.center.monitor.api.StatisticsMessage;
import com.github.hboy.web.bean.ApplicationBean;
import com.github.hboy.web.bean.ExceptionRecord;
import com.github.hboy.web.bean.MonitoringRecord;
import com.github.hboy.web.service.itf.MonitorApiService;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;


import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class MonitorApiServiceImpl implements MonitorApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(MonitorApiServiceImpl.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private static final SimpleDateFormat readableDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("yyyyMMddHH");
    private static final NumberFormat nf  =  NumberFormat.getPercentInstance();
    private static final String UTF8 = "utf-8";
    private static final String FORMAT_KEY = "%s|%s|%s|%s|%s|%s|%s";
    private static final String DEF_APP = "defaultapp";
    private static final String DEF_PORT = ":0";
    private static final String FUZZY_MARK = "*";
    private static final String KEY_SEP = "|";
    private static final String COLON = ":";

    static {
        nf.setMinimumFractionDigits(2);
    }

    @Autowired
    private JedisPool pool;

    @Override
    public List<ApplicationBean> getMonitoringApps(){
        Jedis jedis = null;
        List<String> hoursNeedToFQuery = hoursToFQuery();
        Set<String> monitoringApps = new LinkedHashSet<String>();
        List<ApplicationBean> result = new ArrayList<ApplicationBean>();
        try {
            jedis = pool.getResource();
            for (String key : hoursNeedToFQuery) {
                monitoringApps.addAll(getAppsFromKeys(jedis.keys(key + FUZZY_MARK)));
            }
        } catch (Exception e) {
            logger.error("Get monitoring apps error! error message: " + e.getMessage(), e);
            if (pool != null && jedis != null){
                pool.returnBrokenResource(jedis);
            }
            return new ArrayList<ApplicationBean>();
        } finally {
            if (pool != null && jedis != null){
                pool.returnResource(jedis);
            }
        }
        
        for(String app : monitoringApps){
            ApplicationBean applicationBean = new ApplicationBean();
            applicationBean.setAppName_EN(app);
            result.add(applicationBean);
        }
        return result;
    }

    private Set<String> getAppsFromKeys(Set<String> keys){
        Set<String> apps = new LinkedHashSet<String>();
        if(keys == null || keys.isEmpty()){
            return apps;
        }
        for(String key : keys){
            String[] strArray = key.split("\\" + KEY_SEP);
            if(strArray == null || strArray.length == 0){
                continue;
            }
            apps.add(strArray[1]);
        }
        return apps;
    }
    
    /**
     * 获取需要redis模糊查询的时间点（小时）
     * @return
     */
    private List<String> hoursToFQuery(){
        List<String> hoursToFQuery = new ArrayList<String>();
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        int minuteForNow = ca.get(Calendar.MINUTE);
        if(minuteForNow > 30){
            hoursToFQuery.add(hourFormat.format(ca.getTime()));
            return hoursToFQuery;
        }else{
            hoursToFQuery.add(hourFormat.format(ca.getTime()));
            // 日历回退一个小时
            ca.add(Calendar.HOUR_OF_DAY, -1);
            hoursToFQuery.add(hourFormat.format(ca.getTime()));
            return hoursToFQuery;
        }
    }

    /**
     * @return Map<service, List<record>>
     */
    @Override
    public Map<String, List<MonitoringRecord>> calculateRecords(String appName, boolean isClient){
        Map<String, List<MonitoringRecord>> result = new LinkedHashMap<String, List<MonitoringRecord>>();
        List<String> keysToBeFQuery = new ArrayList<String>();
        List<String> hoursInKey = hoursToFQuery();
        Set<byte[]> effectiveKeys = new HashSet<byte[]>();
        for(String hourInKey : hoursInKey){
            keysToBeFQuery.add(hourInKey + FUZZY_MARK + KEY_SEP + appName + KEY_SEP + isClient + KEY_SEP + FUZZY_MARK); //如：2014092611*|appName|false|*
        }
        JedisPool pool = null;
        Jedis jedis = null;
        List<Response<byte[]>> respList = new ArrayList<Response<byte[]>>();
        try {
            jedis = pool.getResource();
            for(String key : keysToBeFQuery){
                effectiveKeys.addAll(jedis.keys(key.getBytes(UTF8)));
            }
            if(effectiveKeys == null || effectiveKeys.isEmpty()){
                return result;
            }

            Pipeline pipe = jedis.pipelined();
            for (byte[] effectiveKey : effectiveKeys) {
                Response<byte[]> resp = pipe.get(effectiveKey);
                respList.add(resp);
            }
            pipe.sync(); // 要结束了管道才能调用response的get()
            
            List<Statistics> statisticsList = deserializeStatisticsList(respList);
            doCalculateRecords(statisticsList, result, isClient, hoursInKey);
            
        } catch (Exception e) {
            logger.error("Calculate records error! error message: " + e.getMessage(), e);
            if (pool != null && jedis != null){
                pool.returnBrokenResource(jedis);
            }
            return new LinkedHashMap<String, List<MonitoringRecord>>();
        } finally {
            if (pool != null && jedis != null){
                pool.returnResource(jedis);
            }
        }
        return result;
    }

    /**
     * 统计展示的核心代码
     * 各个维度参数的统计
     * @param statisticsList
     * @param result
     * @throws ParseException 
     */
    private void doCalculateRecords(List<Statistics> statisticsList, Map<String, List<MonitoringRecord>> result, boolean isClient, List<String> hoursInKey) throws ParseException{
        if(statisticsList == null || statisticsList.isEmpty()){
            return;
        }

        long periodStart = Long.MAX_VALUE;
        long periodEnd = 0;
        for(String hour : hoursInKey){
            periodStart = hourFormat.parse(hour).getTime() < periodStart ? hourFormat.parse(hour).getTime() : periodStart;
            periodEnd = hourFormat.parse(hour).getTime() > periodEnd ? hourFormat.parse(hour).getTime() : periodEnd;
        }

        // 按照(接口名称|本地地址|远程地址)对原始统计信息进行分类
        // 注：调用发起方不需要考虑端口的区分
        Map<String, List<Statistics>> oriStatInfoMap = new LinkedHashMap<String, List<Statistics>>();
        for(Statistics stat : statisticsList){
            try{
                String tempKey = stat.getService() + KEY_SEP + getSmugTCPLine(isClient, stat.getMessage().getLocalAddress(), stat.getMessage().getRemoteAddress()); // 接口名称|本地地址|远程地址
                if(!oriStatInfoMap.containsKey(tempKey)){
                    oriStatInfoMap.put(tempKey, new ArrayList<Statistics>());
                }
                oriStatInfoMap.get(tempKey).add(stat);
                
                if(!result.containsKey(stat.getService())){
                    result.put(stat.getService(), new ArrayList<MonitoringRecord>());
                }
            }catch(Throwable t){
                logger.error("------------------stat:" + stat + ", message:" + stat.getMessage() + ", t.msg:" + t.getMessage(), t);
            }
        }
        
        for(Map.Entry<String, List<Statistics>> oriStatInfoEntry : oriStatInfoMap.entrySet()){
            List<Statistics> statList = oriStatInfoEntry.getValue();
            Statistics tempStat = statList.get(0);
            String service = tempStat.getService();
            String localAddress = handleAddress(isClient, tempStat.getMessage().getLocalAddress(), true);
            String remoteAddress = handleAddress(isClient, tempStat.getMessage().getRemoteAddress(), false);
            
            if(result.containsKey(service)){
                if(statList == null || statList.size() == 0){
                    continue;
                }
                result.get(service).add(genMonitoringRecord(service, localAddress, remoteAddress, statList, periodStart, periodEnd));
            }
        }
        
    }
    
    /**
     * TODO:这个方法略长...... 
     */
    private MonitoringRecord genMonitoringRecord(String service, String localAddress, String remoteAddress, List<Statistics> statList, long periodStart, long periodEnd){
        MonitoringRecord record = new MonitoringRecord();
        record.setService(service);
        record.setLocalAddress(localAddress);
        record.setRemoteAddress(remoteAddress);

        double tps = 0;
        int successNums = 0;
        int failureNums = 0;
        long totalElapsed = 0;
        long averElapsed = 0;
        long maxElapsed = 0;
        long minElapsed = Long.MAX_VALUE; //做比较参照
        Map<String, Double> tpsMap = new HashMap<String, Double>();
        for(Statistics stat : statList){
            StatisticsMessage msg = stat.getMessage();
            long tempFailNums = 0;
            long tempSuccNums = 0;
            if(msg.getFailure() != null){
                tempFailNums = msg.getFailure().size();
            }
            if(msg.getSuccessNum() != null){
                tempSuccNums = msg.getSuccessNum().get();
            }
            
            String tempTimestamp = stat.getTimestamp();
            Double tempTps = tpsMap.get(tempTimestamp);
            if(tempTps == null){
                tpsMap.put(tempTimestamp, (tempSuccNums + tempFailNums) / 60.0);
            }else{
                tpsMap.put(tempTimestamp, tempTps + ((tempSuccNums + tempFailNums) / 60.0)); 
            }

            successNums += tempSuccNums;                // 计算成功次数
            failureNums += tempFailNums;                // 计算失败次数
            
            totalElapsed += msg.getAvgElapsed();           
            
            if(msg.getMaxElapsed() > 0){
                maxElapsed = (msg.getMaxElapsed() > maxElapsed) ? msg.getMaxElapsed() : maxElapsed;
            }
            if(msg.getMinElapsed() > 0){
                minElapsed = (msg.getMinElapsed() < minElapsed) ? msg.getMinElapsed() : minElapsed;
            }
        }
        averElapsed = totalElapsed / statList.size();
        
        Double tempTotalTps = 0.0;
        for(Map.Entry<String, Double> entry : tpsMap.entrySet()){
            tempTotalTps += entry.getValue();
        }
        tps = tempTotalTps / tpsMap.size();
        
        record.setTps(doc3f(tps) + " [#/sec]");
        record.setSuccessNums(successNums + "");
        record.setFailureNums(failureNums + "");
        record.setAverElapsed(nsToMs(averElapsed));
        record.setMaxElapsed(nsToMs(maxElapsed));
        record.setMinElapsed(nsToMs(minElapsed));
        record.setSuccessRate(nf.format(successNums / ((successNums + failureNums) * 1.00)) + "");
        record.setPeriodStart(periodStart + "");
        record.setPeriodEnd(periodEnd + "");
        return record;
    }
    
    /**
     * 纳秒转换为毫秒
     * @param nanoSec
     * @return
     */
    private String nsToMs(long nanoSec){
        return doc3f(nanoSec / 1000000.0) + "ms";
    }
    
    /**
     * 保留小数点后三位
     * @param o
     * @return
     */
    private String doc3f(Object o){
        return String.format("%.3f", o);
    }

    /**
     * 获取"TCP"地址线路
     * 注：调用发起方不需要带端口，所以并非真实的tcp连接定义
     * @param isClient
     * @param localAddress
     * @param remoteAddress
     * @return
     */
    private String getSmugTCPLine(boolean isClient, String localAddress, String remoteAddress){
        if(isClient){
            return localAddress.substring(0, localAddress.indexOf(COLON)) + KEY_SEP + remoteAddress;
        }
        return localAddress + KEY_SEP + remoteAddress.substring(0, remoteAddress.indexOf(COLON));
    }

    private String handleAddress(boolean isClient, String address, boolean isLocal){
        if(isClient && isLocal){
            return address.substring(0, address.indexOf(COLON));
        }
        if(!isClient && !isLocal){
            return address.substring(0, address.indexOf(COLON));
        }
        return address;
    }

    private List<Statistics> deserializeStatisticsList(List<Response<byte[]>> respList){
        if(respList == null || respList.isEmpty()){
            return new ArrayList<Statistics>();
        }
        List<Statistics> result = new ArrayList<Statistics>();
        for(Response<byte[]> resp : respList){
            result.add((Statistics)SerializationUtils.deserialize(resp.get()));
        }
        return result;
    }


    @Override
    public void collect(List<Statistics> statisticsList, String ipFromReq){
        if(statisticsList == null || statisticsList.isEmpty()){
            return;
        }
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            List<byte[]> keys = genKeys(statisticsList, ipFromReq);
            List<byte[]> values = genValues(statisticsList, ipFromReq);
            Pipeline pipe = jedis.pipelined();
            for (int i = 0; i < keys.size(); i++) {
                pipe.setex(keys.get(i), (24 * 60 * 60), values.get(i)); // 过期时间写死1天
            }
            pipe.sync();
        } catch (Exception e) {
            logger.error("Collect statistics error! statisticsList: " + statisticsList + ", error message: " + e.getMessage(), e);
            if (pool != null && jedis != null){
                pool.returnBrokenResource(jedis);
            }
        } finally {
            if (pool != null && jedis != null){
                pool.returnResource(jedis);
            }
        }
    }

    private List<byte[]> genKeys(List<Statistics> statisticsList, String ipFromReq) throws UnsupportedEncodingException{
        List<byte[]> keys = new ArrayList<byte[]>(); //保证顺序
        for(Statistics statistics : statisticsList){
            keys.add(genBinaryKey(statistics, ipFromReq));
        }
        return keys;
    }
    
    private byte[] genBinaryKey(Statistics statistics, String ipFromReq) throws UnsupportedEncodingException{
        String app = statistics.getApplication();
        if(app == null || "".equals(app)){
            app = DEF_APP;
        }
        
        String localAddress = statistics.getMessage().getLocalAddress();
        if(statistics.isClient() && (localAddress == null || "".equals(localAddress))){
            localAddress = ipFromReq + DEF_PORT;
        }
        
        Long reportedTimestamp = Long.valueOf(statistics.getTimestamp());
        if(reportedTimestamp == null || "".equals(reportedTimestamp)){
            reportedTimestamp = new Date().getTime();
        }

        String readableTime = dateFormat.format(new Date(reportedTimestamp));
        
        String key = String.format(FORMAT_KEY,
                                    readableTime,
                                    app,
                                    String.valueOf(statistics.isClient()), 
                                    statistics.getService(),
                                    localAddress,
                                    statistics.getMessage().getRemoteAddress(),
                                    statistics.getMethod());
        return key.getBytes(UTF8);
    }

    private List<byte[]> genValues(List<Statistics> statisticsList, String ipFromReq){
        List<byte[]> values = new ArrayList<byte[]>(); //保证顺序
        for(Statistics statistics : statisticsList){
            byte[] value = genBinaryVal(statistics, ipFromReq);
            values.add(value);
        }
        return values;
    }
    
    private byte[] genBinaryVal(Statistics statistics, String ipFromReq){
        String app = statistics.getApplication();
        if(app == null || "".equals(app)){
            statistics.setApplication(DEF_APP);
        }

        StatisticsMessage msg = statistics.getMessage();
        if(statistics.isClient() && (msg.getLocalAddress() == null || "".equals(msg.getLocalAddress()))){
            msg.setLocalAddress(ipFromReq + DEF_PORT);
            statistics.setMessage(msg);
        }

        return SerializationUtils.serialize(statistics);
    }

    @Override
    public List<ExceptionRecord> getExceptionRecords(String appName, String service, boolean isClient,
                                                     String localAddress, String remoteAddress, String periodStart, String periodEnd) {
        
        List<ExceptionRecord> result = new ArrayList<ExceptionRecord>();
        JedisPool pool = null;
        Jedis jedis = null;
        List<String> keysToBeFQuery = genExceptionRecordFKeys(appName, service, isClient, localAddress, remoteAddress, Long.valueOf(periodStart), Long.valueOf(periodEnd));
        Set<byte[]> effectiveKeys = new HashSet<byte[]>();
        try {
            List<Response<byte[]>> respList = new ArrayList<Response<byte[]>>();
            jedis = pool.getResource();
            for(String key : keysToBeFQuery){
                effectiveKeys.addAll(jedis.keys(key.getBytes(UTF8)));
            }
            if(effectiveKeys == null || effectiveKeys.isEmpty()){
                return result;
            }

            Pipeline pipe = jedis.pipelined();
            for (byte[] effectiveKey : effectiveKeys) {
                Response<byte[]> resp = pipe.get(effectiveKey);
                respList.add(resp);
            }
            pipe.sync(); // 要结束了管道才能调用response的get()
            
            List<Statistics> statisticsList = deserializeStatisticsList(respList);
            doGetExceptionRecords(statisticsList, result);
            
        } catch (Exception e) {
            logger.error("Get exception records error! error message: " + e.getMessage(), e);
            if (pool != null && jedis != null){
                pool.returnBrokenResource(jedis);
            }
            return new ArrayList<ExceptionRecord>();
        } finally {
            if (pool != null && jedis != null){
                pool.returnResource(jedis);
            }
        }
        return result;
    }

    private void doGetExceptionRecords(List<Statistics> statisticsList, List<ExceptionRecord> result){
        if(statisticsList == null || statisticsList.size() == 0){
            return;
        }

        for(Statistics statistics : statisticsList){
            List<FailureMessage> fmList = statistics.getMessage().getFailure();
            if(fmList == null || fmList.size() == 0){
                continue;
            }
            for(FailureMessage fm : fmList){
                ExceptionRecord er = new ExceptionRecord();
                er.setElapsed(nsToMs(fm.getElapsed()));
                er.setException(fm.getException());
                er.setInput(fm.getInput());
                String dateStr = readableDateFormat.format(new Date(fm.getTimestamp()));
                er.setTimePoint(dateStr);
                result.add(er);
            }
        }
    }
    
    private List<String> genExceptionRecordFKeys(String appName, String service, Boolean isClient,
            String localAddress, String remoteAddress, long periodStart, long periodEnd){
        
        List<String> keysToBeFQuery = new ArrayList<String>();
        
        // 支持多小时动态生成。目前只去到小时的粒度
        long tempTimestamp = periodStart;
        Calendar ca = Calendar.getInstance();
        while(periodEnd >= tempTimestamp){
            Date tempDate = new Date(tempTimestamp);
            String tempHour = hourFormat.format(tempDate);
            if(isClient){
                keysToBeFQuery.add(tempHour + FUZZY_MARK + KEY_SEP
                        + appName               + KEY_SEP
                        + isClient.toString()   + KEY_SEP
                        + service               + KEY_SEP
                        + localAddress          + FUZZY_MARK + KEY_SEP
                        + remoteAddress         + KEY_SEP + FUZZY_MARK);   //如： 2014092816*|test|false|com.yy.cs.demo.Scribe|127.0.0.1*|/127.0.0.1:1234|*
            }else{
                keysToBeFQuery.add(tempHour + FUZZY_MARK + KEY_SEP
                        + appName               + KEY_SEP
                        + isClient.toString()   + KEY_SEP
                        + service               + KEY_SEP
                        + localAddress          + KEY_SEP
                        + remoteAddress         + FUZZY_MARK);   //如： 2014092816*|test|false|com.yy.cs.demo.Scribe|127.0.0.1:1234|/127.0.0.1*
            }

            ca.setTime(tempDate);
            // 加一个小时
            ca.add(Calendar.HOUR_OF_DAY, +1);
            tempTimestamp = ca.getTime().getTime();
        }
        return keysToBeFQuery;
    }

}
