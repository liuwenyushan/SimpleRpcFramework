package com.doit.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.doit.rpc.annotation.RpcService;
import com.doit.rpc.bean.RpcRequest;
import com.doit.rpc.bean.RpcResponse;
import com.doit.rpc.handler.RpcDecoder;
import com.doit.rpc.handler.RpcEncoder;
import com.doit.rpc.handler.RpcHandler;
import com.doit.rpc.registry.ServiceRegistry;
import com.doit.rpc.util.FindClassesUtil;

public class RpcServer {
	private String serverAdress;
	private ServiceRegistry serviceRegistry;

	private Logger logger = Logger.getLogger(RpcServer.class);
	private final String packageName = "com.doit.rpc.service";

	// 存放接口与RPC服务对象之间的映射关系
	private Map<String, Object> handlerMap = new HashMap<>();

	public RpcServer(String serverAdress, ServiceRegistry serviceRegistry) {
		this.serverAdress = serverAdress;
		this.serviceRegistry = serviceRegistry;
		setRpcBean();
		startServer();

	}

	public void setRpcBean() {
		// 获取指定包下所有类名称
		try {
			List<String> classNames = FindClassesUtil.getClassName(packageName);

			// 扫描指定包下所有类，获取带有RpcService注解的类存入handlerMap中
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				if (clazz.getAnnotation(RpcService.class) != null) {
					String interfaceName = clazz
							.getAnnotation(RpcService.class).value().getName();
					handlerMap.put(interfaceName, clazz.newInstance());
				}
			}
		} catch (Exception e) {
			logger.error("获取服务类错误");
		}
	}

	// 启动服务器
	public void startServer() {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap
					.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel channel)
								throws Exception {
							// 对Rpc请求进行处理
							channel.pipeline()
									.addLast(new RpcDecoder(RpcRequest.class))// 将Rpc请求进行解码
									.addLast(new RpcEncoder(RpcResponse.class))// 将Rpc响应进行编码
									.addLast(new RpcHandler(handlerMap));// 处理Rpc请求
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			String[] array = serverAdress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);

			ChannelFuture future = bootstrap.bind(host, port).sync();

			if (serviceRegistry != null) {
				// 注册服务地址
				serviceRegistry.register(serverAdress);
			}
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();

		}
	}
}
