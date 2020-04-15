package com.halflife3.Networking.Client;

import com.halflife3.GameObjects.Vector2;
import com.halflife3.Networking.NetworkingUtilities;
import com.halflife3.Networking.Packets.ConnectPacket;
import com.halflife3.Networking.Packets.DisconnectPacket;
import com.halflife3.Networking.Packets.PositionListPacket;
import com.halflife3.Networking.Server.Server;

import java.io.IOException;
import java.net.*;

import static com.halflife3.Networking.Server.Server.GET_PORT_PORT;

public class Client {

	//region Variables
//    For "catching" the server
	protected MulticastSocket serverSocket;
	protected InetAddress     group;

	//    For receiving clients' positions
	protected MulticastSocket positionSocket;
	private   int             incPacketSize = 2000;

	//    For sending packets to the server
	private InetAddress    hostAddress;
	private DatagramSocket outSocket;

	//    Client's data
	public  InetAddress         clientAddress;
	private int                 uniquePort;
	private EventListenerClient listenerClient;
	public  Vector2             startingPosition;

	//    List of Clients
	public PositionListPacket listOfClients;
	//endregion

	public Client() {
		try {
			listenerClient = new EventListenerClient();
			serverSocket   = new MulticastSocket(Server.MULTICAST_PORT);
			positionSocket = new MulticastSocket(Server.POSITIONS_PORT);
			outSocket      = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Joins the multicast group on the static <code>Server.MULTICAST_PORT</code> to listen for packets multicasted by
	 * the server and sets the interface of the <code>serverSocket</code> and the <code>positionSocket</code> to the
	 * preferred IPv4 address
	 */
	public void joinMulticastGroup() {
		try {
			System.out.println("Searching for a multicast group...");
			group = InetAddress.getByName(Server.MULTICAST_ADDRESS);

			serverSocket   = new MulticastSocket(Server.MULTICAST_PORT);
			positionSocket = new MulticastSocket(Server.POSITIONS_PORT);

			clientAddress = NetworkingUtilities.getWifiInterface();
			serverSocket.setInterface(clientAddress);
			positionSocket.setInterface(clientAddress);

			serverSocket.joinGroup(group);
			positionSocket.joinGroup(group);

			System.out.println(
					"Joined group: " + Server.MULTICAST_ADDRESS + " with address: " + clientAddress.toString());
		} catch (ConnectException e) {
			System.out.println("Unable to join the group");
			NetworkingUtilities.CreateErrorMessage("Unable To Join The Group",
					"The connection to the group was unsuccessful", "Message: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listens for a packet on the server multicast port and after receiving it registers and prints out the server's
	 * IPv4 address
	 */
	public void getHostInfo() {
		try {
//            Receives the Welcome packet
			DatagramPacket firstPacket = new DatagramPacket(new byte[0], 0);
			System.out.println("Looking for host...");
			serverSocket.receive(firstPacket);

//            Gets the server's address
			hostAddress = firstPacket.getAddress();

			System.out.println("Host found: " + hostAddress);
			System.out.println("Waiting for unique port...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends the serialised <code>ConnectPacket</code> class object to the server, letting it know the player has
	 * connected to the game, and gets the unique port and starting position from the server
	 */
	public void connectToServer() {
//        Lets the server know the client has connected
		sendPacket(new ConnectPacket(), Server.LISTENER_PORT);

//        Gets the unique port to communicate with the server
		getUniqueInfo();

		System.out.println("Client connection set up. Starting game...");
	}

	/**
	 * Sends the serialised <code>DisconnectPacket</code> class object to the server, letting it know the player has
	 * disconnected from the game, and closes all communication sockets
	 */
	public void disconnect() {
		sendPacket(new DisconnectPacket(), uniquePort);

		serverSocket.close();
		outSocket.close();
		positionSocket.close();
	}

	/**
	 * Briefly creates a new <code>MulticastSocket</code> for the <code>serverSocket</code>, receives the
	 * <code>UniquePortPacket</code> class object which is then processed by the client event listener to initialise
	 * the unique communication port and the starting position of the player, and changes
	 * <code>serverSocket</code> back to the previous <code>MulticastSocket</code>
	 */
	public void getUniqueInfo() {
		try {
			serverSocket = new MulticastSocket(GET_PORT_PORT);
			serverSocket.setInterface(clientAddress);
			serverSocket.joinGroup(group);

			receivePacket();

			serverSocket = new MulticastSocket(Server.MULTICAST_PORT);
			serverSocket.setInterface(clientAddress);
			serverSocket.joinGroup(group);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Unique Client Port: " + uniquePort);
	}

	/**
	 * Receives any serialised class as an array of bytes, converts it back to an object by calling the
	 * byteArrayToObject() method and passing it the byte array, and passes the object to
	 * the client event listener for further processing
	 */
	public void receivePacket() {
		try {
			byte[] recBuf = new byte[3000];
			var    packet = new DatagramPacket(recBuf, recBuf.length);
			serverSocket.receive(packet);
			Object o = NetworkingUtilities.byteArrayToObject(recBuf);
			listenerClient.received(o, this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receives the serialised <code>PositionListPacket</code> class as an array of bytes, converts it back to the
	 * class object by calling the byteArrayToObject() method and passing it the byte array, and passes the object to
	 * the client event listener for further processing
	 */
	public void receivePositions() {
		try {
			byte[] recBuf = new byte[incPacketSize];
			var    packet = new DatagramPacket(recBuf, recBuf.length);
			positionSocket.receive(packet);
			Object o = NetworkingUtilities.byteArrayToObject(recBuf);
			if (incPacketSize == 2000)
				incPacketSize = NetworkingUtilities.objectToByteArray(o).length + 100;
			listenerClient.received(o, this);
		} catch (SocketException ignored) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends any object passed to it as the first argument to the server with a port, passed as the
	 * second argument.
	 *
	 * @param objectToSend Object to be sent
	 * @param port         Port used on which the server will be listening for the packet
	 */
	public void sendPacket(Object objectToSend, int port) {
		byte[] tempBuf = NetworkingUtilities.objectToByteArray(objectToSend);
		var    packet  = new DatagramPacket(tempBuf, tempBuf.length, hostAddress, port);
		try {
			outSocket.send(packet);
		} catch (NullPointerException | SocketException ignored) {
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//region Getters and setters
	public InetAddress getClientAddress() { return clientAddress; }

	public Vector2 getStartingPosition() {
		return startingPosition;
	}

	public int getUniquePort() {
		return uniquePort;
	}

	public void setUniquePort(int uniquePort) {
		this.uniquePort = uniquePort;
	}
	//endregion
}
