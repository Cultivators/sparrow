package pers.xj.sparrow.extension;

import org.junit.jupiter.api.Test;

public class ExtensionLoaderTest {


    @Test
    public void testExtensionLoader() throws Exception {

        SpiHelloInterface spi1 = ExtensionLoader.getExtensionLoader(SpiHelloInterface.class).getExtension("spi1");
        spi1.sayHello();

        SpiHelloInterface spi2 = ExtensionLoader.getExtensionLoader(SpiHelloInterface.class).getExtension("spi2");
        spi2.sayHello();

    }
}
