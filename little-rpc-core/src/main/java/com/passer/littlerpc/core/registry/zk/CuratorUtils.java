package com.passer.littlerpc.core.registry.zk;

import com.esotericsoftware.minlog.Log;
import com.passer.littlerpc.common.exception.ZkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtils {
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final int BLOCK_UNTIL_CONNECTED_TIME = 30;
    private static final String ZK_ROOT_PATH = "little-rpc";
    private static final Set<String> REGISTER_PATH_SET = ConcurrentHashMap.newKeySet();
    private static final Map<String, List<String>> REGISTER_SERVICE_MAP = new ConcurrentHashMap<>();


    private static CuratorFramework zkClient;

    private CuratorUtils(){}

    public static CuratorFramework getZkClient() {
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.newClient(DEFAULT_ZOOKEEPER_ADDRESS, retryPolicy);
        zkClient.start();

        try {
            boolean success = zkClient.blockUntilConnected(BLOCK_UNTIL_CONNECTED_TIME, TimeUnit.SECONDS);
            if (!success) {
                throw new ZkException("Connected to zk timeout!");
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return zkClient;
    }

    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTER_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node[{}] already exists!", path);
            } else {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node[{}] was created successfully", path);
            }
            REGISTER_PATH_SET.add(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createPersistentNode(String path) {
        createPersistentNode(getZkClient(), path);
    }

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String path) {
        if (REGISTER_SERVICE_MAP.containsKey(path)) {
            return REGISTER_SERVICE_MAP.get(path);
        }
        try {
            List<String> childrenNodes = zkClient.getChildren().forPath(path);
            REGISTER_SERVICE_MAP.put(path, childrenNodes);
            registerWatcher(zkClient, path);
            return childrenNodes;
        } catch (Exception e) {
            throw new ZkException(String.format("Get children nodes for path [%s] failed: %s", path, e.getMessage()));
        }
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress address) {
        log.info("will clear registry: {}", REGISTER_PATH_SET.toString());
        REGISTER_PATH_SET.stream().forEach(path -> {
            try {
                if (path.endsWith(address.toString())) {
                    zkClient.delete().forPath(path);
                }
            } catch (Exception e) {
                Log.error("clear registry for path [{}] failed", path);
            }
        });
    }

    private static void registerWatcher(CuratorFramework zkClient, String path) {
        CuratorCache cache = CuratorCache.build(zkClient, path);
        PathChildrenCacheListener pathChildrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework zkClient, PathChildrenCacheEvent event) throws Exception {
                List<String> childrenNodes = zkClient.getChildren().forPath(path);
                REGISTER_SERVICE_MAP.put(path, childrenNodes);
            }
        };
        CuratorCacheListener listener = CuratorCacheListener.builder()
                .forPathChildrenCache(path, zkClient, pathChildrenCacheListener).build();
        cache.listenable().addListener(listener);
        cache.start();
    }
}
