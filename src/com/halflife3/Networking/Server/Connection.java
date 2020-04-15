package com.halflife3.Networking.Server;

import com.halflife3.GameObjects.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Packets.UniquePortPacket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Connection implements Runnable {
	private final int                 lengthOfPackets;
	private final Server              server;
	private final Vector2             spawnPoint;
	private final ClientList          clientList;
	private final InetAddress         clientAddress;
	private final EventListenerServer listenerServer;
	private       DatagramSocket      uniqueSocket;
	private       Vector2             client_position;
	private       boolean             running;
	private final ExecutorService     executor = Executors.newSingleThreadExecutor();

	public Connection(InetAddress ip, int port, UniquePortPacket upp, Server s, ClientList cl) {
		server          = s;
		clientList      = cl;
		clientAddress   = ip;
		spawnPoint      = upp.getStartPosition();
		client_position = upp.getStartPosition();
		listenerServer  = new EventListenerServer();
		lengthOfPackets = NetworkingUtilities.objectToByteArray(new PositionPacket()).length;
		try { uniqueSocket = new DatagramSocket(port); } catch (SocketException e) { e.printStackTrace(); }
		executor.submit(() -> {
			while (true) {
				s.multicastPacket(upp, Server.GET_PORT_PORT);
				NetworkingUtilities.WaitXSeconds(1);
			}
		});

	}

	@Override
	public void run() {
		connectionListener();
		executor.shutdown();
		running = true;
		while (running) {
			connectionListener();
		}
	}

	public void close() {
		running = false;
		uniqueSocket.close();
	}

	private void connectionListener() {
		byte[]         posBuf = new byte[lengthOfPackets];
		DatagramPacket incPos = new DatagramPacket(posBuf, posBuf.length);

		try { uniqueSocket.receive(incPos); } catch (IOException e) { return; }

		Object receivedPosition = NetworkingUtilities.byteArrayToObject(posBuf);
		listenerServer.received(receivedPosition, clientAddress, server, clientList);
	}

	public Vector2 getPosition() {
		return client_position;
	}

	public void setPosition(Vector2 position) {
		this.client_position = position;
	}

	public Vector2 getSpawnPoint() {
		return spawnPoint;
	}
}