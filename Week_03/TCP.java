package com.citiccard.core.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCP {
    ExecutorService executorService = Executors.newFixedThreadPool(64);
    // 通道管理器
    private Selector selector;

    private static long start = 1611384801559L;

    private static final String  br = "\r\n";
 
    public void initServer(int port) throws Exception {
        // 获得一个ServerSocket通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置通道为 非阻塞
        serverSocketChannel.configureBlocking(false);
        // 将该通道对应的serverSocket绑定到port端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 获得通道管理器
        this.selector = Selector.open();
 
        // 将通道管理器和该通道绑定，并为该通道注册selectionKey.OP_ACCEPT事件
        // 注册该事件后，当事件到达的时候，selector.select()会返回，
        // 如果事件没有到达selector.select()会一直阻塞（将fd注册到selector上）
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
 
    // 采用轮训的方式监听selector上是否有需要处理的事件，如果有，进行处理
    public void listen() throws Exception {
        System.out.println("start server");
        // 轮询访问selector
        while (true) {
            // 当注册事件到达时，方法返回，否则该方法会一直阻塞
            int n = selector.select();//调用select方法，将fd集合从用户空间拷贝到内核空间，内核遍历fd，只要有一个fd数据准备好了就用户进程（线程）就会从阻塞状态唤醒
            // 获得selector中选中的相的迭代器，选中的相为注册的事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                // 删除已选的key 以防重负处理
                ite.remove();
                //只有ServerSocketChannel关联的Key才会有，说明有一个新的客户端连接请求到来
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 获得和客户端连接的通道
                    SocketChannel channel = server.accept();
                    // 设置成非阻塞
                    channel.configureBlocking(false);
                    // 在这里可以发送消息给客户端
//                    channel.write(ByteBuffer.wrap(new String("hello I am Server").getBytes()));
                    // 在客户端 连接成功之后，为了可以接收到客户端的信息，需要给通道设置读的权限
                    channel.register(this.selector, SelectionKey.OP_READ);
                    // 获得了可读的事件
                    System.out.println("新创建的连接");
 
                } else if (key.isReadable()) {//说明时普通的SocketChannel读就绪
                    read(key);
                }
 
            }
        }
    }
 
    // 处理 读取客户端发来的信息事件
    private void read(SelectionKey key) throws Exception {
        // 服务器可读消息，得到事件发生的socket通道
        SocketChannel channel = (SocketChannel) key.channel();
        // 穿件读取的缓冲区
//        read(channel);
        Thread.sleep(20);
        StringBuilder sb = new StringBuilder("HTTP/1.1 200 OK").append(br);
        sb.append("Content-Type:text/html;charset=UTF-8").append(br);
        String body = "hello, nio";
        sb.append("Content-Length:" + body.getBytes().length).append(br);
        sb.append(br);
        sb.append(body);
        System.out.println("响应客户端");
        write(channel,sb.toString());
    }

    public static void read(SocketChannel socketChannel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(512);//read into buffer.
        int bytesRead = socketChannel.read(buf);
        while(bytesRead != -1) {
            buf.flip();//make buffer ready for read
            while(buf.hasRemaining()) {
                System.out.print((char)buf.get());
            }
            buf.clear();//make buffer ready for writing
            bytesRead = socketChannel.read(buf);
        }
    }

    public static void write(SocketChannel socketChannel,  String content) throws IOException {
        ByteBuffer outBuf = ByteBuffer.wrap(content.getBytes());
        while(outBuf.hasRemaining()) {
            socketChannel.write(outBuf);
        }
        socketChannel.close();
    }
 
    public static void main(String[] args) throws Throwable {
        TCP server = new TCP();
        server.initServer(8989);
        server.listen();
    }
}

