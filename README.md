#jFitbit

jFitbit is an unofficial Java Fitbit client. While Fitbit provides an API for fetching daily totals, intraday resolution data is unavailable. However, the XML served for building the Flash-based graphs on the Fitbit.com site provides the interface to your data used in this client.

This client currently supports fetching the following data at an intraday resolution:
 * Activity score on a 5-minute interval
 * Floor count on a 5-minute interval (if device supported)
 * Sleep level on a 1-minute interval
 * Step count on a 5-minute interval

And the following are also available at a daily resolution:
 * Activity level 
 * Activity score
 * Calories consumed
 * Floor count
 * Step count

##Example Usage
```
Fitbit fb = Fitbit.create( "fitbit-email", "fitbit-password" );
  	
FitbitQuery fbQuery = FitbitQuery.create( )
  .from( DateTime.now( ).minusDays( 1 ) )
  .toNow( );
		
for ( StepCount s : fb.stepCount( fbQuery ) )
  System.out.println( s.getInterval( ).getStart( ) + " " + s.getSteps( ) );
```

##Dependencies
 * [Apache HttpClient 4.3.2](http://hc.apache.org/)
 * [Joda-Time 2.3](http://www.joda.org/joda-time/)

##Downloads

Source is hosted at the [jFitbit GitHub repository](https://github.com/claygregory/jfitbit). Downloads are also available on the [GitHub project's Downloads section] (https://github.com/claygregory/jfitbit/downloads)
