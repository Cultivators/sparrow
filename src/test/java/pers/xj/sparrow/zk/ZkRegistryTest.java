package pers.xj.sparrow.zk;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import pers.xj.sparrow.extension.ExtensionLoader;
import pers.xj.sparrow.registry.Registry;
import pers.xj.sparrow.registry.RegistryFactory;
import pers.xj.sparrow.registry.ZookeeperRegistryFactory;
import pers.xj.sparrow.url.URL;
import pers.xj.sparrow.url.URLUtil;

import java.io.IOException;
import java.util.List;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/9 11:41
 */
public class ZkRegistryTest {

    private Registry registry;

    private TestingServer zkServer;

    private final URL registerUrl = URLUtil.assembleURL("zk://127.0.0.1:2181/pers.xj.sparrow.zk.ZkRegistryTest?version=v1");

    @Before
    public void init() throws Exception {
        zkServer = new TestingServer(8989, true);
        zkServer.start();
        URL zkUrl = URLUtil.assembleURL("zk://127.0.0.1:8989");

        RegistryFactory registryFactory = ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension("zookeeper");
        registry = registryFactory.getRegister(zkUrl);
    }

    @After
    public void stop() throws IOException {
        zkServer.stop();
    }

    @Test
    public void testRegister(){
        registry.register(registerUrl);
        List<URL> urls = registry.lookup(registerUrl);
        for (URL url : urls){
            System.out.println("url>>>>>>>>>>>>" + url.toString());
        }
    }

}
