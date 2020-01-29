public class EventListener {

    public void received(Object p) {
        if(p instanceof AddConnectionPacket) {
            AddConnectionPacket packet = (AddConnectionPacket)p;
            ConnectionHandler.connections.put(packet.id,new Connection(packet.id));
            System.out.println(packet.id + " has connected");
        }else if(p instanceof RemoveConnectionPacket) {
            RemoveConnectionPacket packet = (RemoveConnectionPacket)p;
            System.out.println("Connection: " + packet.id + " has disconnected");
            ConnectionHandler.connections.remove(packet.id);
        }
    }

}
