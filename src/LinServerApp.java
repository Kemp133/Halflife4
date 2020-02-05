import java.io.*;
import java.net.*;

class LinServerThread implements Runnable {
    private int DGRAM_LENGTH;
    private byte[] b;
    private static final String MCAST_ADDR = "235.1.1.1";
    //private static final int DEST_PORT = 6666;
    public LinServerThread(int data_length){
        this.DGRAM_LENGTH = data_length;
        this.b = new byte[data_length];
    }
    @Override
    public void run() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket(9898);
            //receive dgram

            DatagramPacket dgram = new DatagramPacket(b, b.length);

            datagramSocket.receive(dgram);

            System.err.println("Sending " + byteArrayToStr(b) +
                    dgram.getAddress() + ':' + dgram.getPort());

            /*while (true) {
                System.err.print(".");
                datagramSocket.send(dgram);
                Thread.sleep(1000);
            }*/
            //datagramSocket.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public byte[] getB(){
        return b;
    }

    //@google https://www.jianshu.com/p/17e771cb34aa
    public static String byteArrayToStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray);
        return str;
    }
}

public class LinServerApp {
    private static final int LENGTH = 256;
    public static void main(String[] args) throws IOException {

        //start the server
        System.out.println("Awaiting clients...");
        LinServerThread server = new LinServerThread(LENGTH);
        new Thread(server).start();
        byte[] data = server.getB();
        String str = new String(data);
        while(str.equals("exm"))
            System.out.println("String is" + str);
        //Thread.interrupted();
    }

}

