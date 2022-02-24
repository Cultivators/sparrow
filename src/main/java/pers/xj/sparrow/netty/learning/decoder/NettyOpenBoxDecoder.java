package pers.xj.sparrow.netty.learning.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/24 17:54
 */

@Slf4j
public class NettyOpenBoxDecoder {

    static String spliter = "\r\n";
    static String spliter2 = "\t";
    static String content = "两岸猿声啼不住，轻舟已过万重山!";

    public static void main(String[] args) throws Exception {
        // 行分割数据包解码器
        testLineBasedFrameDecoder();

        // 自定义长度数据包解码器
//        testLengthFieldBasedFrameDecoder();
    }

    /**
     *  int maxFrameLength - 发送的数据包的最大长度
     *  int lengthFieldOffset - 长度字段偏移量
     *  int lengthFieldLength - 长度字段自己占用的字节数
     *  int lengthAdjustment - 长度字段的偏移量矫正 lengthAdjustment就是夹在内容字段和长度字段中的部分
     *  int initialBytesToStrip - 丢弃的起始字节数
     */
    private static void testLengthFieldBasedFrameDecoder() throws Exception{
        final LengthFieldBasedFrameDecoder spliter = new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4);
        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(spliter);
                channel.pipeline().addLast(new StringDecoder());
                channel.pipeline().addLast(new StringProcessHandler());
            }
        };

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(initializer);
        for (int i = 0; i < 10; i++) {
            int random = new Random().nextInt(3);
            //1-3之间的随机数
            ByteBuf buf = Unpooled.buffer();
            byte[] bytes = content.getBytes("UTF-8");
            buf.writeInt(bytes.length * random);
            for (int k = 0; k < random; k++) {
                buf.writeBytes(bytes);
            }
            embeddedChannel.writeInbound(buf);
        }


    }

    private static void testLineBasedFrameDecoder() throws UnsupportedEncodingException {
        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                channel.pipeline().addLast(new StringDecoder());
                channel.pipeline().addLast(new StringProcessHandler());
            }
        };

        EmbeddedChannel embeddedChannel = new EmbeddedChannel(initializer);
        for (int i = 0; i < 10; i++) {
            int random = new Random().nextInt(3);
            ByteBuf buf = Unpooled.buffer();
            for (int k = 0; k < random; k++) {
                buf.writeBytes(content.getBytes("UTF-8"));
            }
            buf.writeBytes(spliter.getBytes("UTF-8"));
            embeddedChannel.writeInbound(buf);
        }

    }
}
