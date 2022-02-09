package pers.xj.sparrow.registry;

import lombok.extern.slf4j.Slf4j;
import pers.xj.sparrow.url.URL;
import pers.xj.sparrow.url.URLUtil;
import pers.xj.sparrow.zkClient.CuratorZookeeperClient;
import pers.xj.sparrow.zkClient.ZookeeperClient;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xinjie.wu
 * @desc 基于zookeeper的注册中心 (curator)
 * @date 2022/1/24 16:19
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry{

    private final ZookeeperClient zookeeperClient;

    public ZookeeperRegistry(URL url) {
        super(url);
        // 直接写死通过curator作为zookeeper的第三方客户端。
        zookeeperClient = new CuratorZookeeperClient(url);
    }

    @Override
    protected void doRegister(URL url) {
        zookeeperClient.create(ZkUtils.toUrlPath(url), true);
        // TODO 这里耦合了具体的第三方框架curator，其实是不方便扩展的。看对应源码怎么解决的
        zookeeperClient.addListener(ZkUtils.toServicePath(url), (type, oldData, newData) -> {
            log.info("doRegister ->>> type:{}, oldData:{}, newData:{} ", type, oldData, newData);
            doWatch(type, url);
        });
    }

    @Override
    protected void doUnRegister(URL url) {
        zookeeperClient.delete(ZkUtils.toUrlPath(url));
        // TODO 这里耦合了具体的第三方框架curator，其实是不方便扩展的。看对应源码怎么解决的
        zookeeperClient.removeListener(ZkUtils.toServicePath(url), (type, oldData, newData) -> {
            log.info("doUnRegister ->>> type:{}, oldData:{}, newData:{} ", type, oldData, newData);
            doWatch(type, url);
        });
    }


    @Override
    public List<URL> doLookup(URL url) {
        // 简化：直接拿providers下的URL。dubbo是拿所有Type下（服务提供者 URL 、服务消费者 URL 、路由规则 URL 、配置规则 URL）的URL。
        List<String> providers = zookeeperClient.getChildren(ZkUtils.toTypePath(url));
        // 将string转换成url
        List<URL> urls = providers.stream().map(provider -> URLUtil.assembleURL(URL.decode(provider))).collect(Collectors.toList());
        return urls;
    }


}
