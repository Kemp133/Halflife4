package com.halflife3.Controller.Exceptions;

/** An {@code Exception} used to indicate that a {@code scene} doesn't exist in the {@code SceneManager} with the given label */
public class SceneDoesNotExistException extends Exception {
	public SceneDoesNotExistException(String message) { super(message); }
}