package com.claygregory.jfitbit;

import java.util.Date;

import org.joda.time.Interval;

public abstract class FitbitInterval {

	private Interval interval;

	public Interval getInterval( ) {
		return this.interval;
	}

	public void setInterval( Interval interval ) {
		this.interval = interval;
	}
	
	public long getTimestamp( ) {
		return this.interval.getStartMillis( );
	}

	public Date getTimestampAsDate( ) {
		return this.interval.getStart( ).toDate( );
	}
}
