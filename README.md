#jFitbit

jFitbit is an unofficial Java Fitbit client. While Fitbit provides an official API for fetching daily summaries,
intraday resolution data is generally unavailable. This client accesses the JSON endpoints backing
the web graphs to pull down intraday data.

**jFitbit has recently migrated to the JSON endpoints ("/getNewGraphData"), from the former XML endpoints – continue reading for changes**

This client currently supports fetching the following data at an intraday resolution:

 * Calorie burn/activity level on a 5-minute interval
 * Floor count on a 5-minute interval (if device supported)
 * Sleep level on a 1-minute interval
 * Step count on a 5-minute interval
 
Beyond the above time series data, jFitbit also provides access to:

  * Tracking device status and information
  * Weight measurements

##Example Usage
```java
Fitbit fb = Fitbit.create( "fitbit-email", "fitbit-password" );

//display sync info to stdout
FitbitTracker tracker = fb.getTracker( );
System.out.println( "Tracker:       " + tracker.getProductName( ) );
System.out.println( "Last sync:     " + tracker.getLastSync( ) );
System.out.println( "Battery level: " + tracker.getBattery( ) );

//Write today's step log to stdout
System.out.println( "\n\nToday's step activity" );
for ( StepCount sc : fb.getStepCount( LocalDate.now( ) ) ) {
    System.out.println( sc.getInterval( ).getStart( ) + " " + sc.getValue( ) );
}
     
//Write today's floor log to stdout
System.out.println( "\n\nToday's floor activity" );
for ( FloorCount fc : fb.getFloorCount( LocalDate.now( ) ) ) {
    System.out.println( fc.getInterval( ).getStart( ) + " " + fc.getValue( ) );
}
    
//Write today's estimated calorie-burn to stdout
System.out.println( "\n\nToday's calorie-burn" );
for ( CalorieBurn cb : fb.getCaloriesBurned( LocalDate.now( ) ) ) {
    System.out.println( cb.getInterval( ).getStart( ) + " " + cb.getValue( ) + "\t" + cb.getActivityLevel( ) );
}

//Write today's sleep session stdout
System.out.println( "\n\nToday's sleep" );
for ( SleepSession ss : fb.getSleepSessions( LocalDate.now( ) ) ) {

    System.out.println( "\nAsleep for " + ss.getDurationAsleep( ).getStandardMinutes( ) + " minutes" );
    System.out.println( "Restless for " + ss.getDurationRestless( ).getStandardMinutes( ) + " minutes" );
    System.out.println( "Awake for " + ss.getDurationAwake( ).getStandardMinutes( ) + " minutes" );
    
    //Write minute-by-minute sleep state over duration of session to stdout
    for ( SleepLevel level : ss.getSleepLevels( ) ) {
        System.out.println( level.getInterval( ).getStart( ) + " " + level.getValue( ) );
    }
}

//Write weight measurements over past 30 days to stdout
System.out.println( "\n\nRecent weight measurements" );
for ( Weight w : fb.getWeights( LocalDate.now( ).minusDays( 30 ), LocalDate.now( ) ) ) {
    System.out.println( w.getDateTime( ) + " " + w.getValue( ) );
}

```

##Notes on Localization

Responses may contain localized strings based on the country selection of the user account.
As a workaround, users experiencing difficulty with their locale may temporarily switch their account to
en_US as follows:

```java
Fitbit fb = Fitbit.create( "fitbit-email", "fitbit-password" );

// Override locale to be en_US for compatibility
fb.enableLocaleOverride( );

//data download and processing

// Restore original user locale upon completion
fb.restoreUserLocale( );
```

###Time Zone

Another issue can result from discrepancies between local time and the time zone selected
in the Fitbit web profile. As the JSON timestamps lack zone information, we assume the local system time matches
that selected zone on the Fitbit account.

If using the mobile app to sync, disabling the option to set time zone automatically is also recommended.
Otherwise you may find data is lost or time shifted when traveling.

##Changes from version 2.x.x

Previously, jFitbit relied on the XML endpoints that backed the Flash-based graphed on fitbit.com. With
the advent of the new online dashboards backed by  ("/getNewGraphData"), jFitbit migrated forward. While the
same basic raw data is available, derived activity and summary data is not as readily present.

Along with a refactor of the data models, daily-resolution queries have been removed; instead this client focuses solely
on providing intraday-resolution data. This release also find new added access to weight measurements and tracker status.

This update isn't plug-and-play with previous jFitbit releases. Presumably you can continue using the 2.x client
if desired, but I have no clue how long Fitbit will continue providing the older XML endpoints ("/getGraphData").

##Dependencies
 * [Apache HttpClient 4.3.x](http://hc.apache.org/)
 * [Gson 2.3.x](https://github.com/google/gson)
 * [Joda-Time 2.8.x](http://www.joda.org/joda-time/)


##Android Compatibility

This project is built against Apache HttpClient 4.3.x. Since the HttpClient packaged with Android is effectively a
fork of HttpClient 4.0,  compatibility is an annoying issue. This project can be used on Android, however, using the
[HttpClient for Android project](https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html) in place of
the above Apache HttpClient dependency.

##License

See the included [LICENSE](LICENSE.md) for rights and limitations under the terms of the MIT license.

##Downloads

Source is hosted at the [jFitbit GitHub repository](https://github.com/claygregory/jfitbit).
