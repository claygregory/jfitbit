#jFitbit

jFitbit is an unofficial Java Fitbit client. While Fitbit provides an API for fetching daily totals, intraday resolution data is unavailable. Fortunately, the XML used to build the Flash-based graphs on the Fitbit.com site provides simple access to your data.

This client currently supports fetching the following data at an intraday resolution:
 * Activity score on a 5-minute interval
 * Sleep level on a 1-minute interval
 * Step count on a 5-minute interval

And the following are also available as a daily resolution:
 * Activity level 
 * Activity score
 * Step count

##Dependencies
 * [Apache HttpClient 4.2](http://hc.apache.org/)
 * [cg-jcommons](https://github.com/claygregory/cg-jcommons)

##Downloads

Source is hosted at the [jFitbit GitHub repository](https://github.com/claygregory/jfitbit). Downloads are also available on the [GitHub project's Downloads section] (https://github.com/claygregory/jfitbit/downloads)
