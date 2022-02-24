package pers.xj.sparrow.netty.learning.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author xinjie.wu
 * @desc StringHeaderDecoder
 * @date 2022/2/24 9:24
 */
@Slf4j
public class StringHeaderDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        if (byteBuf.readableBytes() < 4){
            return;
        }
        // 头已经完整
        // 在真正开始从buffer读取数据之前，调用markReaderIndex()设置回滚点
        // 回滚点为 header的readIndex位置
        byteBuf.markReaderIndex();
        int len = byteBuf.readInt();
        //从buffer中读出头的大小，这会使得readIndex前移
        //剩余长度不够body体，reset 读指针
        if (byteBuf.readableBytes() < len){
            //读指针回滚到header的readIndex位置处，没进行状态的保存
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] bytes = new byte[len];
        byteBuf.readBytes(bytes, 0, len);
        list.add(new String(bytes, "UTF-8"));
    }

    public static void main(String[] args) {

        String content = "两岸猿声啼不住，轻舟已过万重山;";

        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new StringHeaderDecoder());
                channel.pipeline().addLast(new StringProcessHandler());
            }
        };

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(initializer);
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < 10; i++) {
            int random = new Random().nextInt(3);
            ByteBuf byteBuf = Unpooled.buffer();
            // 写入消息的长度
            byteBuf.writeInt(bytes.length * random);
            for (int k = 0; k < random; k++){
                byteBuf.writeBytes(bytes);
            }
            embeddedChannel.writeInbound(byteBuf);
        }

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
