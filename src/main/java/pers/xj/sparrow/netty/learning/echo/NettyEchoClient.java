package pers.xj.sparrow.netty.learning.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * @author xinjie.wu
 * @date 2022/2/22 16:30
 */
@Slf4j
public class NettyEchoClient {

    Bootstrap b = new Bootstrap();

    private final int port;

    private final String ip;

    NettyEchoClient(int port, String ip){
        this.port = port;
        this.ip = ip;
    }

    public void runClient(){
        // 创建reactor线程组
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {

            // 1、设置reactor 线程组
            b.group(workerLoopGroup);

            // 2、设置nio类型的channel
            b.channel(NioSocketChannel.class);

            // 3、设置监听端口
            b.remoteAddress(ip, port);

            // 4、设置通道的参数
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            // 5、装配子通道流水线
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(NettyEchoClientHandler.INSTANCE);
                }
            });

            ChannelFuture f = b.connect();
            f.addListener((ChannelFuture cf) -> {
                if (cf.isSuccess()) {
                    log.info("EchoClient客户端连接成功！");
                } else {
                    log.info("EchoClient客户端连接失败！");
                }
            });

            // 阻塞，知道连接完成
            f.sync();
            Channel channel = f.channel();

            Scanner scanner = new Scanner(System.in);
            log.info("请输入发送内容：");
            while (scanner.hasNext()){
                String text = scanner.next();
                byte[] bytes = (LocalDateTime.now() + ">>>" + text).getBytes(StandardCharsets.UTF_8);
                // 发送ByteBuf
                ByteBuf byteBuf = channel.alloc().buffer();
                byteBuf.writeBytes(bytes);
                channel.writeAndFlush(byteBuf);
                log.info("请输入发送内容：");
            }

        }catch (Exception e){
            log.error("EchoClient error：{}", e);
        }finally {
            workerLoopGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new NettyEchoClient(10086, "127.0.0.1").runClient();
    }
}
