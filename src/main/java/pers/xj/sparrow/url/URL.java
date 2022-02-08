package pers.xj.sparrow.url;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class URL implements Serializable {

    private String protocol;

    private String host;

    private Integer port;

    // interfaceName
    private String path;

    private Map<String, String> params;

    // 用于数字字段的转换
    private volatile transient Map<String, Number> numbers;

    public URL(String protocol, String host, Integer port, String path, Map<String, String> params){
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.params = params;
    }

    public URL(String protocol, String host, Integer port, String path){
        this(protocol, host, port, path, Maps.newHashMap());
    }

    public URL createCopy(){
        Map<String, String> params = new HashMap<>();
        if (this.params != null) {
            params.putAll(this.params);
        }

        return new URL(protocol, host, port, path, params);
    }

    public static String encode(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String decode(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    // --------------只扩展了String、Integer、Long 常用类型的转换-----------------------

    public String getParam(String name, String defaultVal){
        String value = params.get(name);
        if (StringUtils.isBlank(value)){
            return defaultVal;
        }
        return value;
    }

    public void addParam(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        params.putIfAbsent(name, value);
    }

    public void removeParam(String name) {
        if (name != null) {
            params.remove(name);
        }
    }

    public void addParam(Map<String, String> params) {
        params.putAll(params);
    }

    public Boolean getBooleanParam(String name, boolean defaultVal) {
        String value = getParam(name, null);
        if (StringUtils.isBlank(value)) {
            return defaultVal;
        }
        return Boolean.parseBoolean(value);
    }

    public Integer getIntParam(String name, int defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.intValue();
        }
        String value = params.get(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(name, i);
        return i;
    }

    public Long getLongParam(String name, long defaultValue) {
        Number n = getNumbers().get(name);
        if (n != null) {
            return n.longValue();
        }
        String value = params.get(name);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(name, l);
        return l;
    }

}
