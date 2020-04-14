package com.halflife3.Networking;

import javafx.scene.control.*;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * A class to hold useful Networking based utilities (mainly for Client and Server) to reduce code duplication inside of
 * the client and server
 */
public class NetworkingUtilities {
	/**
	 * A method to convert a given byte array into an object
	 *
	 * @param buf The byte buffer to convert
	 *
	 * @return The object created from the given byte buffer
	 */
	public static Object byteArrayToObject(byte[] buf) {
		Object o = null;
		try (var bs = new ByteArrayInputStream(buf)) {
			try (var is = new ObjectInputStream(new BufferedInputStream(bs))) {
				o = is.readObject();
			}
		} catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
		return o;
	}

	/**
	 * A method to convert a given object into a byte array
	 *
	 * @param o The object to convert
	 *
	 * @return The byte array created from the given object
	 */
	public static byte[] objectToByteArray(Object o) {
		byte[] sendBuf = null;
		try (var bs = new ByteArrayOutputStream()) {
			try (var os = new ObjectOutputStream(new BufferedOutputStream(bs))) {
				os.writeObject(o);
				os.flush();
				sendBuf = bs.toByteArray();
			}
		} catch (IOException e) { e.printStackTrace(); }
		return sendBuf;
	}

	/**
	 * A method which sets the destination for Send/Receive, discards all packets from other addresses, transfers
	 * the socket into a "connected" state, setting its appropriate fields. This includes checking the existence of
	 * the route to the destination according to the system's routing table and setting the local endpoint
	 * accordingly. Then the preferred outbound IP is returned.
	 *
	 * @return The preferred outbound IPv4 address
	 *
	 * @throws SocketException if a new DatagramSocket could not be created
	 * @throws UnknownHostException if a host by the name "8.8.8.8" could not be found
	 */
	public static InetAddress getWifiInterface() throws SocketException, UnknownHostException {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress();
		}
	}

	/**
	 * A method which iterates through all the network interfaces of the machine and returns the IPv4 address of the
	 * Wi-Fi interface if the machine is connected to a Wi-Fi network
	 *
	 * @return A suitable IPv4 address to use, using the WiFi interface
	 *
	 * @throws SocketException In the event getting the correct address and finding a suitable socket is unsuccessful
	 */
	public static InetAddress getWifiInterface2() throws SocketException {
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface net = interfaces.nextElement();
			if (!net.getName().startsWith("wlan") || !net.isUp())
				continue;

			Enumeration<InetAddress> addresses = net.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				if (addr.toString().length() < 17) {
					return addr;
				}
			}
		}

		throw new SocketException("Interface could not be found");
	}

	/**
	 * A helper method to create an error message alert to create nicer exceptions for us to use
	 *
	 * @param title      The title of the {@code Alert}
	 * @param headerText The header text of the {@code Alert}
	 * @param content    The body text of the {@code Alert}
	 */
	public static void CreateErrorMessage(String title, String headerText, String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(content);
		alert.show();
	}

	/**
	 * A method used to halt the program for X number of seconds
	 *
	 * @param secondsToWait The time (in seconds) to wait
	 */
	public static void WaitXSeconds(int secondsToWait) {
		try { Thread.sleep(secondsToWait * 1000); } catch (InterruptedException ignored) {}
	}
}