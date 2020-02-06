import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

class McastClientThread implements Runnable{
    private static final String MCAST_ADDR = "127";
    private static final int DEST_PORT = 6666;
    private int DGRAM_LENGTH;
    private byte[] b = new byte[256];
    //Example String
    String exm = "exm";

    public McastClientThread(int length){
        b = exm.getBytes();
    }

    public void run(){
        try {
            DatagramPacket dgram = new DatagramPacket(b,DGRAM_LENGTH, new InetSocketAddress("localhost", 9898));

            DatagramSocket send_portal = new DatagramSocket();

            send_portal.send(dgram);

            System.err.println("Send to " + byteArrayToStr(dgram.getData()) + " portal " + dgram.getSocketAddress().toString());
            send_portal.close();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //@google https://www.jianshu.com/p/17e771cb34aa
    public static String byteArrayToStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray);
        return str;
    }
    public static byte[] strToByteArray(String str) {
        if (str == null) {
            return null;
        }
        byte[] byteArray = str.getBytes();
        return byteArray;
    }
}
public class LinClientApp {

    public static void main(String[] args){
        McastClientThread client = new McastClientThread(256);
        new Thread(client).start();
        System.out.println("Send to server");
        Thread.interrupted();
    }
}
