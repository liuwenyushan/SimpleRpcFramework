package com.doit.rpc.handler;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;

import com.doit.rpc.bean.RpcRequest;
import com.doit.rpc.bean.RpcResponse;

public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private Logger logger = Logger.getLogger(RpcHandler.class);
	private final Map<String, Object> handlerMap;

	/**
	 * @param handlerMap
	 *            <服务类接口名称，实现服务接口的类实例>
	 * 
	 */
	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request)
			throws Exception {

		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());

		try {
			Object result = handle(request);
			response.setResult(result);
		} catch (Throwable e) {
			response.setError(e);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Object serviceBean = handlerMap.get(className);

		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		Method method = serviceClass.getMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(serviceBean, parameters);

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error("server caught exception", cause);
		ctx.close();
	}

}
