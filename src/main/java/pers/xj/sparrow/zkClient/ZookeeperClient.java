package pers.xj.sparrow.zkClient;

import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

import java.util.List;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/7 22:24
 */
public interface ZookeeperClient {

    void create(String path, boolean ephemeral);

    void delete(String path);

    List<String> getChildren(String path);

    void addListener(String path, CuratorCacheListener listener);

    void removeListener(String path, CuratorCacheListener listener);

}
