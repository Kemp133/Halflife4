import java.io.*;
import java.net.*;

class DomServerThread implements Runnable {
    private static final int DGRAM_LENGTH = 32;
    private static final String MCAST_ADDR = "235.1.1.1";
    private static final int DEST_PORT = 6666;

    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();

            byte[] b = new byte[DGRAM_LENGTH];
            DatagramPacket dgram;

            dgram = new DatagramPacket(b, b.length,
                    InetAddress.getByName(MCAST_ADDR), DEST_PORT);

            System.err.println("Sending " + b.length + " bytes to " +
                    dgram.getAddress() + ':' + dgram.getPort());
            while (true) {
                System.err.print(".");
                datagramSocket.send(dgram);
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

public class DomServerApp {

    public static void main(String[] args) throws IOException {
        //start the server
        ServerSocket serverSocket = new ServerSocket(6666);
        System.out.println("Awaiting clients...");
        Socket s = serverSocket.accept();
    }
}
