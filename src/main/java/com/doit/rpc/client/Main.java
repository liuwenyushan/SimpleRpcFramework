package com.doit.rpc.client;

import com.doit.rpc.service.MyService;
import com.doit.rpc.util.PropertiesUtil;

public class Main {

	public static void main(String[] args) throws Exception {

		String registryAddress = PropertiesUtil.getProperty("registry.address");
		RpcProxy rpcProxy = new RpcProxy(new ServiceDiscovery(registryAddress));
		MyService myService = rpcProxy.create(MyService.class);
		String result = myService.show("你好");
		System.out.println(result);
	}
}
