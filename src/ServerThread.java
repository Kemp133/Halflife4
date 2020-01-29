import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class ServerThread implements Runnable{
    Socket s = null;
    BufferedReader br = null;

    public ServerThread(Socket s) throws IOException{
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String content = null;
            while ((content = readFromClient()) != null){
                System.out.println("From client：" + content);
                for (Socket socket : App.sockets) {
                    PrintStream ps = new PrintStream(s.getOutputStream());
                    ps.println(content);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromClient(){
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            App.sockets.remove(s);
        }
        return null;
    }
}
class App {
    private static final int SERVER_PORT = 6000;
    public static ArrayList<Socket> sockets = new ArrayList<Socket>();
    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Starting searching for...");
        while(true){
            Socket s = serverSocket.accept();
            System.out.println("someone in ...");
            sockets.add(s);
            new Thread(new ServerThread(s)).start();
        }
    }
}



