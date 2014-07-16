package com.claygregory.jfitbit;


/**
 * Calories consumed over a given interval.
 * 
 * @author Clay Gregory
 *
 */
public class CalorieCount extends FitbitInterval {
		
	private int calories;

	public int getCalories( ) {
		return this.calories;
	}

	public void setCalories( int calories ) {
		this.calories = calories;
	}
}
