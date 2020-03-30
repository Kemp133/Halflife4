package com.halflife3.Networking;

import java.io.*;

public class NetworkingUtilities {
	/**
	 * A method to convert a given byte array into an object
	 * @param buf The byte buffer to convert
	 * @return The object created from the given byte buffer
	 */
	public static Object byteArrayToObject (byte[] buf) {
		Object o = null;
		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(buf)) {
			try (ObjectInputStream instream = new ObjectInputStream(new BufferedInputStream(byteStream))) {
				o = instream.readObject();
			}
		} catch (IOException | ClassNotFoundException ignored) {}
		return o;
	}

	/**
	 * A method to convert a given object into a byte array
	 * @param o The object to convert
	 * @return The byte array created from the given object
	 */
	public static byte[] objectToByteArray(Object o) {
		byte[] sendBuf = null;
		try(ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
			try(ObjectOutputStream outstream = new ObjectOutputStream(new BufferedOutputStream(byteStream))) {
				outstream.writeObject(o);
				outstream.flush();
				sendBuf = byteStream.toByteArray();
			}
		} catch (IOException ignored) {}
		return sendBuf;
	}
}