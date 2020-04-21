package com.halflife3.Mechanics;

import com.halflife3.GameObjects.Vector2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Vector2Test {
	double x1 = 5, y1 = 15;
	double x2 = 10, y2 = 20;

	//region Vector Addition
	@Test
	@DisplayName("Adding a vector to another vector")
	public void AddVectorAndVector() {
		Vector2 v1       = new Vector2(x1, y1);
		Vector2 v2       = new Vector2(x2, y2);
		Vector2 expected = new Vector2(x1 + x2, y1 + y2);

		assertEquals(expected, v1.add(v2));
	}

	@Test
	@DisplayName("Adding component values to a vector")
	public void AddValuesAndVector() {
		Vector2 v1 = new Vector2(x1, y1);
		Vector2 expected = new Vector2(x1 + x2, y1 + y2);

		assertEquals(expected, v1.add(x2, y2));
	}
	//endregion

	//region Vector Subtraction
	@Test
	@DisplayName("Subtracting a vector from another vector")
	public void SubtractVectorFromVector() {
		Vector2 v1 = new Vector2(x1, y1);
		Vector2 v2 = new Vector2(x2, y2);
		Vector2 expected = new Vector2(x1 - x2, y1 - y2);

		assertEquals(v1.subtract(v2), expected);
	}

	@Test
	@DisplayName("Subtracting component values from a vector")
	public void SubtractValuesFromVector() {
		Vector2 v1 = new Vector2(x1, y1);
		Vector2 expected = new Vector2(x1 - x2, y1 - y2);

		assertEquals(v1.subtract(x2, y2), expected);
	}
	//endregion

	//region Vector Multiplication
	@Test
	@DisplayName("Multiplying a vector by a scalar")
	public void MultiplyVectorByScalar() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x1, y1);
		Vector2 expected1 = new Vector2(x1 * x2, y1 * x2);
		Vector2 expected2 = new Vector2(x1 * y2, y1 * y2);

		Assertions.assertAll(
				"Asserting both vector multiplications",
				() -> assertEquals(v1.multiply(x2), expected1),
				() -> assertEquals(v2.multiply(y2), expected2)
		);
	}
	//endregion

	//region Vector Division
	@Test
	@DisplayName("Dividing a vector by a scalar")
	public void DivideVectorByScalar() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x1, y1);
		Vector2 expected1 = new Vector2(x1 / x2, y1 / x2);
		Vector2 expected2 = new Vector2(x1 / y2, y1 / y2);

		Assertions.assertAll(
				"Asserting both vector divisions",
				() -> assertEquals(v1.divide(x2), expected1),
				() -> assertEquals(v2.divide(y2), expected2)
		);
	}
	//endregion

	//region Distance Between Vectors
	@Test
	@DisplayName("Getting euclidean distance of vectors")
	public void GetVectorDistance() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double expected = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));

		Assertions.assertAll(
				"Asserting distance same from each vector",
				() -> assertEquals(v1.distance(v2), expected),
				() -> assertEquals(v2.distance(v1), expected)
		);
	}

	@Test
	@DisplayName("Getting euclidean distance of vectors")
	public void GetSquareVectorDistance() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double expected = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);

		Assertions.assertAll(
				"Asserting distance same from each vector",
				() -> assertEquals(v1.squareDistance(v2), expected),
				() -> assertEquals(v2.squareDistance(v1), expected)
		);
	}
	//endregion

	//region Vector Magnitude
	@Test
	@DisplayName("Getting magnitude of vector")
	public void GetVectorMagnitude() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double expected1 = Math.sqrt(Math.pow(x1, 2) + Math.pow(y1, 2));
		double expected2 = Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));

		Assertions.assertAll(
				"Asserting magnitudes are correct",
				() -> assertEquals(v1.magnitude(), expected1),
				() -> assertEquals(v2.magnitude(), expected2)
		);
	}
	//endregion

	//region Vector Normalisation
	@Test
	@DisplayName("Getting normalised vector")
	public void GetNormalisedVector() {
		Vector2 v1        = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double  mag1      = CalculateMagnitude(x1, y1), mag2 = CalculateMagnitude(x2, y2);
		Vector2 expected1 = new Vector2(x1 / mag1, y1/mag1), expected2 = new Vector2(x2 / mag2, y2/mag2);

		assertAll(
				"Asserting all vectors normalise correctly",
				() -> assertEquals(v1.normalise(), expected1),
				() -> assertEquals(v2.normalise(), expected2)
		);
	}
	//endregion

	//region Vector Mirroring
	@Test
	@DisplayName("Getting reversed vector")
	public void GetReversedVector() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		Vector2 expected1 = new Vector2(-x1, -y1), expected2 = new Vector2(-x2, -y2);

		assertAll(
				"Asserting both vectors reverse correctly",
				() -> assertEquals(v1.reverse(), expected1),
				() -> assertEquals(v2.reverse(), expected2)
		);
	}
	//endregion

	//region Get/Set components
	@Test
	@DisplayName("Get x from vector")
	public void GetXFromVector() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);

		assertAll(
				"Asserting that getX() gets the correct value",
				() -> assertEquals(v1.getX(), x1),
				() -> assertEquals(v2.getX(), x2)
		);
	}

	@Test
	@DisplayName("Set x in vectors")
	public void SetXInVector() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double x3 = 100, x4 = 10000;
		Vector2 expected1 = new Vector2(x3, y1), expected2 = new Vector2(x4, y2);

		//Using setX functions to change x values
		v1.setX(x3);
		v2.setX(x4);

		assertAll(
				"Asserting that setX(x) correctly sets the x value",
				() -> assertEquals(v1, expected1),
				() -> assertEquals(v2, expected2)
		);
	}

	@Test
	@DisplayName("Get y from vectors")
	public void GetYFromVector() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);

		assertAll(
				"Asserting that getY() gets the correct value",
				() -> assertEquals(v1.getY(), y1),
				() -> assertEquals(v2.getY(), y2)
		);
	}

	@Test
	@DisplayName("Set y in vectors")
	public void SetYInVector() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		double y3 = -100, y4 = -10000;
		Vector2 expected1 = new Vector2(x1, y3), expected2 = new Vector2(x2, y4);

		//Using setX functions to change x values
		v1.setY(y3);
		v2.setY(y4);

		assertAll(
				"Asserting that setY(y) correctly sets the y value",
				() -> assertEquals(v1, expected1),
				() -> assertEquals(v2, expected2)
		);
	}
	//endregion

	//region Overridden Methods
	@Test
	@DisplayName("Testing toString method of the vectors")
	public void VectorToString() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2);
		String expected1 = GetToStringRepresentation(x1, y1), expected2 = GetToStringRepresentation(x2, y2);

		assertAll(
				"Asserting toString method working for vectors",
				() -> assertEquals(v1.toString(), expected1),
				() -> assertEquals(v2.toString(), expected2)
		);
	}

	@Test
	@DisplayName("Testing equals method of the vectors")
	public void VectorEquals() {
		Vector2 v1 = new Vector2(x1, y1), v2 = new Vector2(x2, y2), v3 = new Vector2(x1, y1);

		assertAll(
				"Asserting equals method is working for the vectors",
				() -> assertNotEquals(v1.equals(v2), true), //assert v1 is not equal to v2
				() -> assertNotEquals(v2.equals(v1), true), //as above, but other way around (just in case)
				() -> assertTrue(v1.equals(v3)) //check that v1 equals v3
		);
	}
	//endregion

	//region Deep Copy
	@Test
	@DisplayName("Vector deep copy")
	public void VectorDeepCopy() {
		Vector2 v1 = new Vector2(x1, y2);
		Vector2 v2 = new Vector2(v1);

		//Check that they're equal
		assertTrue(v2.equals(v1));

		//Modify the first vector
		v1.setY(123456);

		//Check to make sure they're not equal (otherwise the copy was shallow)
		assertFalse(v2.equals(v1));
	}
	//endregion

	//region Useful known good methods
	/**
	 * This method is used as an analogue for the method in the {@code Vector2} class, and is known to work
	 * @param x First component
	 * @param y Second component
	 * @return The magnitude of the given components
	 */
	private double CalculateMagnitude (double x, double y) {
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}

	/**
	 * This method is used to get the desired toString representation of the vector. Here in the event
	 * this representation ever changes so tests are easier to modify
	 * @param x The x value of the vector
	 * @param y The y value of the vector
	 * @return The desired representation which toString in {@code Vector2} should provide
	 */
	private String GetToStringRepresentation(double x, double y) {
		return "(" + x + ", " + y + ")";
	}
	//endregion
}