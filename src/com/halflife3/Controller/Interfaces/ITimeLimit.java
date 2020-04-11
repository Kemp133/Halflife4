package com.halflife3.Controller.Interfaces;

/**
 * An interface used to mark a {@code GameMode} as using a time limit. Any game loops can then use this interface to implement time based
 * modes
 */
@FunctionalInterface
public interface ITimeLimit {
	boolean timeLimitReached(double delta);
}