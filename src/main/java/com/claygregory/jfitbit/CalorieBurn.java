package com.claygregory.jfitbit;


/**
 * Calories burned and activity level over the given interval.
 * 
 * @author Clay Gregory
 *
 */
public class CalorieBurn extends ActivityValue<Integer> {

	private String activityLevel;
		
	public String getActivityLevel( ) {
		return activityLevel;
	}

	public void setActivityLevel( String activityLevel ) {
		this.activityLevel = activityLevel;
	}
}
