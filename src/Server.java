import java.io.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(6666);
            System.out.println("Awaiting client...");

            Socket client = server.accept();
            System.out.println("Client connected");

            DataInputStream dIn = new DataInputStream(client.getInputStream());
            String message = dIn.readUTF();
            System.out.println("Client says: " + message);

            server.close();
        } catch (Exception e) {
            System.out.println("Doh!:" + e);
        }
    }
}
