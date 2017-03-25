package nio.handler;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yangchinqi on 3/21/17.
 */
public class ServerHandler extends Thread {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public ServerHandler(int port) throws Throwable{
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(port);
        serverSocketChannel.socket().bind(address);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void run(){
        while(true) {
            Set<SelectionKey> selectedKeys;
            try {
                selector.select();
                selectedKeys = selector.selectedKeys();
            }catch (Throwable t) {
                t.printStackTrace();
                continue;
            }
            Iterator<SelectionKey> ite = selectedKeys.iterator();
            SelectionKey selectionKey;
            while(ite.hasNext()) {
                selectionKey = ite.next();
                SocketChannel socketChannel;
                WorkerHandler workerHandler;
                if (selectionKey.isAcceptable()) {
                    try {
                        socketChannel = serverSocketChannel.accept();
                        new WorkerHandler(socketChannel, selector);
                    }catch (Throwable t) {
                        t.printStackTrace();
                        continue;
                    }
                } else if (selectionKey.isReadable() || selectionKey.isWritable()) {
                    selectionKey.interestOps(selectionKey.interestOps() & ~SelectionKey.OP_READ);
                    workerHandler = (WorkerHandler)selectionKey.attachment();
                    cachedThreadPool.submit(workerHandler);
                }
                ite.remove();
            }
        }
    }
}
