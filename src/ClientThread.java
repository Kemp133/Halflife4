import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


class ClientThread implements Runnable{
    private BufferedReader br;
    private Socket client;

    public ClientThread(Socket client) throws IOException{
        this.client = client;
        br = new BufferedReader(new InputStreamReader(client.getInputStream()));
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

    public void stop(){
        try {
            client.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientApp {
    public static String log_out = "exit";
    private static final int SERVER_PORT=6000;

    public static void main(String[] args) throws IOException{
        //Enter the server's IP
        System.out.println("Enter the server IP address: ");
        Scanner in = new Scanner(System.in);
        String ip = in.nextLine();

        //Connect to the server with the IP
        Socket s = new Socket(ip, SERVER_PORT);
        System.out.println("Connected to " + ip);
        new Thread(new ClientThread(s)).start();

        PrintStream ps = new PrintStream(s.getOutputStream());
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while((line = br.readLine()) != null){
            if (line.equals(log_out)) {
                ps.println(line);
                System.out.println("You have disconnected!");
                break;
            }
            ps.println(line);
        }
        Thread.currentThread().interrupt();
    }
}


`