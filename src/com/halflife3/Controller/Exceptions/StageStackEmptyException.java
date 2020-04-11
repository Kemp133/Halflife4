package com.halflife3.Controller.Exceptions;

/**
 * An {@code Exception} used to alert the programmer that the {@code Stages} stack in {@code SceneManager} is empty. Used in the {@code
 * restorePreviousStage()} method to indicate that there's only one stage left on the stack, and hence you can't restore the previous stage
 */
public class StageStackEmptyException extends Exception {
	public StageStackEmptyException(String exception) { super(exception); }
}
