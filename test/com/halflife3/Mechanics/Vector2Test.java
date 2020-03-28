package com.halflife3.Mechanics;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vector2Test {

	@Test
	@DisplayName("Adding a vector to another vector")
	void AddVectorAndVector() {
		int x1 = 5, y1 = 15;
		int x2 = 10, y2 = 20;

		Vector2 v1 = new Vector2(x1, y1);
		Vector2 v2 = new Vector2(x2, y2);
		Vector2 added = new Vector2(x1 + x2, y1 + y2);

		assertEquals(added, v1.add(v2));
	}

	@Test
	@DisplayName("Adding component values to a vector")
	void AddVectorAndValues() {
		int x1 = 5, y1 = 15;
		int x2 = 10, y2 = 20;

		Vector2 v1 = new Vector2(x1, y1);
		Vector2 added = new Vector2(x1 + x2, y1 + y2);

		assertEquals(added, v1.add(x2, y2));
	}
}
