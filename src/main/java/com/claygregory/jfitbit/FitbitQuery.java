package com.claygregory.jfitbit;

import java.util.Date;

/**
 * 
 * @author Clay Gregory
 *
 */
public class FitbitQuery {

	private long minimumTimestamp = System.currentTimeMillis( ) - 24 * 60 * 60 * 1000;

	private long maximumTimestamp = System.currentTimeMillis( );
	
	private FitbitResolution resolution = FitbitResolution.INTRADAY;

	private FitbitQuery( ) {
		// empty
	}

	public static FitbitQuery create( ) {
		return new FitbitQuery( );
	}
	
	public long getMaximumTimestamp( ) {
		return this.maximumTimestamp;
	}

	public Date getMaximumTimestampAsDate( ) {
		return new Date( this.maximumTimestamp );
	}
	
	public long getMinimumTimestamp( ) {
		return this.minimumTimestamp;
	}

	public Date getMinimumTimestampAsDate( ) {
		return new Date( this.minimumTimestamp );
	}
	
	public FitbitResolution getResolution( ) {
		return this.resolution;
	}

	public FitbitQuery maximumTimestamp( Date maximumTimestampDate ) {
		this.maximumTimestamp = maximumTimestampDate.getTime( );
		return this;
	}

	public FitbitQuery maximumTimestamp( long maximumTimestamp ) {
		this.maximumTimestamp = maximumTimestamp;
		return this;
	}

	public FitbitQuery minimumTimestamp( Date minimumTimestampDate ) {
		this.minimumTimestamp = minimumTimestampDate.getTime( );
		return this;
	}

	public FitbitQuery minimumTimestamp( long minimumTimestamp ) {
		this.minimumTimestamp = minimumTimestamp;
		return this;
	}
	
	public FitbitQuery resolution( FitbitResolution resolution ) {
		this.resolution = resolution;
		return this;
	}
}
