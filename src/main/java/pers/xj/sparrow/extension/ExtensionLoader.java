package pers.xj.sparrow.extension;

import lombok.extern.slf4j.Slf4j;
import pers.xj.sparrow.exception.SparrowException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于motan的简版扩展类加载器
 */
@Slf4j
public class ExtensionLoader<T> {

    private static ConcurrentHashMap<Class<?>, ExtensionLoader<?>> extensionLoaderConcurrentHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Class<T>> extensionClass = new ConcurrentHashMap<>();
    // 初始化标识
    private volatile boolean init = false;
    // 配置文件加载路径前缀
    private static final String PREFIX = "META-INFO/service/";

    private Class<T> clz;

    private ClassLoader classLoader;

    private ExtensionLoader(Class<T> clz) {
        this(clz, Thread.currentThread().getContextClassLoader());
    }

    private ExtensionLoader(Class<T> clz, ClassLoader classLoader){
        this.clz = clz;
        this.classLoader = classLoader;
    }

    /**
     * 获取扩展类加载器
     * @param clz
     * @param <T>
     * @return
     */
    public <T> ExtensionLoader<T> getExtensionLoader(Class<T> clz){

        checkInterfaceType(clz);

        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) extensionLoaderConcurrentHashMap.get(clz);
        if (Objects.isNull(extensionLoader)){
            extensionLoader = initExtensionLoader(clz);
        }
        return extensionLoader;
    }

    /**
     * 双层校验 ，防并发
     * @param clz
     * @param <T>
     * @return
     */
    public static synchronized <T> ExtensionLoader<T> initExtensionLoader(Class<T> clz){
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) extensionLoaderConcurrentHashMap.get(clz);
        if (Objects.isNull(extensionLoader)){
            extensionLoader = new ExtensionLoader(clz);
            extensionLoaderConcurrentHashMap.putIfAbsent(clz, extensionLoader);

            extensionLoader = (ExtensionLoader<T>) extensionLoaderConcurrentHashMap.get(clz);
        }

        return extensionLoader;
    }

    /**
     * 懒加载：只有第一次去获取实例的时候才去加载所有的扩展实例
     * @param key
     * @return
     */
    public Class<T> getExtensionClass(String key){

        checkInit(key);

        return extensionClass.get(key);
    }

    public T getExtension(String key) throws Exception {
        Class<T> clz = getExtensionClass(key);
        if (Objects.isNull(clz)){
            throw new SparrowException("找不到扩展点为：" + key + "的实例");
        }
        return clz.newInstance();
    }

    private void checkInit(String key){
        if (!init){
            loadExtensionClasses();
        }
    }

    private synchronized void loadExtensionClasses(){
        if (init){
            return;
        }
        // 根据特定路径加载所有扩展实例
        extensionClass = loadExtensionClasses(PREFIX);

        init = true;
    }

    private ConcurrentHashMap<String, Class<T>> loadExtensionClasses(String prefix){
        String fullName = prefix + clz.getName();
        // class.getResource("/") == class.getClassLoader().getResource("")
        Set<String> classNames = new HashSet<>();
        try {
            Enumeration<URL> urls;

            if (Objects.isNull(classLoader)) {
                urls = ClassLoader.getSystemResources(fullName);
            } else {
                urls = classLoader.getResources(fullName);
            }

            if (null == urls || !urls.hasMoreElements()){
                return new ConcurrentHashMap<>();
            }

            while (urls.hasMoreElements()){
                URL url = urls.nextElement();
                parseUrl(url, classNames);
            }

        }catch (Exception e){
            log.error("执行ExtensionLoader#loadExtensionClasses(java.lang.String)异常，prefix:{}, clz:{}，原因：{}", prefix, clz.getName(), e);
        }

        return loadClass(classNames);
    }

    private void parseUrl(URL url, Set<String> classNames){

        try(
                InputStream inputStream = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8.name()))){
            String line;
            while ((line = reader.readLine()) != null){
                parseLine(line, classNames);
            }

        }catch (Exception e){
            log.error("读取@Spi配置文件异常，原因：{}", e);
        }
    }

    private void parseLine(String line, Set<String> classNames){

        line = line.trim();
        if (line.startsWith("#") || line.length() <= 0){
            return;
        }

        classNames.add(line);
    }

    private ConcurrentHashMap<String, Class<T>> loadClass(Set<String> classNames){
        ConcurrentHashMap<String, Class<T>> className2ClassMap = new ConcurrentHashMap<>();
        for (String className : classNames){

            try{
                Class<T> type;
                if (Objects.isNull(classLoader)){
                    type = (Class<T>) Class.forName(className);
                }else {
                    type = (Class<T>) Class.forName(className, true, classLoader);
                }
                // 1、校验当前类为public
                if (!Modifier.isPublic(type.getModifiers())){
//                    log.error("当前类：{}，非public类", className);
                    // TODO 后续对SparrowException进行细节封装
                    throw new SparrowException("当前类：" + className + "，非public类");
                }

                // 2、校验当前类必须有public的无参构造器
                Constructor<?>[] constructors = type.getConstructors();
                if (null == constructors || constructors.length == 0){
//                    log.error("当前类：{}，不存在public的无参构造器", className);
                    throw new SparrowException("当前类：" + className + "，不存在public的无参构造器");
                }

                Optional<Constructor<?>> optional = Arrays.stream(constructors)
                        .filter(constructor -> Modifier.isPublic(constructor.getModifiers()) && constructor.getParameterTypes().length == 0)
                        .findAny();

                if (!optional.isPresent()){
//                    log.error("当前类：{}，不存在public的无参构造器", className);
                    throw new SparrowException("当前类：" + className + "，不存在public的无参构造器");
                }

                // 3、校验从配置文件中拿到的类跟扩展类加载器传进来的类是否存在父子类关系
                if (!clz.isAssignableFrom(type)){
//                    log.error("当前类：{}，不是{}，的实例", className, clz.getName());
                    throw new SparrowException("当前类：" + className +"，不是" + clz.getName() +"，的实例");
                }

                SpiMeta spiMeta = type.getAnnotation(SpiMeta.class);
                // 如果扩展类有SpiMeta的注解，那么获取对应的name，如果没有的话获取classname
                String spiMetaName = Objects.isNull(spiMeta) ? type.getSimpleName() : spiMeta.name();
                if (className2ClassMap.contains(spiMetaName)){
                    throw new SparrowException("spiMetaName：" + spiMetaName + "，已经存在");
                }

                className2ClassMap.put(spiMetaName, type);

            }catch (Exception e){
                log.error("加载Spi类失败，原因：{}", e);
            }

        }

        return className2ClassMap;
    }


    private static <T> void checkInterfaceType(Class<T> clz) {
        if (null == clz){
            throw new SparrowException("clz为空");
        }
        if (!clz.isInterface()){
            throw new SparrowException("clz非接口类型");
        }
        if (!clz.isAnnotationPresent(Spi.class)){
            throw new SparrowException("clz不包含@Spi注解");
        }
    }
}
