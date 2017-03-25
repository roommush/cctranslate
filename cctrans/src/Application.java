import nio.handler.ServerHandler;

/**
 * Created by yangchinqi on 3/18/17.
 */
public class Application {

    public static void main(String[] args){
        try {
            new ServerHandler(8080).start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
