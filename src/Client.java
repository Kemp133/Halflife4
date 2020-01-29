import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

        String ip = in.readLine();
        int port = 6666;
        System.out.println("IP: " + ip);

        Socket client = new Socket("localhost", port);
        System.out.println("Connected");

        DataOutputStream dOut = new DataOutputStream(client.getOutputStream());
        dOut.writeUTF("UwU ;-; C: TT ^^");
        dOut.flush();
        dOut.close();
        client.close();

    }
}
