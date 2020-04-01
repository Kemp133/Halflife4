package com.halflife3.Controller.GameModes;

import com.halflife3.Controller.Interfaces.ITimeLimit;

import java.util.HashMap;

public class MainMode extends GameMode implements ITimeLimit {
	protected Timer  timer;
	protected double scoreLimit;
	protected HashMap<Integer, Double> scores;

	protected HashMap<Integer, String> teams;

	//Initialiser
	{
		timer = new Timer(5 * Timer.MINUTE, Timer.TimeDirection.DOWN);
		teams = new HashMap<>();
	}

	public MainMode (String GameModeName, double score) {
		super(GameModeName);
//		this.CanRespawn = CanRespawn;
		this.scoreLimit = score;
	}

	@Override
	void initialise () {

	}

	@Override
	void gameLoop () {

	}

	@Override
	void finished () {
		//Send packet to end the game
		//Set up game to await for the won/lost packet
	}

	@Override
	boolean won (int team) {
		//Send team with id of @team the won packet
		//Send everybody else the lost packet

		return false;
	}

	@Override
	boolean lost () {
		return false;
	}

	@Override
	public boolean timeLimitReached (double delta) {
		return timer.getHasFinished();
	}

	//region Get/Set Scores
	public double getTeamScore (int team) { return scores.get(team); }

	public void setTeamScore (int team, double score) { this.scores.put(team, score); }
	//endregion
}
