package pers.xj.sparrow.registry;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import pers.xj.sparrow.url.URL;

import java.util.List;
import java.util.Set;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/1/24 17:26
 */
@Slf4j
public abstract class AbstractRegistry implements Registry{

    private URL registerUrl;

    // 记录本机存活的服务
    private final Set<URL> availableServices = Sets.newConcurrentHashSet();

    public AbstractRegistry(URL url){
        // 对进出的url都进行createCopy保护，避免registry中的对象被修改，避免潜在的并发问题。
        this.registerUrl = url.createCopy();
    }

    @Override
    public void register(URL url) {
        if (null == url){
            log.warn("url对象为空");
            return;
        }
        doRegister(url);
        availableServices.add(url);
    }

    @Override
    public void unregister(URL url) {
        if (null == url){
            log.warn("url对象为空");
            return;
        }
        doUnRegister(url);
        availableServices.remove(url);
    }

    @Override
    public List<URL> lookup(URL url) {
        return doLookup(url);
    }

    public void doWatch(CuratorCacheListener.Type type, URL url){
         // 待扩展
    }

    protected abstract void doRegister(URL url);

    protected abstract void doUnRegister(URL url);

    protected abstract List<URL> doLookup(URL url);

}
