
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;


class ClientThread implements Runnable{
    private Socket s;
    BufferedReader br = null;

    public ClientThread(Socket s) throws IOException{
        this.s = s;
        br = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    @Override
    public void run() {
        String content = null;
        try {
            while((content = br.readLine()) != null){
                System.out.println(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class App {
    private static final int SERVER_PORT=6000;
    public static void main(String[] args) throws IOException{
        System.out.println("Starting to connect.");
        //change host here.
        Socket s = new Socket("192.168.0.35",SERVER_PORT);
        new Thread(new ClientThread(s)).start();
        PrintStream ps = new PrintStream(s.getOutputStream());
        String line = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while((line = br.readLine())!=null){
            ps.println(line);
        }
    }
}


