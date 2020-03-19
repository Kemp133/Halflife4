package com.halflife3.Model;

import com.halflife3.Networking.Packets.PositionPacket;
import com.halflife3.Networking.Server.ClientListServer;
import com.halflife3.View.MapRender;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class AI {

	public static PositionPacket getBotMovement(PositionPacket bot) {
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

		Vector2 newVel = getNextVelocity(botPosition, toGoTo);

		if (newVel != null) {
			bot.velX = newVel.getX();
			bot.velY = newVel.getY();
		}

		return bot;
	}

//    TODO: Write A* for AI, make it return a Vector2 velocity value
	private static Vector2 getNextVelocity(Vector2 from, Vector2 to) {
		Vector2 up = new Vector2(0, 100);
		Vector2 right = new Vector2(100, 0);
		Vector2 down = new Vector2(0, -100);
		Vector2 left = new Vector2(-100, 0);

		double upDist = from.squareDistance(to);
		double rightDist = from.squareDistance(to);
		double downDist = from.squareDistance(to);
		double leftDist = from.squareDistance(to);

		ArrayList<Double> distanceList = new ArrayList<>(Arrays.asList(upDist, rightDist, downDist, leftDist));

//		  Avoids hitting walls or moving to one's self
		if (isWall(up, MapRender.get_list()) || upDist == 0) distanceList.set(0, Double.MAX_VALUE);
		if (isWall(right, MapRender.get_list()) || rightDist == 0) distanceList.set(1, Double.MAX_VALUE);
		if (isWall(down, MapRender.get_list()) || downDist == 0) distanceList.set(2, Double.MAX_VALUE);
		if (isWall(left, MapRender.get_list()) || leftDist == 0) distanceList.set(3, Double.MAX_VALUE);

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
	private static boolean isWall(Vector2 location, Deque<Bricks> listOfWalls) {
		Rectangle scanArea = new Rectangle(location.getX() - 10, location.getY() - 10, 20, 20);
		for (Bricks wall : listOfWalls)
			if (scanArea.intersects(wall.getBounds().getBoundsInLocal()))
				return true;

		return false;
	}
}
