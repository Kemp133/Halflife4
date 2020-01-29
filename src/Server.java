import java.io.*;
import java.net.*;
import java.util.Enumeration;

public class Server {

    public static void main(String[] args) throws IOException {
        String ipv4;

        //Get the server IPv4 address
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

        //Setup a server socket
        ServerSocket server = new ServerSocket(6666);
        System.out.println("Awaiting client...");

        //Connect a client
        Socket client = server.accept();
        System.out.println("Client connected");

        //Read from the client input
        DataInputStream dIn = new DataInputStream(client.getInputStream());
        String message = dIn.readUTF();
        System.out.println("Client says: " + message);

        //Close the server socket
        server.close();
    }
}
