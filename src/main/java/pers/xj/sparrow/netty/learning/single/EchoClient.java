package pers.xj.sparrow.netty.learning.single;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;


/**
 * @author xinjie.wu
 * @desc echo 客户端
 * @date 2022/2/19 18:11
 */

@Slf4j
public class EchoClient {

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8089);
        // 获取通道
        SocketChannel channel = SocketChannel.open(address);
        // 设置为非阻塞模式
        channel.configureBlocking(false);
        while (!channel.finishConnect()){}
        log.info("客户端连接成功");
        Processor processor = new Processor(channel);
        new Thread(processor).start();

    }

    static class Processor implements Runnable{

        final SocketChannel channel;
        final Selector selector;

        Processor(SocketChannel channel) throws IOException{
            this.channel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

        @Override
        public void run() {
            try{
                while (!Thread.interrupted()){
                    // select()方法会返回读事件已经就绪的那些通道
                    int selectedNum = selector.select();
                    log.info("selector.select->int : {}", selectedNum);
                    // 一旦调用了select()方法，并且返回值表明有一个或更多个通道就绪了，然后可以通过调用selector的selectedKeys()方法，访问“已选择键集
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        if (key.isWritable()){

                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            Scanner scanner = new Scanner(System.in);
                            System.out.println("请输入内容：");
                            if (scanner.hasNext()){
                                String text = scanner.next();
                                SocketChannel socketChannel = (SocketChannel) key.channel();
                                byteBuffer.put((LocalDateTime.now() + ">>>" + text).getBytes());
                                byteBuffer.flip();
                                socketChannel.write(byteBuffer);
                                byteBuffer.clear();
                            }

                        }
                        if (key.isReadable()){
                            // 若选择键的IO事件是“可读”事件,读取数据
                            SocketChannel socketChannel = (SocketChannel) key.channel();

                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            int len = -1;
                            while((len = socketChannel.read(byteBuffer)) > 0){
                                byteBuffer.flip();
                                log.info("server echo:>>>{}", new String(byteBuffer.array(), 0, len));
                                byteBuffer.clear();
                            }

                        }

                    }

                    keys.clear();

                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws IOException {
        new EchoClient().start();
    }

}
