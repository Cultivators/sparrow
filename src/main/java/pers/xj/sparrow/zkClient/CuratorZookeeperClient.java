package pers.xj.sparrow.zkClient;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import pers.xj.sparrow.url.URL;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xinjie.wu
 * @desc 现只支持curator,先不做其他zookeeper客户端的扩展
 * @date 2022/1/25 9:54
 */
@Slf4j
public class CuratorZookeeperClient extends AbstractZookeeperClient{

    private final CuratorFramework client;

    public CuratorZookeeperClient(URL url){
        super(url);
        int timeout = url.getIntParam(ZK_TIMEOUT_KEY, ZK_DEFAULT_CONNECTION_TIMEOUT);
        int sessionExpiredTime = url.getIntParam(ZK_SESSION_EXPIRED_KEY, ZK_DEFAULT_SESSION_TIMEOUT);
        client = CuratorFrameworkFactory.builder()
                .connectString(getConnectUrl())
                .retryPolicy(new RetryNTimes(1, 1000))
                .connectionTimeoutMs(timeout)
                .sessionTimeoutMs(sessionExpiredTime)
                .build();
        // 校验 TODO
        client.start();
        try{
            boolean connect = client.blockUntilConnected(timeout, TimeUnit.MILLISECONDS);
            log.info("zookeeper连接状态：{}", connect);
        }catch (InterruptedException e){
            log.error("zookeeper连接异常，原因：{}", e);
        }
    }

    @Override
    public void createEphemeralNode(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        }catch (KeeperException.NodeExistsException e){
            log.warn("ZNode：{} 已存在，会删掉重建。", path);
            deleteNode(path);
            createPersistentNode(path);
        }catch (Exception e){
            log.error("创建zookeeper临时节点异常，原因：{}", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void createPersistentNode(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
        }catch (KeeperException.NodeExistsException e){
            log.warn("ZNode：{} 已存在，{}", path, e);
        }catch (Exception e){
            log.error("创建zookeeper持久化节点异常，原因：{}", e);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteNode(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (KeeperException.NoNodeException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
