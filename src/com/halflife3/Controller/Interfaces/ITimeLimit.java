package com.halflife3.Controller.Interfaces;

@FunctionalInterface
public interface ITimeLimit {
	boolean timeLimitReached(double delta);
}
