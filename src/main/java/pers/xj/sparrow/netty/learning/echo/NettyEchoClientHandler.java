package pers.xj.sparrow.netty.learning.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author xinjie.wu
 * @date 2022/2/22 16:52
 */

@Slf4j
@ChannelHandler.Sharable
public class NettyEchoClientHandler extends ChannelInboundHandlerAdapter {

    public static final NettyEchoClientHandler INSTANCE = new NettyEchoClientHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf)msg;
        int len = in.readableBytes();
        byte[] bytes = new byte[len];
        in.getBytes(0, bytes);
        log.info("client received: " + new String(bytes, StandardCharsets.UTF_8));
        in.release();
    }
}
