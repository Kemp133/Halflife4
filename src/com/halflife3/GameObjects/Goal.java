package com.halflife3.GameObjects;

import com.halflife3.GameObjects.Interfaces.ICollidable;
import com.halflife3.Mechanics.Vector2;
import com.halflife3.View.MapRender;
import javafx.scene.shape.*;

public class Goal implements ICollidable {
	private Rectangle rectangle;
	private char      scoringTeam;
	private Vector2   position;

	public Goal(int x, int y) {
		rectangle   = new Rectangle(x, y, MapRender.BLOCK_SIZE, MapRender.BLOCK_SIZE);
		scoringTeam = (x < MapRender.mapWidth / 2) ? 'R' : 'L';
		position = new Vector2(x, y);
	}

	@Override
	public Rectangle getBounds() {
		return rectangle;
	}

	public char getScoringTeam() {
		return scoringTeam;
	}

//	public void setTeam(char team) { this.team = team; } //Could be used to set team goals in the future


	public Vector2 getPosition() {
		return position;
	}
}
