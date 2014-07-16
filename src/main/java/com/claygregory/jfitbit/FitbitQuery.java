package com.claygregory.jfitbit;

import java.util.Date;

import org.joda.time.DateTime;

/**
 * Universal parameters for date range and resolution
 * of Fitbit data queries.
 * 
 * Date intervals are [from,to) for ease of paging
 * 
 * @author Clay Gregory
 *
 */
public class FitbitQuery {

	private long from = System.currentTimeMillis( ) - 24 * 60 * 60 * 1000;

	private long to = System.currentTimeMillis( );
	
	private FitbitResolution resolution = FitbitResolution.INTRADAY;

	private FitbitQuery( ) {
		// empty
	}

	public static FitbitQuery create( ) {
		return new FitbitQuery( );
	}
	
	/**
	 * Retrieve data from date, inclusive
	 */
	public FitbitQuery from( DateTime date ) {
		return this.from( date.getMillis( ) );
	}
	
	/**
	 * Retrieve data from date, inclusive
	 */
	public FitbitQuery from( Date date ) {
		return this.from( date.getTime( ) );
	}
	
	/**
	 * Retrieve data from timestamp, inclusive
	 */
	public FitbitQuery from( long date ) {
		this.from = date;
		return this;
	}

	protected DateTime from( ) {
		return new DateTime( this.from );
	}

	/**
	 * Retrieve data up to date, exclusive
	 */
	public FitbitQuery to( DateTime date ) {
		return this.to( date.getMillis( ) );
	}

	/**
	 * Retrieve data up to date, exclusive
	 */
	public FitbitQuery to( Date date ) {
		return this.to( date.getTime( ) );
	}


	/**
	 * Retrieve data up to timestamp, exclusive
	 */
	public FitbitQuery to( long date ) {
		this.to = date;
		return this;
	}
	
	protected DateTime to( ) {
		return new DateTime( this.to );
	}
	
	/**
	 * Retrieves data up to now, exclusive
	 */
	public FitbitQuery toNow( ) {
		return this.to( DateTime.now( ) );
	}
	
	/**
	 * Controls resolution of results, either daily or intraday.
	 */
	public FitbitQuery atResolution( FitbitResolution resolution ) {
		this.resolution = resolution;
		return this;
	}
	
	protected FitbitResolution atResolution( ) {
		return this.resolution;
	}

	/**
	 * @deprecated Replaced with fluent interface via {@link #to()} returning a Date
	 */
	@Deprecated
	public long getMaximumTimestamp( ) {
		return this.to( ).getMillis( );
	}
	
	/**
	 * @deprecated Replaced with fluent interface via {@link #to()}
	 */
	@Deprecated
	public Date getMaximumTimestampAsDate( ) {
		return this.to( ).toDate( );
	}

	/**
	 * @deprecated Replaced with fluent interface via {@link #from()} returning a Date
	 */
	@Deprecated
	public long getMinimumTimestamp( ) {
		return this.from( ).getMillis( );
	}
	
	/**
	 * @deprecated Replaced with fluent interface via {@link #from()}
	 */
	@Deprecated
	public Date getMinimumTimestampAsDate( ) {
		return  this.from( ).toDate( );
	}
	
	/**
	 * @deprecated Replaced with fluent interface via {@link #atResolution()}
	 */
	@Deprecated
	public FitbitResolution getResolution( ) {
		return this.resolution;
	}

	/**
	 * @deprecated Replaced with fluent interface via {@link #to(Date)}
	 */
	@Deprecated
	public FitbitQuery maximumTimestamp( Date maximumTimestampDate ) {
		this.to( maximumTimestampDate );
		return this;
	}
	
	/**
	 * @deprecated Replaced with fluent interface via {@link #to(long)}
	 */
	@Deprecated
	public FitbitQuery maximumTimestamp( long maximumTimestamp ) {
		return this.to( maximumTimestamp );
	}

	/**
	 * @deprecated Replaced with fluent interface via {@link #from(Date)}
	 */
	@Deprecated
	public FitbitQuery minimumTimestamp( Date minimumTimestampDate ) {
		return this.from( minimumTimestampDate );
	}

	/**
	 * @deprecated Replaced with fluent interface via {@link #from(long)}
	 */
	@Deprecated
	public FitbitQuery minimumTimestamp( long minimumTimestamp ) {
		return this.from(  minimumTimestamp );
	}
	
	/**
	 * @deprecated Replaced with fluent interface via {@link #atResolution(FitbitResolution))}
	 */
	@Deprecated
	public FitbitQuery resolution( FitbitResolution resolution ) {
		return this.atResolution( resolution );
	}
}
