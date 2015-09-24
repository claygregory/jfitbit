package com.claygregory.jfitbit;

import java.util.Collections;
import java.util.List;

import org.joda.time.Duration;

/**
 * Level of sleep as reflected in Fitbit dashboard.
 * 
 * Levels are rough interpretable as
 * 	1: asleep
 *  2: restless
 *  3: awake/active
 * 
 * @author Clay Gregory
 *
 */
public class SleepSession extends FitbitInterval {
	
	public static class SleepLevel extends ActivityValue<Integer>{ };
	
	public static int SLEEP_LEVEL_ASLEEP = 1;
	
	public static int SLEEP_LEVEL_RESTLESS = 2;
	
	public static int SLEEP_LEVEL_AWAKE = 3;
		
	private List<SleepLevel> sleepLevels = Collections.emptyList( );

	public Duration getDurationAsleep( ) {
		return getDurationAtLevel( SLEEP_LEVEL_ASLEEP );
	}

	public Duration getDurationAwake( ) {
		return getDurationAtLevel( SLEEP_LEVEL_AWAKE );
	}
	
	public Duration getDurationInBed( ) {
		return this.getInterval( ).toDuration( );
	}
	
	public Duration getDurationRestless( ) {
		return getDurationAtLevel( SLEEP_LEVEL_RESTLESS );
	}
	
	public List<SleepLevel> getSleepLevels( ) {
		return this.sleepLevels;
	}
	
	public void setSleepLevels( List<SleepLevel> sleepLevels ) {
		this.sleepLevels = sleepLevels;
	}
	
	protected Duration getDurationAtLevel( int sleelLevel ) {
		Duration duration = new Duration( 0 );
		for ( SleepLevel level : this.getSleepLevels( ) ) {
			if ( level.getValue( ) == sleelLevel )
				duration = duration.plus( level.getInterval( ).toDuration( ) );
		}
		return duration;
	}
}
