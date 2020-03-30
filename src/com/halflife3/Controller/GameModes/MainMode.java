package com.halflife3.Controller.GameModes;

import com.halflife3.Controller.Interfaces.ITimeLimit;

import java.util.HashMap;

public class MainMode extends GameMode implements ITimeLimit {
	protected Timer  timer;
	protected double score;

	protected HashMap<Integer, String> teams;

	//Initialiser
	{
		timer = new Timer(5 * Timer.MINUTE, Timer.TimeDirection.DOWN);
		teams = new HashMap<>();
	}

	public MainMode (String GameModeName, boolean CanRespawn, double score) {
		super(GameModeName);
	}

	@Override
	void initialise () {

	}

	@Override
	void gameLoop () {

	}

	@Override
	void finished () {

	}

	@Override
	boolean won () {
		return false;
	}

	@Override
	boolean lost () {
		return false;
	}

	@Override
	public boolean timeLimitReached (double delta) {
		return false;
	}

	//region Get/Set Score
	public double getScore () { return score; }

	public void setScore (double score) { this.score = score; }
	//endregion
}
