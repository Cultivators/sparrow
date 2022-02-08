package pers.xj.sparrow.registry;

import pers.xj.sparrow.url.URL;

import java.util.List;


public interface Registry {

    void register(URL url);

    void unregister(URL url);

    List<URL> lookup(URL url);
}
