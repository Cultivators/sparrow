package pers.xj.sparrow.netty.learning.single;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 * @author xinjie.wu
 * @desc 反应器(单线程)
 * @date 2022/2/19 15:32
 */
@Slf4j
public class EchoServerReactor implements Runnable{

    Selector selector;
    ServerSocketChannel serverSocket;

    EchoServerReactor() throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();

        InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8089);
        serverSocket.socket().bind(address);
        // 非阻塞
        serverSocket.configureBlocking(false);

        SelectionKey key = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        // acceptor执行器的初始化是阻塞的
        key.attach(new AcceptorHandler());
    }

    @Override
    public void run() {
        try{
            while (!Thread.interrupted()){
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()){
                    dispatch(iterator.next());
                }
                keys.clear();

            }
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    void dispatch(SelectionKey key){
        Runnable handler = (Runnable) key.attachment();
        // 调用之前attach绑定到选择键的handler处理器对象
        if (Objects.nonNull(handler)){
            handler.run();
        }

    }


    // 新连接处理器
    class AcceptorHandler implements Runnable{

        @Override
        public void run() {
            try {
                // 1、接收新连接
                SocketChannel channel = serverSocket.accept();
                if (Objects.nonNull(channel)){
                    //2、需要为新连接创建一个输入输出的handle处理器
                    new EchoHandler(channel, selector);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) throws IOException{
        new Thread(new EchoServerReactor()).start();
    }
}
