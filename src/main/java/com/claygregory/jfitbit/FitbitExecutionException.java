package com.claygregory.jfitbit;

/**
 * General processing exceptions during Fitbit fetches
 * @author clay
 *
 */
public class FitbitExecutionException extends RuntimeException {

	private static final long serialVersionUID = 8555581489120167250L;

	public FitbitExecutionException( ) {
		super( );
	}
	
	public FitbitExecutionException( Throwable t ) {
		super( t );
	}
}
