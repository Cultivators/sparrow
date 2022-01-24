package pers.xj.sparrow.url;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
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

}
