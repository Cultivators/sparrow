package pers.xj.sparrow.netty.learning.decoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xinjie.wu
 * @date 2022/2/24 9:34
 */
@Slf4j
public class StringProcessHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("打印：{}", msg.toString());
    }
}
