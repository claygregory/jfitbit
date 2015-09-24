package com.claygregory.jfitbit;

import org.joda.time.DateTime;

public class Weight {

	private DateTime dateTime;
	
	private float value;

	public DateTime getDateTime( ) {
		return dateTime;
	}

	public float getValue( ) {
		return value;
	}

	public void setDateTime( DateTime dateTime ) {
		this.dateTime = dateTime;
	}

	public void setValue( float value ) {
		this.value = value;
	}
}
