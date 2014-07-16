package com.claygregory.jfitbit;

import org.joda.time.Duration;

/**
 * 
 * Breakdown of daily activity over 24-hour interval beginning at {@link #getTimestamp}
 * 
 * @author Clay Gregory
 *
 */
public class ActivityLevel extends FitbitInterval {

	private Duration fairlyActive;
	
	private Duration lightlyActive;
	
	private Duration veryActive;
	
	public Duration getFairlyActive( ) {
		return this.fairlyActive;
	}

	public Duration getLightlyActive( ) {
		return this.lightlyActive;
	}

	public Duration getVeryActive( ) {
		return this.veryActive;
	}

	public void setFairlyActive( Duration fairlyActive ) {
		this.fairlyActive = fairlyActive;
	}

	public void setLightlyActive( Duration lightlyActive ) {
		this.lightlyActive = lightlyActive;
	}

	public void setVeryActive( Duration veryActive ) {
		this.veryActive = veryActive;
	}

}
