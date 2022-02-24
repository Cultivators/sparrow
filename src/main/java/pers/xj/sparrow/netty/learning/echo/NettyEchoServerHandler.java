package pers.xj.sparrow.netty.learning.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xinjie.wu
 * @date 2022/2/22 16:15
 */
@Slf4j
@ChannelHandler.Sharable
public class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {

    public static final  NettyEchoServerHandler INSTANCE = new NettyEchoServerHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        log.info("msg type：{}", in.hasArray() ? "堆内存" : "直接内存");
        // 读取数据
        int len = in.readableBytes();
        byte[] bytes = new byte[len];
        in.getBytes(0, bytes);
        log.info("server received: {}", new String(bytes, "UTF-8"));
        // 写回数据
        log.info("写回前，msg.refCnt:{}", ((ByteBuf)msg).refCnt());
        ChannelFuture writeFuture = ctx.writeAndFlush(msg);
        writeFuture.addListener((ChannelFuture f) -> {
            log.info("写回后，msg.refCnt:{}", ((ByteBuf)msg).refCnt());
        });
//        super.channelRead(ctx, msg);
    }
}
