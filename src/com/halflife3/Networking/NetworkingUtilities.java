package com.halflife3.Networking;

import java.io.*;

/** A class to store all networking related methods */
public class NetworkingUtilities {
	/**
	 * Converts an object (packet) into a byte array
	 * @param o The object to turn into a byte array
	 * @return A byte array representing the original object
	 */
	public static byte[] objectToByteArray (Object o) {
		byte[] sendBuf = null;
		try (var byteStream = new ByteArrayOutputStream()) {
			try (var outputStream = new ObjectOutputStream(new BufferedOutputStream(byteStream))) {
				outputStream.flush();
				outputStream.writeObject(o);
				outputStream.flush();
				sendBuf = byteStream.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sendBuf;
	}

	/**
	 * Converts a byte array into an object (packet)
	 * @param buf The byte array to convert into an object
	 * @return The object created from the byte array
	 */
	public static Object byteArrayToObject (byte[] buf) {
		Object o = null;
		try (var byteStream = new ByteArrayInputStream(buf)) {
			try (var inputStream = new ObjectInputStream(new BufferedInputStream(byteStream))) {
				o = inputStream.readObject();
			}
		} catch (IOException | ClassNotFoundException ignored) {/*e.printStackTrace();*/}
		return o;
	}

	/** Waaaaait a second... */
	public static void waitASecond() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ignored) {} //renamed variable to ignored, stops compiler complaining and nothing useful is done anyways
	}
}
