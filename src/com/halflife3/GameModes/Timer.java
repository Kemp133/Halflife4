package com.halflife3.GameModes;

public class Timer {
	private double        timeTo;
	private double        currentTime;
	private TimeDirection timeDirection;
	private boolean       hasFinished;

	public static final int MINUTE = 60;

	public enum TimeDirection {
		UP,
		DOWN
	}

	public Timer (double timeToSet, TimeDirection timeDirection) {
		this.timeDirection = timeDirection;

		if (timeDirection.equals(TimeDirection.UP)) timeTo = timeToSet;
		else currentTime = timeToSet;
	}

	//region Get/Set Time
	public double getTimeTo () { return timeTo; }

	public void setTimeTo (double timeTo) { this.timeTo = timeTo; }
	//endregion

	//region Get/Set Time Direction
	public TimeDirection getTimeDirection () { return timeDirection; }

	public void setTimeDirection (TimeDirection timeDirection) { this.timeDirection = timeDirection; }
	//endregion

	//region Add/Subtract time
	public void addTime (double time) { this.timeTo += time; }

	public void removeTime (double time) { this.timeTo -= time; }
	//endregion

	//region Get/Set hasFinished
	public boolean getHasFinished () { return hasFinished; }
	public void setHasFinished (boolean hasFinished) { this.hasFinished = hasFinished; }
	//endregion

	//region Update Timer
	public void UpdateTime (double timeElapsed) {
		timeTo += (timeDirection.equals(TimeDirection.UP)) ? timeElapsed : -timeElapsed;
		hasFinished = (timeDirection.equals(TimeDirection.UP)) ? currentTime >= timeTo : currentTime >= 0;
	}
	//endregion
}
