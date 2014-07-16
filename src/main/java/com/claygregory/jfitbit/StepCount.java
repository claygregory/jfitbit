package com.claygregory.jfitbit;


/**
 * Number of steps taken over the interval beginning at specified timestamp.
 * The daily resolution may not equal exactly the sum of five minute intervals.
 * 
 * @author Clay Gregory
 *
 */
public class StepCount extends FitbitInterval {
	
	private int steps;

	public int getSteps( ) {
		return this.steps;
	}

	public void setSteps( int steps ) {
		this.steps = steps;
	}
}
