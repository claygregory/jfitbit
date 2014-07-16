package com.claygregory.jfitbit;


/**
 * Fitbit calculated activity score. This defines the level of activity above a baseline of 0
 * when complete sedentary. This is not calculated dependent on height or weight of individual.
 * 
 * According to Fitbit: The Active Score is a rough translation of your average metabolic equivalent of task
 * (METs = Active Score * .001 + 1)
 * 
 * @author Clay Gregory
 *
 */
public class ActivityScore extends FitbitInterval {
		
	private int score;
	
	public int getScore( ) {
		return this.score;
	}
	
	public void setScore( int score ) {
		this.score = score;
	}
}
