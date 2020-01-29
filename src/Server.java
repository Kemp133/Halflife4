import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine();
            ServerSocket ss = new ServerSocket(6666);
            Socket socket = ss.accept();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String str = (String) dis.readUTF();
            System.out.println("Client says: " + str);
            ss.close();
        } catch (Exception e) {
            System.out.println("Doh!:" + e);
        }
    }
}
