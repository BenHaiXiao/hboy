package com.github.hboy.center.remoting.codec;

import com.github.hboy.common.util.Constants;
import com.github.hboy.center.remoting.codec.thrift.ThriftCodec;
import com.github.hboy.common.config.InvokerConfig;

/**
 * @author xiaobenhai
 * Date: 2016/3/17
 * Time: 22:14
 */
public class CodecFactory {

    private CodecFactory(){}
    
    public static Codec getCodec(InvokerConfig url){
        Codec codec = null;
        if(url.getServiceProtocol() == Constants.ServiceProtocolType.thrift){
            codec = new ThriftCodec(url);
        }else{
            codec = new ThriftCodec(url);
        }
        //todo 扩展协议
        return codec;
    }
	 
}
