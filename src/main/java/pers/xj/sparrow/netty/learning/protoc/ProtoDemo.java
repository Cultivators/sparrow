package pers.xj.sparrow.netty.learning.protoc;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xinjie.wu
 * @desc TODO
 * @date 2022/2/23 21:28
 */

@Slf4j
public class ProtoDemo {


    public static void main(String[] args) throws InvalidProtocolBufferException {
        MsgProto.Msg.Builder builder = MsgProto.Msg.newBuilder();
        builder.setId(2);
        builder.setName("科比布莱尔");
        MsgProto.Msg msg = builder.build();

        byte[] bytes = msg.toByteArray();
        msg = MsgProto.Msg.parseFrom(bytes);

        log.info("id:{}, name:{}", msg.getId(), msg.getName());

    }

}
