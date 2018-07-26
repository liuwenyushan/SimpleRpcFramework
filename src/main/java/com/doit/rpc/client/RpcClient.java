package com.doit.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import org.apache.log4j.Logger;

import com.doit.rpc.bean.RpcRequest;
import com.doit.rpc.bean.RpcResponse;
import com.doit.rpc.handler.RpcDecoder;
import com.doit.rpc.handler.RpcEncoder;

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

	private static final Logger logger = Logger.getLogger(RpcClient.class);

	private String host;
	private int port;
	private RpcResponse response;

	private final Object obj = new Object();

	public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response)
			throws Exception {
		this.response = response;
		// 收到resopnse后唤醒线程,返回响应
		synchronized (obj) {
			obj.notifyAll();
		}

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		logger.error("client caught exception", cause);
		ctx.close();

	}

	public RpcResponse send(RpcRequest request) throws InterruptedException {
		EventLoopGroup group = new NioEventLoopGroup();
		logger.info("开始发送请求");
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class)
					.handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline()
									.addLast(new RpcEncoder(RpcRequest.class))// 将Rpc请求进行编码
									.addLast(new RpcDecoder(RpcResponse.class))// 将Rpc响应进行解码
									.addLast(RpcClient.this);// 使用RpcClient发送请求
						}
					});
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();

			// 未收到response时阻塞线程
			synchronized (obj) {
				obj.wait();
			}
			if (response != null) {
				future.channel().closeFuture().sync();
			}
			// logger.info("请求发送成功，返回响应" + response.getResult().toString());
			return response;

		} finally {
			group.shutdownGracefully();
		}
	}
}
