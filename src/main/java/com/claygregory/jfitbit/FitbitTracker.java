package com.claygregory.jfitbit;

import org.joda.time.DateTime;

public class FitbitTracker {

	private String id;
	
	private String type;
	
	private String battery;
	
	private DateTime lastSync;
	
	private String productName;

	public String getBattery( ) {
		return battery;
	}

	public String getId( ) {
		return id;
	}

	public DateTime getLastSync( ) {
		return lastSync;
	}

	public String getProductName( ) {
		return productName;
	}

	public String getType( ) {
		return type;
	}

	public void setBattery( String battery ) {
		this.battery = battery;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public void setLastSync( DateTime lastSync ) {
		this.lastSync = lastSync;
	}

	public void setProductName( String productName ) {
		this.productName = productName;
	}

	public void setType( String type ) {
		this.type = type;
	}
	
}
