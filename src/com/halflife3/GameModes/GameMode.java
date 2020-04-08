package com.halflife3.GameModes;

public abstract class GameMode {
	protected String GameModeName;
	public boolean hasFinished = false;
	public boolean win = false;

	public GameMode(String GameModeName) { this.GameModeName = GameModeName; }
	public abstract void initialise();
	public abstract void gameLoop(double elapsedTime);
	public abstract void finished();
	public abstract boolean won();
	public abstract boolean lost();

	//region Get/Set GameModeName
	public String getGameModeName() { return GameModeName; }
	public void setGameModeName(String gameModeName) { GameModeName = gameModeName; }
	//endregion
}
