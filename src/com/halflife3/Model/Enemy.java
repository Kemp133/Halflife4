package com.halflife3.Model;

import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.Rectangle;

public abstract class Enemy extends Sprite {
	private int health;

	public Enemy(Vector2 position, Vector2 velocity, int health) {
		super(position, velocity);
		this.health = health;
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(position.getX(), position.getY(), getWidth(), getHeight());
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(), position.getY() - Camera.GetOffset().getY());
	}

	@Override
	public void update(double time) {
		position = position.add(velocity.multiply(time));
	}

	public void addVelocity(Vector2 toAdd) {
		velocity = velocity.add(toAdd);
	}

	public void moveTo(Vector2 position) {
		//step 1: find shortest path without walls
		//step 2: move to target
		this.position = getNextMove(this.position, position);
	}

	public void moveTo(GameObject entity) {
		//step 1: find shortest path without walls
		//step 2: move to target
		this.position = getNextMove(this.getPosition(), entity.getPosition());
	}

	//this method gets the path between two positions
	public Vector2 getNextMove(Vector2 original, Vector2 position) {

		if (original == position) {
			return position;
		}

		Vector2 up = new Vector2(original.getX(), original.getY() + 20);
		Vector2 right = new Vector2(original.getX() + 20, original.getY());
		Vector2 down = new Vector2(original.getX(), original.getY() - 20);
		Vector2 left = new Vector2(original.getX() - 20, original.getY());

		double udis = up.squareDistance(position);
		double rdis = right.squareDistance(position);
		double ddis = down.squareDistance(position);
		double ldis = left.squareDistance(position);

		double[] shortest = {udis, rdis, ddis, ldis};

		if (isWall(up)) shortest[0] += 1000000;
		if (isWall(right)) shortest[1] += 1000000;
		if (isWall(down)) shortest[2] += 1000000;
		if (isWall(left)) shortest[3] += 1000000;

		int closestRoute = FindSmallest(shortest);

		Vector2 chosen = null;
		switch (closestRoute) {
			case 0:
				chosen = up;
				break;
			case 1:
				chosen = right;
				break;
			case 2:
				chosen = down;
				break;
			case 3:
				chosen = left;
				break;
		}

		return chosen;
	}

	public static int FindSmallest(double[] arr1) {
		int index = 0;
		double min = arr1[index];

		for (int i = 1; i < arr1.length; i++) {
			if (arr1[i] < min) {
				min = arr1[i];
				index = i;
			}
		}
		return index;
	}

	//TODO: Rewrite the code to find the Vector2 position of the closest Player
	public Vector2 closestPlayerPosition(Player[] playerList) {

		double[] distance = {getDistance(playerList[0]), getDistance(playerList[1]), getDistance(playerList[2]), getDistance(playerList[3])};

		int closet = FindSmallest(distance);

		return playerList[closet].getPosition();
	}

	//TODO: create a method that checks for walls
	public boolean isWall(Vector2 location) {
		return false;
	}

	//TODO: need to move to specific location, also avoiding the obstacle

	//TODO: add a death animation
	public void death() {
		if (health == 0) {
			destroy();
		}
	}

	//TODO: overlapping hitbox means damage, if not, move to player
	public abstract void attackPattern(Player[] playerList);

	//region Health Getter and Setter
	public void setHealth(int health) {
		this.health = health;
	}
	public int getHealth() {
		return health;
	}
	//endregion
}
