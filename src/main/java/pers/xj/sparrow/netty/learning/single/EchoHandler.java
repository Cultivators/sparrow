package pers.xj.sparrow.netty.learning.single;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author xinjie.wu
 * @desc IOHanlder 处理输入输出的处理器
 * @date 2022/2/19 17:21
 */
@Slf4j
public class EchoHandler implements Runnable{

    final SocketChannel channel;
    final SelectionKey key;
    final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    static final int RECIEVING = 0, SENDING = 1;
    volatile int state = RECIEVING;

    EchoHandler(SocketChannel channel, Selector selector) throws Exception {
        this.channel = channel;
        channel.configureBlocking(false);
        // 仅仅取得选择键，后设置感兴趣的IO事件
        key = channel.register(selector, 0);
        // 将handler作为选择键的附件
        key.attach(this);

        // key关联通道,给key注册就相当于给通道注册。给对应的通道注册读事件。
        key.interestOps(SelectionKey.OP_READ);

        // 唤醒阻塞在selector.select上的线程，让该线程及时去处理其他事情.
        selector.wakeup();
    }

    @Override
    public void run(){
        try{

            if (state == SENDING){
                // 写入通道
                channel.write(byteBuffer);
                // 写完之后切换为写模式
                byteBuffer.clear();
                key.interestOps(SelectionKey.OP_READ);

                state = RECIEVING;

            }else if (state == RECIEVING){
                // 从通道读
                int length = -1;
                while ((length = channel.read(byteBuffer)) > 0){
                    log.info("从通道读取的数据：{}", new String(byteBuffer.array(), 0, length));
                }
                // 读完之后，准备开始写入通道,byteBuffer切换成读模式
                byteBuffer.flip();
                // 读完后，注册write就绪事件
                key.interestOps(SelectionKey.OP_WRITE);

                state = SENDING;

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
