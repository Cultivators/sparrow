package pers.xj.sparrow.registry;

import pers.xj.sparrow.extension.Spi;
import pers.xj.sparrow.url.URL;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/1/24 17:07
 */

@Spi
public interface RegistryFactory {

    Registry getRegister(URL url);
}
