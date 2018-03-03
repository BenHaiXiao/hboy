package com.github.hboy.center.thrift.protocol;

import com.github.hboy.common.util.Constants;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.transport.TTransport;

/**
 * @author xiaobenhai
 * Date: 2016/3/19
 * Time: 21:17
 */
public class ThriftProtocolFactory {
	
	private ThriftProtocolFactory(){}
	
	public static TProtocol getTProtocol(final TTransport transport,final Constants.ThriftProtocolType type){
		
		TProtocol protocol = null;
		if(type == Constants.ThriftProtocolType.BINARY){
			 protocol = new TBinaryProtocol(transport);
		}else if (type == Constants.ThriftProtocolType.COMPACT){
			protocol = new TCompactProtocol(transport);
		}else if (type == Constants.ThriftProtocolType.JSON){
			protocol = new TJSONProtocol(transport);
		}else if (type == Constants.ThriftProtocolType.SIMPLEJSON){
			protocol = new TSimpleJSONProtocol(transport);
		}else{
			 protocol = new TBinaryProtocol(transport);
		}
		return protocol;
	}

	
	public static TProtocolFactory getTProtocolFactory(final Constants.ThriftProtocolType type){
		
		TProtocolFactory protocol = null;
		if(type == Constants.ThriftProtocolType.BINARY){
			 protocol = new TBinaryProtocol.Factory();
		}else if (type == Constants.ThriftProtocolType.COMPACT){
			protocol = new TCompactProtocol.Factory();
		}else if (type == Constants.ThriftProtocolType.JSON){
			protocol = new TJSONProtocol.Factory();
		}else if (type == Constants.ThriftProtocolType.SIMPLEJSON){
			protocol = new TSimpleJSONProtocol.Factory();
		}else{
			 protocol = new TBinaryProtocol.Factory();
		}
		return protocol;
	}
}
