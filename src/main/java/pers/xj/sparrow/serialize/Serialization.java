package pers.xj.sparrow.serialize;

import pers.xj.sparrow.extension.Spi;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/9 15:20
 */

@Spi
public interface Serialization {

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> clz);
}
