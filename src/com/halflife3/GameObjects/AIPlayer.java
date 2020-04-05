package com.halflife3.GameObjects;

import com.halflife3.Controller.GameModes.MainMode;
import com.halflife3.Mechanics.Vector2;
import javafx.scene.canvas.*;

public class AIPlayer extends Controllable {

	public AIPlayer(Vector2 position) {
		super(position);
		keys.add("AI");
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
			Vector2 nextVel = new Vector2(velocity);
			if (velocity.getX() * nextVel.subtract(deceleration).getX() > 0)
				velocity.subtract(deceleration);
			position.add(new Vector2(velocity).multiply(time));
			circle.setCenterX(position.getX() + getWidth() / 2 + 1);
			circle.setCenterY(position.getY() + getHeight() / 2 + 1);
			if (stunned > 0)
				stunned--;
		}
	}

}
