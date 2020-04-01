package com.halflife3.Controller.Interfaces;

import com.halflife3.Controller.SceneManager;

public interface IController {
	void initialise(SceneManager manager);
	void start();
	void end();
	void run(SceneManager manager);
}
