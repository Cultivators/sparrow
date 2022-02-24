package pers.xj.sparrow.netty.learning.decoder;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/24 8:52
 */
@Slf4j
public class IntegerProcessHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Integer result = (Integer) msg;
        log.info("接收Byte2IntegerDecoder结果：{}", result);
    }
}
