package pers.xj.sparrow.extension;

@SpiMeta(name = "spi1")
public class SpiHelloInterfaceImplV1 implements SpiHelloInterface{

    @Override
    public void sayHello() {
        System.out.println(this.getClass().getName() + "_say Hello");
    }
}
