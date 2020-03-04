package com.halflife3.View;

import com.halflife3.Model.Vector2;

public class Camera {
	private static Vector2 offset;

	public Camera() {}

	static {
		offset = new Vector2();
	}

	public static void SetOffset(Vector2 toSet) {
		offset = toSet;
	}
	public static void SetOffsetX(double x) { offset.setX(x);}
	public static void SetOffsetY(double y) { offset.setY(y);}

	public static Vector2 GetOffset() {
		return new Vector2(offset);
	}
	public static double GetOffsetX() { return offset.getX(); }
	public static double GetOffsetY() { return offset.getY(); }
}
