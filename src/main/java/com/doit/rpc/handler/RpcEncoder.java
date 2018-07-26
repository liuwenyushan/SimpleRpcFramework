package com.doit.rpc.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.doit.rpc.util.SerializationUtil;

/**
 * @author liuxuefeng
 * @funtion Rpc响应编码
 */
@SuppressWarnings("rawtypes")
public class RpcEncoder extends MessageToByteEncoder {
	private Class<?> genericClass;

	public RpcEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out)
			throws Exception {

		if (genericClass.isInstance(in)) {
			byte[] data = SerializationUtil.serilaze(in);
			out.writeInt(data.length);
			out.writeBytes(data);
		}

	}

}
