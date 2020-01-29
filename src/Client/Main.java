public class Main {

    public static void main(String[] args) {
        Client client = new Client("92.236.117.224",1234);
        client.connect();

        AddConnectionPacket packet = new AddConnectionPacket();
        client.sendObject(packet);

    }

}