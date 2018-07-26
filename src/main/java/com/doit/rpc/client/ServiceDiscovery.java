package com.doit.rpc.client;

import io.netty.util.internal.ThreadLocalRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.doit.rpc.registry.Constant;

public class ServiceDiscovery {

	private static final Logger logger = Logger
			.getLogger(ServiceDiscovery.class);
	private CountDownLatch latch = new CountDownLatch(1);
	private volatile List<String> dataList = new ArrayList<>();
	private String registryAddress;

	public ServiceDiscovery(String registryAddress) {
		this.registryAddress = registryAddress;

		ZooKeeper zk = connectServer();
		if (zk != null) {
			watchNode(zk);
		}
	}

	public String discover() {
		String data = null;
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				logger.info("using only data: " + data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				logger.info("using random data: " + data);
			}
		}
		return data;
	}

	private ZooKeeper connectServer() {
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
					new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							if (event.getState() == Event.KeeperState.SyncConnected) {
								// 当zookeeper处于已连接状态CountDownLatch值-1，唤醒线程
								latch.countDown();
							}
						}
					});
			// 线程等待
			latch.await();
		} catch (Exception e) {
			logger.error("", e);
		}
		return zk;
	}

	private void watchNode(final ZooKeeper zk) {
		try {
			List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH,
					new Watcher() {
						@Override
						public void process(WatchedEvent event) {
							if (event.getType() == Event.EventType.NodeChildrenChanged) {
								// 客户端运行期间持续监听子节点变化事件（服务器的上下线），当子节点发生变化，重新调用watchNode方法生成新的nodelist
								watchNode(zk);
							}
						}
					});
			List<String> dataList = new ArrayList<>();
			for (String node : nodeList) {
				byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/"
						+ node, false, null);
				dataList.add(new String(bytes));
			}
			logger.debug("node data: " + dataList);
			this.dataList = dataList;
		} catch (KeeperException | InterruptedException e) {
			logger.error("", e);
		}
	}
}
