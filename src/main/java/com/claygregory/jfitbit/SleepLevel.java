package com.claygregory.jfitbit;


/**
 * Level of sleep as reflected in Fitbit dashboard.
 * 
 * Levels are rough interpretable as
 * 	1: asleep
 *  2: awake
 *  3: awake and active
 * 
 * @author Clay Gregory
 *
 */
public class SleepLevel extends FitbitInterval {
		
	private int level;

	public int getLevel( ) {
		return this.level;
	}

	public void setLevel( int level ) {
		this.level = level;
	}
}
