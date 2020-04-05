package com.halflife3.Networking;

import javafx.scene.control.Alert;

import java.io.*;
import java.net.*;
import java.util.Enumeration;

/**
 * A class to hold useful Networking based utilities (mainly for Client and Server) to reduce code duplication inside
 * of the client and server
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
		try (ByteArrayInputStream bs = new ByteArrayInputStream(buf)) {
			try (ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(bs))) {
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
		try (ByteArrayOutputStream bs = new ByteArrayOutputStream()) {
			try (ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(bs))) {
				os.writeObject(o);
				os.flush();
				sendBuf = bs.toByteArray();
			}
		} catch (IOException e) { e.printStackTrace(); }
		return sendBuf;
	}

	/**
	 * A method to firstly set the interface to WiFi (if it exists), and then all addresses to find a suitable address
	 * to use for datagram packets
	 *
	 * @return A suitable address to use, using the WiFi interface
	 *
	 * @throws SocketException In the event getting the correct interface and finding a suitable socket is unsuccessful
	 */
	public static InetAddress setWifiInterface() throws SocketException {
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

		throw new SocketException("Interface could not be set");
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
}