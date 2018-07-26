package com.doit.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import com.doit.rpc.bean.RpcRequest;
import com.doit.rpc.bean.RpcResponse;

public class RpcProxy {
	private String serverAddress;
	private ServiceDiscovery serviceDiscovery;

	public RpcProxy(ServiceDiscovery serviecDiscovery) {
		this.serviceDiscovery = serviecDiscovery;
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass) {
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),
				new Class<?>[] { interfaceClass }, new InvocationHandler() {
					@Override
					// 所有动态代理类的方法调用，都会交由InvocationHandler接口实现类里的invoke()方法去处理
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						// 生成Rpc请求
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						request.setClassName(method.getDeclaringClass()
								.getName());
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);

						if (serviceDiscovery != null) {
							// 发现服务地址
							serverAddress = serviceDiscovery.discover();
						}
						if (serverAddress != null) {
							String[] array = serverAddress.split(":");
							String host = array[0];
							int port = Integer.parseInt(array[1]);
							RpcClient client = new RpcClient(host, port);
							RpcResponse response = client.send(request);
							if (response.hasError()) {
								throw response.getError();
							} else {
								return response.getResult();
							}
						} else {
							throw new Exception("没有找到Server");
						}

					}
				});
	}
}
