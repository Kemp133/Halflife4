package com.halflife3.Controller.Interfaces;

/**
 * This interface is used to provide implementation for the common methods used by a controller. This can then be used by other classes to
 * call required methods
 */
public interface IController {
	void initialise();
	void start();
	void end();
	void run();
}