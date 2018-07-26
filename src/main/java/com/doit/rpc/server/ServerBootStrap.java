package com.doit.rpc.server;

import com.doit.rpc.registry.ServiceRegistry;
import com.doit.rpc.util.PropertiesUtil;

public class ServerBootStrap {
	public static void main(String[] args) throws Exception {

		// 生成服务注册对象
		String registryAdress = PropertiesUtil.getProperty("registry.address");
		ServiceRegistry serviceRegistry = new ServiceRegistry(registryAdress);

		// 启动服务器，并在zookeeper上注册服务地址
		String serverAdress = PropertiesUtil.getProperty("server.address");

		RpcServer rpcServer = new RpcServer(serverAdress, serviceRegistry);
	}
}
