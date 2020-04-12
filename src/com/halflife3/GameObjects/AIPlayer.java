package com.halflife3.GameObjects;

import com.halflife3.Controller.ClientController;
import com.halflife3.GameModes.MainMode;
import com.halflife3.Networking.Server.Server;
import javafx.scene.canvas.*;

public class AIPlayer extends Controllable {
	public final float RELOAD_TIME = MainMode.NORMAL_RELOAD_DURATION * Server.SERVER_FPS / ClientController.FPS;

	public  float   reload = RELOAD_TIME;
	private boolean active;
	private boolean alreadyLooking;
	private Vector2 soughtPos;
	private Vector2 nextPos;

	public AIPlayer(Vector2 position) {
		super(position);
		keys.add("AI");
	}

	@Override
	public void render(GraphicsContext gc) {}

	@Override
	public void update(double time) {
		if (reload < RELOAD_TIME)
			reload++;
		if (stunned <= Server.SERVER_FPS / 2f && stunned != 0) {
			velocity.setX(0);
			velocity.setY(0);
			deceleration.setX(0);
			deceleration.setY(0);
			stunned--;
		} else {
			if (velocity.getX() * new Vector2(velocity).subtract(deceleration).getX() > 0)
				velocity.subtract(deceleration);
			position.add(new Vector2(velocity).multiply(time));
			circle.setCenterX(position.getX() + getWidth() / 2 + 1);
			circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			if (stunned > 0)
				stunned--;
		}
	}

	public boolean inactive() {
		return !active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAlreadyLooking() {
		return alreadyLooking;
	}

	public void setAlreadyLooking(boolean alreadyLooking) {
		this.alreadyLooking = alreadyLooking;
	}

	public Vector2 getSoughtPos() {
		return (soughtPos == null) ? null : new Vector2(soughtPos);
	}

	public void setSoughtPos(Vector2 soughtPos) {
		this.soughtPos = soughtPos;
	}

	public Vector2 getNextPos() {
		return (nextPos == null) ? null : new Vector2(nextPos);
	}

	public void setNextPos(Vector2 nextPos) {
		this.nextPos = nextPos;
	}

	public void reset() {
		soughtPos      = null;
		nextPos        = null;
		alreadyLooking = false;
		resetBasics();
	}
}
