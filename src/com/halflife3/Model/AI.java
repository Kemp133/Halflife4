package com.halflife3.Model;

import com.halflife3.View.MapRender;
import javafx.scene.shape.Rectangle;

import java.util.Deque;

public abstract class AI extends Player {

	public AI(Vector2 position, Vector2 velocity) {
		super(position, velocity);
	}

//    Returns vector of direction to move next to (PX_TO_MOVE pixels)
	public Vector2 getNextMove(Vector2 original, Vector2 position) {
		if (original == position)
			return position;

		final int PX_TO_MOVE = 20;

		Vector2 up = new Vector2(original.getX(), original.getY() + PX_TO_MOVE);
		Vector2 right = new Vector2(original.getX() + PX_TO_MOVE, original.getY());
		Vector2 down = new Vector2(original.getX(), original.getY() - PX_TO_MOVE);
		Vector2 left = new Vector2(original.getX() - PX_TO_MOVE, original.getY());

		double upDist = up.squareDistance(position);
		double rightDist = right.squareDistance(position);
		double downDist = down.squareDistance(position);
		double leftDist = left.squareDistance(position);

		double[] distances = {upDist, rightDist, downDist, leftDist};

//		  Avoids hitting walls or moving to one's self
		if (isWall(up, MapRender.get_list()) || upDist == 0) distances[0] = Integer.MAX_VALUE;
		if (isWall(right, MapRender.get_list()) || rightDist == 0) distances[1] = Integer.MAX_VALUE;
		if (isWall(down, MapRender.get_list()) || downDist == 0) distances[2] = Integer.MAX_VALUE;
		if (isWall(left, MapRender.get_list()) || leftDist == 0) distances[3] = Integer.MAX_VALUE;

		switch (findIndexOfSmallest(distances)) {
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

	public static int findIndexOfSmallest(double[] array) {
		int index = 0;
		double smallest = array[0];

		for (int i = 1; i < array.length; i++) {
			if (array[i] < smallest) {
				smallest = array[i];
				index = i;
			}
		}

		return index;
	}

	public Vector2 closestPlayerPosition(Player[] playerList) {

		double[] distances =
				{getDistance(playerList[0]),
				getDistance(playerList[1]),
				getDistance(playerList[2]),
				getDistance(playerList[3])};

		int closest = findIndexOfSmallest(distances);

		return playerList[closest].getPosition();
	}

//	  Checks if the object at 'location' is a wall
	public boolean isWall(Vector2 location, Deque<Bricks> listOfWalls) {
		Rectangle scanArea = new Rectangle(location.getX() - 10, location.getY() - 10, 20, 20);
		for (Bricks wall : listOfWalls)
			if (scanArea.intersects(wall.getBounds().getBoundsInLocal()))
				return true;

		return false;
	}

	//TODO: overlapping hitbox means damage, if not, move to player
	public abstract void attackPattern(Player[] playerList);
}
