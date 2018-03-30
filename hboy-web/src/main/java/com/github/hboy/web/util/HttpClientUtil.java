package com.github.hboy.web.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class HttpClientUtil {
	
	 private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);
	
	public static final String HTTP_HEADER_XRP = "X-Real-IP";
	
	public static final String HTTP_HEADER_XFF = "X-Forwarded-For";
	
	public static final String DEFAULT_CHARSET = "UTF-8";
	

    public static String toQueryString(Map<String, String> params) throws UnsupportedEncodingException {

        ArrayList<String> p = new ArrayList<String>();
        for (Entry<String, String> entry : params.entrySet()) {
            p.add(encode(entry.getKey())  + "=" + encode(entry.getValue()));
        }
        StringBuffer sb = new StringBuffer(); 
        for(int i = 0; i<p.size(); i++){
        	sb.append(p.get(i));
        	if(i != p.size()-1){
        		sb.append('&');
        	}
        }
        return sb.toString();
    }
    
    public static String encode(String value) {
        if (value == null || value.length() == 0) { 
            return "";
        }
        try {
            return URLEncoder.encode(value, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static String decode(String value) {
        if (value == null || value.length() == 0) { 
            return "";
        }
        try {
            return URLDecoder.decode(value, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * 获取request 地址
     * 版本1.0.2之后调整了优先级，X-Forwarded-For优先获取
     * @param request
     * @return
     */
    public static String getRequestorIp(HttpServletRequest request) {

        // 优先级1：从X-Forwarded-For中取，有被伪造的风险
        // 适用于多级nginx代理的形式
        String xff = request.getHeader(HTTP_HEADER_XFF);
        if (StringUtils.isNotBlank(xff)) {
            if (xff.indexOf(",") < 0) {
                return xff;
            }
            return StringUtils.substringBefore(xff, ",");
        }
        
        // 优先级2：从X-Real-IP中取，有多重代理时，这个地址可能不准确，但一般不会被伪造
        String ip = request.getHeader(HTTP_HEADER_XRP);
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }


        // 优先级3：直接获取RemoteAddr，有反向代理时这个地址会不准确
        return request.getRemoteAddr();
    }
    
}
