package pers.xj.sparrow.registry;

import pers.xj.sparrow.extension.SpiMeta;
import pers.xj.sparrow.url.URL;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/1/24 17:08
 */
@SpiMeta(name = "zookeeper")
public class ZookeeperRegistryFactory implements RegistryFactory{

    @Override
    public Registry getRegister(URL url) {
        // TODO 缓存
        return new ZookeeperRegistry(url);
    }
}
