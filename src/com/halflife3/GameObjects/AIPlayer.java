package com.halflife3.GameObjects;

import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.Networking.Packets.PositionPacket;
import javafx.scene.canvas.*;
import javafx.scene.shape.*;

public class AIPlayer extends Controllable {
	public  Circle         circle;
	public  float          stunned    = 0;
	private Vector2        originalPosition;
	private PositionPacket packetToSend;

	public AIPlayer(Vector2 position) {
		super(position);
	}

	@Override
	public Circle getBounds() {
		return circle;
	}

	@Override
	public void render(GraphicsContext gc) {}

	@Override
	public void update(double time) {
		if (stunned <= MainMode.STUN_DURATION && stunned != 0) {
			velocity.setX(0);
			velocity.setY(0);
			deceleration.setX(0);
			deceleration.setY(0);
			stunned--;
		} else {
			orgPos = new Vector2(position);
			Vector2 nextVel = new Vector2(velocity);
			if (velocity.getX() * nextVel.subtract(deceleration).getX() > 0) velocity.subtract(deceleration);
			position.add(new Vector2(velocity).multiply(time));
			isMoving = !orgPos.equals(position);
			circle.setCenterX(position.getX() + getWidth() / 2 + 1);
			circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			if (stunned > 0) stunned--;
		}
	}
}
