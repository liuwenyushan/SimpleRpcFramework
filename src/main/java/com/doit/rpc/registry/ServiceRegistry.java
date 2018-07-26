package com.doit.rpc.registry;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class ServiceRegistry {
	// 利用zookeeper实现服务注册功能
	private static final Logger logger = Logger
			.getLogger(ServiceRegistry.class);
	private CountDownLatch latch = new CountDownLatch(1);

	private String registryAddress;

	public ServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public void register(String data) {
		if (data != null) {
			ZooKeeper zk = connectServer();
			if (zk != null) {
				createNode(zk, data);
			}
		}
	}

	private ZooKeeper connectServer() {
		ZooKeeper zk = null;

		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
					new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							if (event.getState() == Event.KeeperState.SyncConnected)
								// 当zookeeper处于已连接状态CountDownLatch值-1，唤醒线程
								latch.countDown();
						}
					});
			// 线程等待
			latch.await();
		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
		return zk;
	}

	private void createNode(ZooKeeper zk, String data) {
		try {
			byte[] bytes = data.getBytes();
			// 在zookeeper中创建临时顺序节点，path为/registry/data00000000,data为服务地址
			String path = zk.create(Constant.ZK_DATA_PATH, bytes,
					ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			logger.debug("create zookeeper noode ({" + path + "}=>{" + data
					+ "})");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
