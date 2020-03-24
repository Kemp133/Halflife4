package com.halflife3.Mechanics.AI;

import com.halflife3.Mechanics.Vector2;

import java.util.ArrayList;

public class Node {
    public String type = "Free"; // "Free" || "Wall"
    public double h = 0; // NodeToEnd
    public double g = 0; // StartToNode
    public double f = 0; // TotalCost
    public ArrayList<Node> children = new ArrayList<>();
    private Node parent;
    public Vector2 position;

    public Node (Vector2 position) { this.position = position; }

    public void addChild(Node child) { children.add(child); }

    public Node getParent() { return parent; }

    public void setParent(Node parent) { this.parent = parent; }

    @Override
    public String toString() {
        return "Node{ Position: " + position.getY() + "|" + position.getX() + '}';
    }
}
