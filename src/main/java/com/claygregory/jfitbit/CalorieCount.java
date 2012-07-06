package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

/**
 * Calories consumed over a given interval.
 * 
 * @author Clay Gregory
 *
 */
public class CalorieCount implements TimestampedEvent {

	private long timestamp;
	
	private Duration intervalSize;
	
	private int calories;
	
	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public int getCalories( ) {
		return this.calories;
	}

	@Override
	public long getTimestamp( ) {
		return this.timestamp;
	}

	@Override
	public Date getTimestampAsDate( ) {
		return new Date( this.timestamp );
	}

	public void setIntervalSize( Duration intervalSize ) {
		this.intervalSize = intervalSize;
	}

	public void setCalories( int calories ) {
		this.calories = calories;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

}
