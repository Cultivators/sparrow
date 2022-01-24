package pers.xj.sparrow.extension;


@SpiMeta(name = "spi2")
public class SpiHelloInterfaceImplV2 implements SpiHelloInterface{
    @Override
    public void sayHello() {
        System.out.println(this.getClass().getName() + "_say Hello");
    }
}
