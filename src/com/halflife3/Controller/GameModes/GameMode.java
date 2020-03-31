package com.halflife3.Controller.GameModes;

public abstract class GameMode {
	protected String GameModeName;

	public GameMode(String GameModeName) { this.GameModeName = GameModeName; }
	abstract void initialise();
	abstract void gameLoop();
	abstract void finished();
	abstract boolean won(int team);
	abstract boolean lost();
	public void runGame() {
		initialise();
		gameLoop();
		finished();
	}

	//region Get/Set GameModeName
	public String getGameModeName() { return GameModeName; }
	public void setGameModeName(String gameModeName) { GameModeName = gameModeName; }
	//endregion
}
