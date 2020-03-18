package com.halflife3.Controller.Interfaces;

public interface IGameMode {
	void initialise();
	void gameLoop();
	void finished();
	boolean won();
	boolean lost();
}
