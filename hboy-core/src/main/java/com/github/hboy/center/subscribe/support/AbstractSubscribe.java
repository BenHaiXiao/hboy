package com.github.hboy.center.subscribe.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.hboy.center.subscribe.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.common.config.InvokerConfig;
/**
 * @author xiaobenhai
 * Date: 2016/3/27
 * Time: 19:26
 */
public abstract class AbstractSubscribe implements Subscribe {

    // 日志输出
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // URL地址分隔符，用于文件缓存中，
    private static final String URL_SPLIT = "\\s+";

    private static final char SPLIT = ' ';
    
    // 本地磁盘缓存文件
    private  File file;

    // 本地磁盘缓存，其中key值为服务名称，value为服务提供者列表
    private final Properties properties = new Properties();

//    // 文件缓存定时写入
    private final ExecutorService cacheExecutor = Executors.newFixedThreadPool(1);

    protected final static ObjectMapper mapper = new ObjectMapper();
    
    public AbstractSubscribe() {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL); 
        this.file = loadCacheFile();
        loadProperties();
    }
    
    private File loadCacheFile(){
    	String cacheFile = ".center/center-subscribe.cache";
    	String dragonProjName = System.getProperty("dragon.bizName.projName");
    	String parent = null;
    	if(dragonProjName != null && !"".equals(dragonProjName)){
    		String p = "/data/file";
    		File f = new File(p);
    		if(f.exists() && f.isDirectory()){
    			parent = p + File.separator + dragonProjName;	
    		}
    	}
    	if(parent == null || "".equals(parent)){
    		parent = System.getProperty("user.home");
    	}
    	if(!parent.endsWith(File.separator)){
    		parent += File.separator;
    	}
    	String filename = parent + cacheFile;
		File file = null;
		if (filename != null && !"".equals(filename)) {
			file = new File(filename);
			if (!file.exists() && file.getParentFile() != null
					&& !file.getParentFile().exists()) {
				if (!file.getParentFile().mkdirs()) {
					 throw new
					 IllegalArgumentException("Failed to create loacl directory "
					 + file.getParentFile() + "!");
				}
			}
		}
    	return file;
    }
    
    public File getCacheFile() {
        return file;
    }

    public Properties getCacheProperties() {
        return properties;
    }

    private class SaveProperties implements Runnable{
        public void run() {
            doSaveProperties();
        }
    }
    
    public void doSaveProperties() {
        if (file == null) {
            return;
        }
        Properties newProperties = new Properties();
        InputStream in = null;
        try {
            if (file.exists()) {
                in = new FileInputStream(file);
                newProperties.load(in);
            }
        } catch (Throwable e) {
            logger.warn("Failed to load registry store file, cause: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }     
     // 保存
		try {
			newProperties.putAll(properties);
			// 保存
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream outputFile = new FileOutputStream(file);
			try {
				newProperties.store(outputFile, "");
			} finally {
				outputFile.close();
			}
		} catch (Throwable e) {
			cacheExecutor.execute(new SaveProperties());
		}
    }
    
    /**
     * 加载到Propertie 属性中
     */
    private void loadProperties() {
        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                properties.load(in);
                if (logger.isInfoEnabled()) {
                    logger.info("Load file" + file + ", data: " + properties);
                }
            } catch (Throwable e) {
                logger.warn("Failed to load registry store file " + file, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }

    public List<String> getCacheUrls(InvokerConfig url) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key != null && key.length() > 0 && key.equals(url.getInterfaceName())
                    && value != null && value.length() > 0) {
            	
                String[] arr = value.split(URL_SPLIT);
                List<String> urls = new ArrayList<String>();
                for (String u : arr) {
                	if(u != null && !"|".equals(u.trim())){
                		urls.add(u);
                	}
                }
                if (logger.isInfoEnabled()) {
                    logger.info("ClientURL:"+ url.toString() + "Load Cache urls" + ", data: " + urls);
                }
                return urls;
            }
        }
        return null;
    }

    public void saveProperties(String serviceName,List<InvokerConfig> urls) {
        if (file == null) {
            return;
        }
		try {
			StringBuilder buf = new StringBuilder();
			for (InvokerConfig u : urls) {
			    String str = "";
			    try{
			        str = mapper.writeValueAsString(u);
			    }catch(JsonProcessingException t){
			        logger.warn(t.getMessage(), t);
			    }
				if (buf.length() > 0) {
					buf.append(SPLIT);
				}
				buf.append(str);
			}
			properties.setProperty(serviceName, buf.toString());
			cacheExecutor.execute(new SaveProperties());
		} catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }
}