package com.claygregory.jfitbit;


/**
 * A numeric activity value such as floors climbed or steps taken
 * over a given time interval
 * 
 * @author Clay Gregory
 *
 */
public class ActivityValue<V extends Number> extends FitbitInterval {
		
	private V value;

	public V getValue( ) {
		return this.value;
	}
	
	public void setValue( V value ) {
		this.value = value;
	}
}
