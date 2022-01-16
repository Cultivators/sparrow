package pers.xj.sparrow.extension;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpiMeta {
    /**
     * 扩展实现类的检索值
     * @return
     */
    String name() default "";
}
