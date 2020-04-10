package com.halflife3.Mechanics.PowerUps;

import com.halflife3.GameObjects.Sprite;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.Camera;
import javafx.scene.canvas.GraphicsContext;

public class FastReload extends Sprite {

	public FastReload(Vector2 position, String image) {
		super(position, new Vector2(), image);
		keys.add("reload");
	}

	@Override
	public void render(GraphicsContext gc) {
		gc.drawImage(sprite, position.getX() - Camera.GetOffset().getX(), position.getY() - Camera.GetOffset().getY());
	}

	@Override
	public void update(double time) {

	}
}
