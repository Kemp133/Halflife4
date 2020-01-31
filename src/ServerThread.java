import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

class ServerThread implements Runnable{
    public static String log_out = "exit";
    private final AtomicBoolean running = new AtomicBoolean(false);
    Socket s = null;
    BufferedReader br = null;

    public ServerThread(Socket s) throws IOException{
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        running.set(true);
        while(running.get()){
            try {
                String content = null;
                while ((content = readFromClient()) != null){
                    
                    if(content.equals(log_out)){
                        System.out.println("last client logged out...");
                        App.sockets.remove(s);
                        Thread.currentThread().interrupt();
                    }
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
        
        //get the server ip @from Dom Server.
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp())
                continue;
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet6Address) continue;
                ipv4 = addr.getHostAddress();
                if (iface.getDisplayName().startsWith("Q")) {
                    System.out.println("Server IP: " + ipv4);
                }
            }
        }
        
        //start the server
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



