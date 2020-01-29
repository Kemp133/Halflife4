import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {

        //Enter the server's IP
        System.out.print("Enter the server IP address: ");
        Scanner in = new Scanner(System.in);
        String ip = in.nextLine();
        int port = 6666;

        //Connect to the server with the IP
        Socket client = new Socket("192.168.0.38", port);
        System.out.println("Connected to " + ip);

        //Send input to the server
        DataOutputStream dOut = new DataOutputStream(client.getOutputStream());
        System.out.println("Enter your message:");
        dOut.writeUTF(in.nextLine());

        //Clean up and close the sockets
        dOut.flush();
        dOut.close();
        client.close();

    }
}
