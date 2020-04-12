package com.halflife3.Networking.Server.AI;

import com.halflife3.GameObjects.Vector2;

import java.util.ArrayList;

public class Node {
	public  String          type     = "Free"; // "Free" || "Wall"
	public  double          h        = 0; // NodeToEnd
	public  double          g        = 0; // StartToNode
	public  double          f        = 0; // totalCost
	public  ArrayList<Node> children = new ArrayList<>();
	private Node            parent;
	public  Vector2         position; // Position on the map

	public Node(double x, double y) {
		position = new Vector2(x, y);
	}

	public void addChild(Node child)   { children.add(child); }

	public Node getParent()            { return parent; }

	public void setParent(Node parent) { this.parent = parent; }

	@Override
	public String toString() {
		return "Node{ Position: " + position.getY() + "|" + position.getX() + '}';
	}
}