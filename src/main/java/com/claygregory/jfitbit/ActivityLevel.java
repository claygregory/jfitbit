package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

/**
 * 
 * Breakdown of daily activity over 24-hour interval beginning at {@link #getTimestamp}
 * 
 * @author Clay Gregory
 *
 */
public class ActivityLevel implements TimestampedEvent {

	private Duration fairlyActive;
	
	private Duration intervalSize;
	
	private Duration lightlyActive;
	
	private long timestamp;
	
	private Duration veryActive;
	
	public Duration getFairlyActive( ) {
		return this.fairlyActive;
	}

	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public Duration getLightlyActive( ) {
		return this.lightlyActive;
	}

	@Override
	public long getTimestamp( ) {
		return this.timestamp;
	}

	@Override
	public Date getTimestampAsDate( ) {
		return new Date( this.timestamp );
	}

	public Duration getVeryActive( ) {
		return this.veryActive;
	}

	public void setFairlyActive( Duration fairlyActive ) {
		this.fairlyActive = fairlyActive;
	}

	public void setIntervalSize( Duration intervalSize ) {
		this.intervalSize = intervalSize;
	}

	public void setLightlyActive( Duration lightlyActive ) {
		this.lightlyActive = lightlyActive;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

	public void setVeryActive( Duration veryActive ) {
		this.veryActive = veryActive;
	}

}
