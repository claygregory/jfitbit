package com.claygregory.jfitbit;

import java.util.Date;

import com.claygregory.common.data.Duration;
import com.claygregory.common.data.TimestampedEvent;

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
public class ActivityScore implements TimestampedEvent {

	private long timestamp;
	
	private Duration intervalSize;
	
	private int score;
	
	public Duration getIntervalSize( ) {
		return this.intervalSize;
	}

	public int getScore( ) {
		return this.score;
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

	public void setScore( int score ) {
		this.score = score;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

}
