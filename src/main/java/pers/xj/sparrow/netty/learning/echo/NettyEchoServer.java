package pers.xj.sparrow.netty.learning.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xinjie.wu
 * @date 2022/2/22 14:15
 *
 *  通道 -> ChannelPipeline -> 多个handler
 */
@Slf4j
public class NettyEchoServer {

    ServerBootstrap b = new ServerBootstrap();

    private final int port;

    NettyEchoServer(int port){
        this.port = port;
    }

    public void runServer(){

        // 创建reactor线程组
        // 负责处理连接监听IO事件
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        // 负责数据IO事件和Handler业务处理
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            // 1、设置reactor线程组
            b.group(bossLoopGroup, workerLoopGroup);

            // 2、设置nio类型得channel
            b.channel(NioServerSocketChannel.class);

            // 3、设置监听端口
            b.localAddress(port);

            // 4、设置通道参数
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            // 5、装配子通道流水线(在父通道成功接收一个连接，并创建成功一个子通道后，就会初始化子通道，这里配置的ChannelInitializer实例就会被调用)
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                // 有连接到达时会创建一个channel
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // pipeline管理子通道channel中的Handler
                    // 向子channel流水线添加一个handler处理器
                    socketChannel.pipeline().addLast(NettyEchoServerHandler.INSTANCE);
                }
            });

            // 6、开始绑定server，通过调用sync同步方法阻塞直到绑定成功
            ChannelFuture channelFuture = b.bind().sync();
            log.info("服务启动成功，监听端口：{}", channelFuture.channel().localAddress());

            // 7、等待通道关闭的异步任务结束，服务监听通道会一直等待通道关闭的异步任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        }catch (Exception e){
            log.error("启动服务端异常，原因：{}", e);
        }finally {
            // 8、优雅关闭EventLoopGroup，释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        new NettyEchoServer(10086).runServer();
    }

}
