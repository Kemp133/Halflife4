package com.halflife3.Controller.GameModes;

public abstract class GameMode {
	protected String GameModeName;
	protected boolean CanRespawn;

	public GameMode(String GameModeName) {
		this.GameModeName = GameModeName;
	}

	abstract void initialise();
	abstract void gameLoop();
	abstract void finished();
	abstract boolean won();
	abstract boolean lost();

	//region Get/Set GameModeName
	public String getGameModeName() { return GameModeName; }
	public void setGameModeName(String gameModeName) { GameModeName = gameModeName; }
	//endregion

	//region Get/Set CanRespawn
	public boolean getCanRespawn () { return CanRespawn; }
	public void setCanRespawn (boolean canRespawn) { CanRespawn = canRespawn; }
	//endregion
}
