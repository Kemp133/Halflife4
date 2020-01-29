import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        String ip = in.readLine();
        int port = 6666;

        Socket client = new Socket(ip, port);
        DataOutputStream dOut = new DataOutputStream(client.getOutputStream());

        dOut.writeUTF("Wooh! Connected to the server");
        dOut.flush();
        dOut.close();
        client.close();

    }
}
