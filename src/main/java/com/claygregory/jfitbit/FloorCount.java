package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

/**
 * Number of floors climbed based on Fitbit Ultra altimeter. A floor is
 * equal to approximately 10 feet. The daily resolution may not equal exactly 
 * the sum of five minute intervals.
 * 
 * @author Clay Gregory
 *
 */
public class FloorCount implements TimestampedEvent {

	private long timestamp;
	
	private Duration intervalSize;
	
	private int floors;
	
	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public int getFloors( ) {
		return this.floors;
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

	public void setFloors( int floors ) {
		this.floors = floors;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

}
