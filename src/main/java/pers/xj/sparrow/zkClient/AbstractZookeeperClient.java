package pers.xj.sparrow.zkClient;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import pers.xj.sparrow.url.URL;

import java.util.List;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/1/25 10:49
 */
public abstract class AbstractZookeeperClient implements ZookeeperClient{

    private static final String ZK_BACKUP_KEY = "backup";

    protected static final String ZK_TIMEOUT_KEY = "timeout";

    protected static final String ZK_SESSION_EXPIRED_KEY = "zk.session.expire";

    protected static final int ZK_DEFAULT_CONNECTION_TIMEOUT = 5 * 1000;

    protected static final int ZK_DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    private final URL url;

    AbstractZookeeperClient(URL url){
        this.url = url;
    }

    public void create(String path, boolean ephemeral){
        if (ephemeral){
            createEphemeralNode(path);
            return;
        }
        createPersistentNode(path);
    }

    public void delete(String path){
        deleteNode(path);
    }

    /**
     * 获取zookeeper client 连接地址。处理集群地址。
     * @return
     */
    public String getConnectUrl(){
        String standAloneUrl = url.getHost() + ":" + url.getPort();
        String backups = url.getParam(ZK_BACKUP_KEY, null);
        if (StringUtils.isNotBlank(backups)){
            List<String> backupList = Splitter.on(",").splitToList(backups);
            backupList.add(standAloneUrl);
            return Joiner.on(",").skipNulls().join(backupList);
        }
        return standAloneUrl;
    }

    protected abstract void createEphemeralNode(String path);

    protected abstract void createPersistentNode(String path);

    protected abstract void deleteNode(String path);

}
