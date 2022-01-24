package pers.xj.sparrow.url;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import pers.xj.sparrow.common.constant.URLConstant;
import pers.xj.sparrow.exception.SparrowException;

import java.util.Map;
import java.util.Objects;

/**
 * @author xinjie.wu
 * @desc URL工具类
 * URL 格式：sparrow://host:port/interfaceName?param1=value1&param2=value2
 * dubbo用的工具类是Hutool，会更适合开源项目，毕竟坚持大部分无依赖这一点就值得力挺。
 * @date 2022/1/24 8:54
 */
@Slf4j
public class URLUtil {

    /**
     * 将URL转换为字符串拼接
     * @param url
     * @return
     */
    public static String parseToStr(URL url){

        if (Objects.isNull(url)){
            return null;
        }

        StringBuilder fullStr = new StringBuilder();
        if (StringUtils.isNotBlank(url.getProtocol())){
            if (url.getProtocol().equals("file")){
                fullStr.append("file:///").append(url.getPath());
                return fullStr.toString();
            }
            fullStr.append(url.getProtocol());
            fullStr.append("://");
        }

        if (StringUtils.isNotBlank(url.getHost())){
            fullStr.append(url.getHost());
        }

        if (null != url.getPort()){
            fullStr.append(":").append(url.getPort());
        }

        if (StringUtils.isNotBlank(url.getPath())){
            fullStr.append(URLConstant.DELIMITER).append(url.getPath());
        }

        if (!url.getParams().isEmpty()){
            fullStr.append("?").append(Joiner.on("&").skipNulls().withKeyValueSeparator("=").join(url.getParams()));
        }

        return fullStr.toString();
    }

    /**
     * 组装URL
     * 格式1：sparrow://host:port/interfaceName?param1=value1&param2=value2
     * 格式2：file:///D:/path1/path2/index.html
     * @param urlStr
     * @return
     */
    public static URL assembleURL(String urlStr){
        if (StringUtils.isBlank(urlStr)){
            throw new SparrowException("url不能为空");
        }

        if (urlStr.contains("file:///")){
            return assembleLocalFileProtocol(urlStr);
        }
        Map<String, String> params = null;
        String protocol;
        String host = null;
        Integer port = null;
        String path = null;
        // 协议处理
        int protocolIndex = urlStr.indexOf("://");
        if (protocolIndex > 0){
            protocol = urlStr.substring(0, protocolIndex);
            urlStr = urlStr.substring(protocolIndex + 3);
        }else {
            throw new SparrowException("URL格式不规范，无法解析协议头，urlStr：" + urlStr);
        }

        // 参数处理
        int paramIndex = urlStr.indexOf('?');
        if (paramIndex > 0){
            String param = urlStr.substring(paramIndex + 1);
            params = Splitter.on("&").trimResults().withKeyValueSeparator("=").split(param);
            // 截取后半段
            urlStr = urlStr.substring(0, paramIndex);
        }

        // 处理path
        int pathIndex = urlStr.indexOf("/");
        if (pathIndex > 0){
            path = urlStr.substring(pathIndex + 1);
            urlStr = urlStr.substring(0, pathIndex);
        }

        //host:port/interfaceName
        // 处理host
        int hostIndex = urlStr.lastIndexOf(":");
        if (hostIndex > 0){
            if (urlStr.contains("%")) {
                // ipv6 address with scope id
                // e.g. fe80:0:0:0:894:aeec:f37d:23e1%en0
                // see https://howdoesinternetwork.com/2013/ipv6-zone-id
                // ignore
            } else {
                host = urlStr.substring(0, hostIndex);
                urlStr = urlStr.substring(hostIndex + 1);
            }
        }

        // 处理port
        if (StringUtils.isNotBlank(urlStr)){
            port = Integer.parseInt(urlStr);
        }

        return new URL(protocol, host, port, path, params);
    }

    /**
     * 处理本地文件协议
     * @param urlStr
     * @return
     */
    private static URL assembleLocalFileProtocol(String urlStr){
        int protocolIndex = urlStr.indexOf("file:///");
        String path = urlStr.substring(protocolIndex + 1);
        return new URL("file", null, null, path, null);
    }

    public static void main(String[] args) {
        String path = "sparrow://127.0.0.1:8080/interfaceName?param1=value1&param2=value2";
        System.out.println(assembleURL(path));

        Map<String, String> params = Maps.newHashMap();
        params.put("param1", "val1");
        params.put("param2", "val2");
        params.put("param3", "val3");

        URL url = new URL("sparrow", "127.0.0.1", 8090, "pers.xj.service.UserServiceImp", params);
        System.out.println(url);
    }

}
