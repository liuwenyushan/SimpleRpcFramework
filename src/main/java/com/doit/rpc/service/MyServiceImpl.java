package com.doit.rpc.service;

import com.doit.rpc.annotation.RpcService;

@RpcService(MyService.class)
public class MyServiceImpl implements MyService {
	@Override
	public String show(String message) {
		return "收到信息:" + message;
	}
}
