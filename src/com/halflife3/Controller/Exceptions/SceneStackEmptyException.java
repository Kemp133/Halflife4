package com.halflife3.Controller.Exceptions;

/**
 * An {@code Exception} used to alert the programmer that the {@code Scenes} stack in {@code SceneManager} is empty. Used in the {@code
 * restorePreviousScene()} method to indicate that there's only one scene left on the stack, and hence you can't restore the previous scene
 */
public class SceneStackEmptyException extends Exception {
	public SceneStackEmptyException(String exception) { super(exception); }
}
