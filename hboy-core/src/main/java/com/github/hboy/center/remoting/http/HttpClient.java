package com.github.hboy.center.remoting.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hboy.center.proxy.Invocation;
import com.github.hboy.center.remoting.exchange.Request;
import com.github.hboy.center.remoting.exchange.ResponseFuture;
import com.github.hboy.common.config.annotation.http.HttpMethod;
import com.github.hboy.common.config.annotation.http.HttpParam;
import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.exchange.Response;
import com.github.hboy.common.config.InvokerConfig;
import com.github.hboy.common.config.annotation.http.HttpPath;

/**
 * http请求发起的客户端
 * @author xiaobenhai
 * Date: 2016/4/11
 * Time: 13:36
 */
public class HttpClient implements HttpRequestExecutor{

    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HTTP_CONTENT_TYPE_SIMPLE_FORM = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String HTTP_QUERY_STRING_EQUAL_MARK = "=";
    private static final String HTTP_QUERY_STRING_AND_MARK = "&";
    private static final String HTTP_SCHEME_FORMAT = "http://";
    // 必须是char，因为String单字符和char比较总是false
    private static final Character HTTP_PATH_SLASH = '/';
    private static final int SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE = 1024;
    
    private final static ObjectMapper jacksonMapper = new ObjectMapper();

    private enum HttpMethodType{
        GET,POST;
    }

    static{
        jacksonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jacksonMapper.setSerializationInclusion(Include.NON_NULL);
    }
    
    private final InvokerConfig invokerConfig;

    public InvokerConfig getInvokerConfig() {
        return invokerConfig;
    }

    public HttpClient(InvokerConfig invokerConfig){
        if (invokerConfig == null) {
            throw new IllegalArgumentException("invokerConfig == null");
        }
        this.invokerConfig = invokerConfig;
    }

    @Override
    public ResponseFuture executeRequest(Request request) throws RemotingException {
        checkRequest(request);
        Invocation invocation = request.getData();
        HttpURLConnection con = prepareRequest(invocation);
        validateResponse(con);
        InputStream responseBody = readResponseBody(con);
        return readRemoteInvocationResult(responseBody,invocation);
    }

    private void checkRequest(Request request) throws RemotingException{
        Method method = request.getData().getMethod();
        Object[] args = request.getData().getParameters();
        List<String> paramKeys = getHttpParamKeyList(method);
        
        // 没有声明http参数的key，并且参数的value大于1个
        if((paramKeys == null || paramKeys.size() == 0) && (args != null && args.length > 1)){
            throw new RemotingException("Illegal request! paramKeys.size=0, args.length=" + args.length);
        }

        // 有http参数的key，并且没有参数的value值(在配置的角度，这是不可能出现的情况)
        if((paramKeys != null && paramKeys.size() > 0) && (args == null || args.length ==0)){
            throw new RemotingException("Illegal request! paramKeys.size=" + paramKeys.size() + ", args.length=0");
        }

        // 有http参数的key，并且有参数，但是两者数量不一致
        if((paramKeys != null && paramKeys.size() > 0) && (args != null && args.length > 0) && (paramKeys.size() != args.length)){
            throw new RemotingException("Illegal request! paramKeys.size=" + paramKeys.size() + ", args.length=" + args.length);
        }

        return;
    }
    
    /**
     * 丑陋的嵌套，只为遍历出方法中的入参的@HttpParam注解的值...
     * @param method
     * @param args
     * @return
     */
    private List<String> getHttpParamKeyList(Method method){
        List<String> result = new ArrayList<String>();
        Annotation[][] annos = method.getParameterAnnotations();
        if(annos != null && annos.length > 0){
            for(int i = 0; i < annos.length; i++){
                if(annos[i] != null && annos[i].length > 0){
                    for(int j = 0; j < annos[i].length; j++){
                        if(annos[i][j] != null && annos[i][j] instanceof HttpParam){
                            result.add(((HttpParam) annos[i][j]).name());
                        }
                    }
                }
            }
        }
        return result;
    }

    private ResponseFuture readRemoteInvocationResult(InputStream is, Invocation invocation) throws RemotingException{
        try {
            return doReadRemoteInvocationResult(is, invocation);
        } catch(IOException e){
            throw new RemotingException("Read remote http invocation result error! Something serious happened!", e);
        }
        finally {
            try {
                // Calling the close() methods on the InputStream or OutputStream of an HttpURLConnection 
                // after a request may free network resources associated with this instance 
                // but has no effect on any shared persistent connection.
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                throw new RemotingException("Read remote http invocation result error while close the input stream! Something serious happened!", e);
            }
        }
    }
    
    /**
     * 核心代码
     * 解析远程服务调用后响应的数据流
     * TODO:处理非复杂类型转换，处理泛型的映射，还要处理void的情况
     * @param is
     * @param invocation
     * @return
     * @throws IOException 
     */
    private HttpResponseFuture doReadRemoteInvocationResult(InputStream is, Invocation invocation) throws IOException{
        //TODO: 目前粗暴地认为响应的内容是json字符串
        String jsonBody = getStringFromIs(is);
        Object bizResult = deserialize(jsonBody, invocation.getMethod().getReturnType());
        Response resp = new Response();
        resp.setResult(bizResult);
        return new HttpResponseFuture(resp);
    }

