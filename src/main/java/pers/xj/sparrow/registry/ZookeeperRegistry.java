package pers.xj.sparrow.registry;

import pers.xj.sparrow.common.constant.RegistryConstant;
import pers.xj.sparrow.common.constant.URLConstant;
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

public class ZookeeperRegistry extends AbstractRegistry{

    private final ZookeeperClient zookeeperClient;

    public ZookeeperRegistry(URL url) {
        super(url);
        // 直接写死通过curator作为zookeeper的第三方客户端。
        zookeeperClient = new CuratorZookeeperClient(url);
    }

    @Override
    protected void doRegister(URL url) {
        zookeeperClient.create(toUrlPath(url), true);
        // TODO 监听 + 更新缓存
    }

    @Override
    protected void doUnRegister(URL url) {
        zookeeperClient.delete(toUrlPath(url));
        // TODO 监听 + 更新缓存
    }


    @Override
    public List<URL> doLookup(URL url) {
        // 简化：直接拿providers下的URL。dubbo是拿所有Type下（服务提供者 URL 、服务消费者 URL 、路由规则 URL 、配置规则 URL）的URL。
        List<String> providers = zookeeperClient.getChildren(toTypePath(url));
        // 将string转换成url
        List<URL> urls = providers.stream().map(provider -> URLUtil.assembleURL(URL.decode(provider))).collect(Collectors.toList());
        // TODO 监听 + 更新缓存
//        urls.forEach();
        return urls;
    }

    /**
     * Root层路径 -> 固定为：/sparrow
     * @return
     */
    private String toRootPath(){
        return URLConstant.DELIMITER + URLConstant.DEFAULT_PROTOCOL;
    }

    /**
     * Service层路径
     * @param url
     * @return
     */
    private String toServicePath(URL url){
        String interfaceName = url.getParam(URLConstant.INTERFACE_KEY, url.getPath());
        if (URLConstant.ANY_VALUE.equals(interfaceName)){
            return toRootPath();
        }
        return toRootPath() + URLConstant.DELIMITER + URL.encode(interfaceName);
    }

    /**
     * Type层路径
     * @param url
     * @return
     */
    private String toTypePath(URL url){
        return toServicePath(url) + URLConstant.DELIMITER + url.getParam(RegistryConstant.CATEGORY_KEY, RegistryConstant.PROVIDERS_CATEGORY);
    }

    /**
     * URL层路径
     * @param url
     * @return
     */
    private String toUrlPath(URL url){
        return toTypePath(url) + URLConstant.DELIMITER + URL.encode(URLUtil.parseToStr(url));
    }
}
