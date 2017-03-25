package nio.handler;

import ts.trans.LANG;
import ts.trans.Translator;
import ts.trans.factory.TFactory;
import ts.trans.factory.TranslatorFactory;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by yangchinqi on 3/21/17.
 */
public class WorkerHandler implements Runnable{
    private Selector selector;
    private SocketChannel socketChannel;
    public SelectionKey selectionKey;
    private ByteBuffer readBuffer;
    private ByteBuffer writeBuffer;
    private TFactory factory;
    private Translator translator;

    public WorkerHandler(SocketChannel socketChannel, Selector selector) throws Throwable{
        this.selector = selector;
        socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        readBuffer = ByteBuffer.allocate(socketChannel.socket().getReceiveBufferSize());
        writeBuffer = ByteBuffer.allocate(socketChannel.socket().getSendBufferSize());
        this.socketChannel = socketChannel;
        selectionKey.attach(this);
        factory = new TranslatorFactory();
        translator = factory.get("baidu");
        writeBuffer.put("\r\n你好,我是聪聪...\r\n\r\n[你说啥] : ".getBytes());
        writeBuffer.flip();
        fireWrite();
    }

    public void run() {
        try{
            if(selectionKey.isReadable()){
                fireRead();
            }else if(selectionKey.isWritable()){
                fireWrite();
            }
        }catch (Throwable t){
            t.printStackTrace();
            selectionKey.cancel();
            try {
                socketChannel.close();
            }catch (Throwable tt){
                tt.printStackTrace();
            }
        }
    }

    public void fireRead() throws Throwable{
        int read = socketChannel.read(readBuffer);
        if (read == -1) {
            System.out.println("client disconnected!");
            socketChannel.close();
        }
        //process translation
        getEnAnswer();
        //end
        /*test
        int size = socketChannel.socket().getSendBufferSize() * 10;
        writeBuffer = ByteBuffer.allocate(size);
        for(int i=0; i<size; i++){
            writeBuffer.put("a".getBytes());
        }
        end*/
        selectionKey.interestOps(selectionKey.interestOps() | SelectionKey.OP_READ);
        selector.wakeup();
        writeBuffer.flip();
        fireWrite();
    }

    public void fireWrite() throws Throwable{
        while(writeBuffer.hasRemaining()) {
            socketChannel.write(writeBuffer);
            writeBuffer.compact();
            writeBuffer.flip();
        }
    }

    public void getEnAnswer(){
        String current = "";
        String translate = "";
        String r = "";
        try {
            readBuffer.flip();
            current = Charset.forName("utf-8").decode(readBuffer).toString().trim();
            translate = translator.trans(LANG.ZH, LANG.EN, current);
            readBuffer.clear();
        }catch(Throwable t){
            translate = "对不起,人家没听懂...";
        }
        System.out.println("you say: " + current);
        System.out.println("I say: " + translate);
        if(current == null || "".equals(current.trim())){
            translate = "好吧呀,被调戏了...";
        }else if(translate == null || "".equals(translate.trim())){
            translate = "对不起,该改名叫笨笨了...";
        }else if(!current.contains("<") && translate.contains("<") || !current.contains("{") && translate.contains("{")){
            translate = "不好了,网络连不上了...";
        }
        r = "\r\n[聪聪] : " + translate + "\r\n\r\n[请指教] : ";
        writeBuffer.compact();
        writeBuffer.put(r.getBytes());
    }
}
