package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

/**
 * Number of steps taken over the interval beginning at specified timestamp.
 * The daily resolution may not equal exactly the sum of five minute intervals.
 * 
 * @author Clay Gregory
 *
 */
public class StepCount implements TimestampedEvent {

	private long timestamp;
	
	private Duration intervalSize;
	
	private int steps;
	
	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public int getSteps( ) {
		return this.steps;
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

	public void setSteps( int steps ) {
		this.steps = steps;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

}