    private static Object deserialize(String jsonBody, Class<?> returnType) throws JsonParseException, JsonMappingException, IOException{
        if(returnType.isAssignableFrom(void.class) || returnType.isAssignableFrom(Void.class)){
            return null;
        }
        if(returnType.isAssignableFrom(String.class)){
            return jsonBody;
        }
        // TODO: 需要处理primitive类型
        return jacksonMapper.readValue(jsonBody, returnType);
    }

    private String getStringFromIs(InputStream is) throws IOException{
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
         } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new IOException("Read remote http invocation result error while close the buffered reader! Something serious happened!", e);
                }
            }
        }
        return sb.toString();
    }

    private InputStream readResponseBody(HttpURLConnection con) throws RemotingException{
        try {
            return con.getInputStream();
        } catch (IOException e) {
            throw new RemotingException("Read http response body error while get input stream from httpURLConnection!", e);
        }
    }

    private void validateResponse(HttpURLConnection con) throws RemotingException{
        try {
            if (con.getResponseCode() >= 300) {
                throw new IOException(
                        "Did not receive successful HTTP response: status code = " + con.getResponseCode() +
                        ", status message = [" + con.getResponseMessage() + "]");
            }
        } catch (IOException e) {
            throw new RemotingException("Response from http server is invalid! connection :"+ con.toString() + " error message:" + e.getMessage(), e);
        }
    }

    private void writeRequestBody(HttpURLConnection con, ByteArrayOutputStream baos) throws RemotingException{
        try {
            baos.writeTo(con.getOutputStream());
        } catch (IOException e) {
            throw new RemotingException("Write request body error!", e);
        }
    }
    
    private HttpURLConnection prepareRequest(Invocation invocation) throws RemotingException{
        HttpMethod httpMethod = invocation.getMethod().getAnnotation(HttpMethod.class);
        HttpMethodType methodType = getHttpMethodType(httpMethod);
        
        HttpURLConnection connection;
        try {
            connection = openConnection(this.invokerConfig, invocation, methodType);
        } catch (Throwable t) {
            throw new RemotingException("OpenConnection error! invocation=" + invocation, t);
        }
        
        if (this.invokerConfig.getTimeout() >= 0) {
            connection.setConnectTimeout(this.invokerConfig.getTimeout());
            connection.setReadTimeout(this.invokerConfig.getTimeout());
        }

        if(HttpMethodType.GET == methodType){
            connection.setDoOutput(false);
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new RemotingException("Set http request method error!", e);
            }
            // 不设置Content-Type，默认是纯文本text/plain
        }

        if(HttpMethodType.POST == methodType){
            ByteArrayOutputStream baos = getByteArrayOutputStream(invocation);
            connection.setDoOutput(true);
            try {
                connection.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new RemotingException("Set http request method error!", e);
            }
            connection.setRequestProperty(HTTP_HEADER_CONTENT_TYPE, HTTP_CONTENT_TYPE_SIMPLE_FORM); // form表单提交
            connection.setRequestProperty(HTTP_HEADER_CONTENT_LENGTH, Integer.toString(baos.size()));
            writeRequestBody(connection, baos);
        }
        
        return connection;
    }
    
    private HttpMethodType getHttpMethodType(HttpMethod httpMethod){
        HttpMethodType result = HttpMethodType.POST; // default
        if(httpMethod != null){
            if("get".equalsIgnoreCase(httpMethod.value())){
                result = HttpMethodType.GET;
            }
            if("post".equalsIgnoreCase(httpMethod.value())){
                result = HttpMethodType.POST;
            }
        }
        return result;
    }

    private HttpURLConnection openConnection(InvokerConfig invokerConfig, Invocation invocation, HttpMethodType methodType) throws RemotingException, JsonProcessingException, UnsupportedEncodingException{
        String httpRealUrl = getHttpRealUrl(invokerConfig, invocation, methodType);
        URLConnection con = null;
        try {
            con = new URL(httpRealUrl).openConnection();
        } catch (Throwable t) {
            throw new RemotingException("Open url connection error! httpRealUrl=" + httpRealUrl + ", error message:" + t.getMessage(), t);
        }
        if (!(con instanceof HttpURLConnection)) {
            throw new RemotingException("Service URL [" + httpRealUrl + "] is not an HTTP URL");
        }
        return (HttpURLConnection) con;
    }
    
    /**
     * 核心代码
     * 获取http请求真实协议路径
     * 注意一个约定：
     * 有HttpPath注解时，使用注解中的静态path配置组装httpRealUrl
     * 无HttpPath注解时，使用invocation中的动态path组装httpRealUrl
     * @param config
     * @param invocation
     * @return
     * @throws UnsupportedEncodingException 
     * @throws JsonProcessingException 
     */
    private String getHttpRealUrl(InvokerConfig config, Invocation invocation, HttpMethodType methodType) throws JsonProcessingException, UnsupportedEncodingException{
        StringBuilder httpRealUrlBuilder = new StringBuilder();
        httpRealUrlBuilder.append(HTTP_SCHEME_FORMAT).append(config.getAddress());

        HttpPath rootHttpPathAnnotation = invocation.getServiceType().getAnnotation(HttpPath.class);
        HttpPath childHttpPathAnnotation = invocation.getMethod().getAnnotation(HttpPath.class);
        if(rootHttpPathAnnotation == null && childHttpPathAnnotation == null){
            String path = invocation.getPath();
            if(!path.startsWith("" + HTTP_PATH_SLASH)){
                path = HTTP_PATH_SLASH + path;
            }
            httpRealUrlBuilder.append(path);
            return httpRealUrlBuilder.toString();
        }
        if(rootHttpPathAnnotation != null){
            httpRealUrlBuilder.append(keepPathFit(rootHttpPathAnnotation.value()));
        }
        if(childHttpPathAnnotation != null){
            httpRealUrlBuilder.append(keepPathFit(childHttpPathAnnotation.value()));
        }
        
        if(HttpMethodType.GET == methodType){
            String queryString = doAssembleQueryString(getHttpParamKeyList(invocation.getMethod()), invocation.getParameters());
            if(queryString != null && !"".equals(queryString)){
                httpRealUrlBuilder.append("?" + queryString);
            }
        }
        
        return httpRealUrlBuilder.toString();
    }

    private String keepPathFit(String path){
        if(path == null || "".equals(path)){
            return "";
        }
        StringBuilder cutter = new StringBuilder(path);
        if(HTTP_PATH_SLASH.equals(cutter.charAt(cutter.length() - 1))){
            // 干掉最后一位的斜杆
            cutter.deleteCharAt(cutter.length() - 1);
        }
        if(!HTTP_PATH_SLASH.equals(cutter.charAt(0))){
            // 补充第一位的斜杆
            cutter.insert(0, HTTP_PATH_SLASH);
        }
        return cutter.toString();
    }
    
    private ByteArrayOutputStream getByteArrayOutputStream(Invocation invocation) throws RemotingException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE);
        try {
            writeRemoteInvocation(invocation, baos);
        } catch (Throwable t) {
            throw new RemotingException("Gen output stream error before send request t.getMessage()=" + t.getMessage(), t);
        }
        return baos;
    }

    private void writeRemoteInvocation(Invocation invocation, OutputStream os) throws IOException, IllegalAccessException{
        try {
            doWriteRemoteInvocation(invocation, os);
        }
        finally {
            os.close();
        }
    }
    
    private void doWriteRemoteInvocation(Invocation invocation, OutputStream os) throws IOException, IllegalAccessException{
        String httpBodyContent = doAssembleBody(invocation.getMethod(), invocation.getParameters());
        os.write(httpBodyContent.getBytes("utf-8"));
    }

    /**
     * 核心代码
     * 根据远程调用的相关参数信息，生成http请求中body的内容
     * @param invocation
     * @return
     * @throws JsonProcessingException 
     * @throws IllegalAccessException 
     * @throws UnsupportedEncodingException 
     */
    private String doAssembleBody(Method method, Object[] args) throws JsonProcessingException, IllegalAccessException, UnsupportedEncodingException{
        if(args == null || args.length == 0){
            return "";
        }
        
        StringBuilder httpBodyBuilder = new StringBuilder();
        List<String> paramKeys = getHttpParamKeyList(method);
        if((paramKeys == null || paramKeys.size() == 0) && (args.length == 1)){
            httpBodyBuilder.append(serialize(args[0]));
            return httpBodyBuilder.toString();
        }
        
        return doAssembleQueryString(paramKeys, args);
    }

    /**
     * return k1=v1&k2=v2&k3=v3
     * @throws JsonProcessingException 
     * @throws UnsupportedEncodingException 
     */
    private String doAssembleQueryString(List<String> paramKeys, Object[] args) throws JsonProcessingException, UnsupportedEncodingException{
        if(paramKeys == null || paramKeys.size() == 0 || args == null || args.length == 0){
            return "";
        }
        StringBuilder queryStringBuilder = new StringBuilder();
        for(int i = 0; i < paramKeys.size(); i++){
            String paramKey = paramKeys.get(i);
            Object paramVal = args[i];
            if(paramKey == null || "".equals(paramKey) || paramVal == null){
                continue;
            }
            String paramValJson = serialize(paramVal);
            queryStringBuilder.append(paramKey.trim() + HTTP_QUERY_STRING_EQUAL_MARK + URLEncoder.encode(paramValJson, "UTF-8") + HTTP_QUERY_STRING_AND_MARK);
        }
        if(queryStringBuilder.length() == 0){
            return "";
        }
        // 干掉最后一个多余的字符"&"
        return queryStringBuilder.deleteCharAt(queryStringBuilder.length() - 1).toString();
    }

    /**
     * TODO:处理其他基本类型的情况，以及泛型的情况
     */
    private String serialize(Object paramVal) throws JsonProcessingException{
        if(paramVal.getClass().isAssignableFrom(String.class)){
            return paramVal.toString();
        }
        return jacksonMapper.writeValueAsString(paramVal);
    }
    
}
