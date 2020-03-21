package com.halflife3.Mechanics.AI;

import com.halflife3.Mechanics.GameObjects.Bricks;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.ClientListServer;
import com.halflife3.View.MapRender;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class AI {
	private final int ONE_MOVE = 40;
	private Node[][] map = null;

	public boolean setupMap() {
		try {
			Image mapImage = new Image(new FileInputStream("res/map.png"));
			int width = (int) mapImage.getWidth();
			int height = (int) mapImage.getHeight();

			map = new Node[height][width];

			PixelReader px = mapImage.getPixelReader();

			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					Color color = px.getColor(j,i);
					map[i][j] = new Node(new Vector2(i * ONE_MOVE, j * ONE_MOVE));
					if (color.equals(Color.BLACK))
						map[i][j].type = "Wall";
				}
			}

			for (int i = 1; i < height - 1; i++) {
				for (int j = 1; j < width - 1; j++) {
					map[i][j].addChild(map[i][j+1]);
					map[i][j].addChild(map[i+1][j]);
					map[i][j].addChild(map[i][j-1]);
					map[i][j].addChild(map[i-1][j]);
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println("Could not find file at location: 'res/map.png'");
			return false;
		}

		return true;
	}

	public PositionPacket getBotMovement(PositionPacket bot) {
		Set<Map.Entry<String, PositionPacket>> positions = ClientListServer.positionList.entrySet();
		double shortestDistance = Double.MAX_VALUE;
		Vector2 botPosition = new Vector2(bot.orgPosX, bot.orgPosY);
		Vector2 toGoTo = new Vector2();

//		  Selects the closest enemy to go to next
		for (HashMap.Entry<String, PositionPacket> enemyPacket : positions) {
			if (enemyPacket.getValue().equals(bot))
				continue;

			double distance = Math.pow(bot.orgPosX - enemyPacket.getValue().orgPosX, 2) +
					Math.pow(bot.orgPosY - enemyPacket.getValue().orgPosY, 2);

			if (distance < shortestDistance) {
				shortestDistance = distance;
				toGoTo.setX(enemyPacket.getValue().orgPosX);
				toGoTo.setY(enemyPacket.getValue().orgPosY);
			}
		}

		int botX = (int) (bot.orgPosX) / 40;
		int botY = (int) (bot.orgPosY) / 40;
		int endX = (int) (toGoTo.getX()) / 40;
		int endY = (int) (toGoTo.getY()) / 40;

		Node botNode = new Node(new Vector2(botX, botY));
		Node endNode = new Node(new Vector2(endX, endY));

		aStar(endNode, botNode);

		Vector2 newVel = getNextVelocity(botPosition, toGoTo);

		if (newVel != null) {
			bot.velX = newVel.getX();
			bot.velY = newVel.getY();
		}

		return bot;
	}

	private static Vector2 getNextVelocity(Vector2 from, Vector2 to) {
		Vector2 up = new Vector2(from.getX(), from.getY() + 100);
		Vector2 right = new Vector2(from.getX() + 100, from.getY());
		Vector2 down = new Vector2(from.getX(), from.getY() - 100);
		Vector2 left = new Vector2(from.getX() - 100, from.getY());

		double upDist = up.squareDistance(to);
		double rightDist = right.squareDistance(to);
		double downDist = down.squareDistance(to);
		double leftDist = left.squareDistance(to);

		ArrayList<Double> distanceList = new ArrayList<>(Arrays.asList(upDist, rightDist, downDist, leftDist));

//		  Avoids hitting walls or moving to one's self
		if (isWall(up)) distanceList.set(0, Double.MAX_VALUE);
		if (isWall(right)) distanceList.set(1, Double.MAX_VALUE);
		if (isWall(down)) distanceList.set(2, Double.MAX_VALUE);
		if (isWall(left)) distanceList.set(3, Double.MAX_VALUE);

		switch (findIndexOfSmallest(distanceList)) {
			case 0:
				return up;
			case 1:
				return right;
			case 2:
				return down;
			case 3:
				return left;
		}

//		Never called, returns before this
		return null;
	}

	private ArrayList<Node> aStar(Node Start, Node End) {
		ArrayList<Node> openList = new ArrayList<>();
		ArrayList<Node> closedList = new ArrayList<>();
		ArrayList<Node> pathEndToStart = new ArrayList<>();
		Node q;
		boolean search = true;
		boolean skip = false;
		Comparator<Node> compareByF = (Node n1, Node n2) -> (int) (n1.f - n2.f);

		Start.f = Start.h = Math.abs(Start.position.getX() - End.position.getX()) +
				Math.abs(Start.position.getY() - End.position.getY());

		openList.add(Start);

		while (!openList.isEmpty() && search) {
			openList.sort(compareByF);
			q = openList.remove(0);
			for (Node child : q.children) {
				//region Set successor's parent as q
				if (q.getParent() != null && !openList.isEmpty()) {
					if (q.getParent().f > child.f)
						child.setParent(q);
				} else child.setParent(q);
				//endregion

				//region If successor is the goal, stop search
				if (child.position.equals(End.position)) {
					search = false;
					q = child;
					break;
				}
				//endregion

				//region Calculate successor's g, h and f
				if (child.type.equals("Wall")) {
					child.g = Double.MAX_VALUE;
					child.f = Double.MAX_VALUE;
					child.h = Double.MAX_VALUE;
				} else {
					child.g = q.g + ONE_MOVE;
					child.h = Math.abs(child.position.getX() - End.position.getX()) +
							Math.abs(child.position.getY() - End.position.getY());
					child.f = child.g + child.h;
				}
				//endregion

				//region If a lower f node with successor's position already exists in openList - skip successor
				if (!openList.isEmpty()) {
					for (Node temp : openList) {
						if (temp.position.equals(child.position) && temp.f <= child.f) {
							skip = true;
							break;
						}
					}
				}
				//endregion

				//region If a lower f node with successor's position already exists in closedList - skip successor
				if (!closedList.isEmpty()) {
					for (Node temp : closedList) {
						if (temp.position.equals(child.position) && temp.f <= child.f) {
							skip = true;
							break;
						}
					}
				}
				//endregion

				if (!skip) openList.add(child);
			}

			closedList.add(q);
		}

		q = closedList.remove(closedList.size() - 1);

		while (!q.position.equals(Start.position) && pathEndToStart.size() < 3) {
			q = q.getParent();
			pathEndToStart.add(q);
		}

		return pathEndToStart;
	}

	private static int findIndexOfSmallest(ArrayList<Double> array) {
		int index = 0;
		double smallest = array.get(0);

		for (int i = 1; i < array.size(); i++) {
			if (array.get(i) < smallest) {
				smallest = array.get(i);
				index = i;
			}
		}

		return index;
	}

//	  Checks if the object at 'location' is a wall
	private static boolean isWall(Vector2 location) {
		Rectangle scanArea = new Rectangle(location.getX() - 10, location.getY() - 10, 20, 20);
		for (Bricks wall : MapRender.get_list())
			if (scanArea.intersects(wall.getBounds().getBoundsInLocal()))
				return true;

		return false;
	}
}
