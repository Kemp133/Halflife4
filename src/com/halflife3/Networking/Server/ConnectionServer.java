package com.halflife3.Networking.Server;

import com.halflife3.Networking.AddConnectionPacket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConnectionServer implements Runnable{

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public int id;
    private EventListenerServer listener;
    private boolean running = false;

    public ConnectionServer(Socket socket, int id) {
        this.socket = socket;
        this.id = id;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            listener = new EventListenerServer();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            running = true;

            AddConnectionPacket acp = new AddConnectionPacket();
            acp.id = 1111;
            sendObject(acp);

            while(running) {
                try {
                    Object data = in.readObject();
                    listener.received(data, this);
                }catch(ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            running = false;
            in.close();
            out.close();
            socket.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object packet) {
        try {
            out.writeObject(packet);
            out.flush();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}