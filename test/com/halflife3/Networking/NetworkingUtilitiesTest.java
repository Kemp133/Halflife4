package com.halflife3.Networking;


import com.halflife3.Networking.Packets.PositionPacket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkingUtilitiesTest {

	@Test
	@DisplayName("Object to byte array and back")
	public void ObjectToObject() {
		PositionPacket pp = new PositionPacket();
		pp.posX = 1;
		pp.velX = 3;
		pp.holdsBall = true;

		PositionPacket pp2 = new PositionPacket();

		Object ppObjectToObject = NetworkingUtilities.byteArrayToObject(NetworkingUtilities.objectToByteArray(pp));

		if (ppObjectToObject instanceof PositionPacket) {
			pp2 = (PositionPacket) ppObjectToObject;
		}

		PositionPacket finalPp = pp2;
		Assertions.assertAll(
				() -> assertEquals(pp.posX, finalPp.posX),
				() -> assertEquals(pp.velX, finalPp.velX),
				() -> assertEquals(pp.holdsBall, finalPp.holdsBall)
		);
	}
}