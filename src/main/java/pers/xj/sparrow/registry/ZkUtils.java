package pers.xj.sparrow.registry;

import pers.xj.sparrow.common.constant.RegistryConstant;
import pers.xj.sparrow.common.constant.URLConstant;
import pers.xj.sparrow.url.URL;
import pers.xj.sparrow.url.URLUtil;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/9 11:14
 */
public class ZkUtils {
    /**
     * Root层路径 -> 固定为：/sparrow
     * @return
     */
    public static String toRootPath(){
        return URLConstant.DELIMITER + URLConstant.DEFAULT_PROTOCOL;
    }

    /**
     * Service层路径
     * @param url
     * @return
     */
    public static String toServicePath(URL url){
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
    public static String toTypePath(URL url){
        return toServicePath(url) + URLConstant.DELIMITER + url.getParam(RegistryConstant.CATEGORY_KEY, RegistryConstant.PROVIDERS_CATEGORY);
    }

    /**
     * URL层路径
     * @param url
     * @return
     */
    public static String toUrlPath(URL url){
        return toTypePath(url) + URLConstant.DELIMITER + URL.encode(URLUtil.parseToStr(url));
    }
}
