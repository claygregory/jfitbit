package com.claygregory.jfitbit;


/**
 * Number of floors climbed based on Fitbit altimeter (not present on all models). A floor is
 * equal to approximately 10 feet. The daily resolution may not equal exactly 
 * the sum of five minute intervals.
 * 
 * @author Clay Gregory
 *
 */
public class FloorCount extends FitbitInterval {
		
	private int floors;

	public int getFloors( ) {
		return this.floors;
	}
	
	public void setFloors( int floors ) {
		this.floors = floors;
	}
}
