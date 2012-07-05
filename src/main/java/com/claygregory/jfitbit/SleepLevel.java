package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

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
public class SleepLevel implements TimestampedEvent {
		
	private long timestamp;
	
	private Duration intervalSize;
	
	private int level;
	
	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public int getLevel( ) {
		return this.level;
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

	public void setLevel( int level ) {
		this.level = level;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

}
