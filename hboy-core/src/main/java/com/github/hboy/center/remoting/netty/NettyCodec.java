package com.github.hboy.center.remoting.netty;


import com.github.hboy.center.remoting.Channel;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.github.hboy.center.remoting.RemotingException;
import com.github.hboy.center.remoting.codec.Codec;

/**
 * @author xiaobenhai
 * Date: 2016/2/12
 * Time: 18:36
 */
public class NettyCodec {

    private ChannelDownstreamHandler encoder;

    private ChannelUpstreamHandler decoder;

    public NettyCodec(Codec codec,Channel client) {
        if(codec == null){
        	throw new  IllegalArgumentException("codec == null");
        }
        this.encoder = new NettyEncoder(codec);
        this.decoder = new NettyDecoder(codec);
    }

    public ChannelDownstreamHandler getEncoder() {
        return encoder;
    }

    public ChannelUpstreamHandler getDecoder() {
        return decoder;
    }

    class NettyDecoder extends FrameDecoder {

        private Codec codec;

        public NettyDecoder(Codec codec) {
            this.codec = codec;
        }
        
		@Override
		public Object decode(ChannelHandlerContext ctx, org.jboss.netty.channel.Channel channel,
				ChannelBuffer buffer) throws Exception {
			if (buffer == null) {
				throw new RemotingException(
						RemotingException.SERIALIZATION_EXCEPTION, "Could not decode ChannelBuffer,buffer is null");
			}
			int readable = buffer.readableBytes();
	        if (readable < 4) {
				return null;
	        }
			return codec.decode(buffer);
		}
    }

    class NettyEncoder extends OneToOneEncoder {

        private Codec codec;

        public NettyEncoder(Codec codec) {
            this.codec = codec;
        }

		@Override
		protected Object encode(ChannelHandlerContext ctx, org.jboss.netty.channel.Channel channel,
				Object msg) throws Exception {
			if(msg == null){
				throw new IllegalArgumentException("msg is null.");
			}
			return codec.encode(msg);
		}
    }
}
