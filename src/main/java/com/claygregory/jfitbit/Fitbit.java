package com.claygregory.jfitbit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.claygregory.jfitbit.SleepSession.SleepLevel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Unofficial Fitbit client for retrieving intraday Fitbit data as presented on Fitbit.com.
 * 
 * <p>Note: Fitbit does not provide timezone information on timestamps,
 * so it's assumed the timezone preference set in the Fitbit user profile matches timezone
 * settings in the local environment.</p>
 * 
 * @author Clay Gregory
 *
 */
public class Fitbit {
	
	protected static abstract class ActivityResponseHandler extends ResponseHandler {
		
		/**
		* Receives activity data points from response
		* 
		* @param interval parsed from description
		* @param dataPoint JSON data point at interval
		*/
		protected void processDataPoint( Interval interval, JsonObject dataPoint ) {
		//optional to implement, default noop
		}
		
		protected void processResponse( String json ) {
			
			JsonParser parser = new JsonParser( );
			
			JsonArray dataPoints = parser.parse( json ).getAsJsonObject( )
				.get( "graph" ).getAsJsonObject( )
				.get( "dataSets" ).getAsJsonObject( )
				.get( "activity" ).getAsJsonObject( )
				.get( "dataPoints" ).getAsJsonArray( );
			
			int intervalSize = computeIntervalSize( dataPoints );

			for ( JsonElement dataPoint : dataPoints ) {
				
				JsonObject dataPointObject = dataPoint.getAsJsonObject( );
				String dateTime = dataPointObject.get( "dateTime" ).getAsString( );
				
				DateTime dt = DATE_TIME_FORMAT.parseDateTime( dateTime );
				Interval interval = new Interval( dt, dt.plusMinutes( intervalSize ).minusSeconds( 1 ) );
				this.processDataPoint( interval, dataPointObject );
			}
		}
		
		private int computeIntervalSize( JsonArray dataPoints ) {
			
			DateTime previousDateTime = null;
			int intervalSum = 0;
			float intervalCount = 0;
			for ( JsonElement dataPoint : dataPoints ) {

				String dateTime = dataPoint
					.getAsJsonObject( )
					.get( "dateTime" )
					.getAsString( );
				
				DateTime dt = DATE_TIME_FORMAT.parseDateTime( dateTime );
				if ( previousDateTime != null ) {
					intervalCount++;
					intervalSum += Minutes.minutesBetween( previousDateTime, dt ).getMinutes( );
				}
				previousDateTime = dt;
			}
			
			return intervalCount > 0 ? Math.round( intervalSum / intervalCount ) : 0;
		}
	}
	
	protected static class WeightResponseHandler extends ResponseHandler {
		
		List<Weight> weights = new ArrayList<Weight>( );
		
		protected void processResponse( String json ) {
			
			JsonParser parser = new JsonParser( );
			
			JsonArray dataPoints = parser.parse( json ).getAsJsonObject( )
				.get( "graph" ).getAsJsonObject( )
				.get( "dataSets" ).getAsJsonObject( )
				.get( "weight" ).getAsJsonObject( )
				.get( "dataPoints" ).getAsJsonArray( );
			
			for ( JsonElement dataPoint : dataPoints ) {

				JsonObject dataPointObject = dataPoint.getAsJsonObject( );
				
				Weight w = new Weight( );
				w.setDateTime( DATE_TIME_FORMAT.parseDateTime(
					dataPointObject.get( "dateTime" ).getAsString( )
				) );
				w.setValue( dataPointObject.get( "value" ).getAsFloat( ) );
				
				weights.add( w );
			}
		}
		
		public List<Weight> getWeights( ) {
			return this.weights;
		}
	}
	
	protected static abstract class ResponseHandler {
		
		protected abstract void processResponse( String json );

	}
	
	private static final String AJAX_API_URL = "https://www.fitbit.com/ajaxapi";
	
	private static final String GRAPH_BASE_URL = "http://www.fitbit.com/graph/getNewGraphData";

	private static final String I18N_URL = "https://www.fitbit.com/i18n/switch";
	
	private static final String LOGIN_URL = "https://www.fitbit.com/login";
	
	private static final String SLEEP_BASE_URL = "http://www.fitbit.com/sleep/";
	
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern( "yyyy-M-dd" ).withLocale( Locale.US );

	private static final DateTimeFormatter DATE_TIME_FORMAT =  DateTimeFormat.forPattern( "yyyy-M-dd HH:mm:ss" ).withLocale( Locale.US );
	
	private static final DateTimeFormatter URL_DATE_FORMAT = DateTimeFormat.forPattern(  "yyyy/MM/dd" ).withLocale( Locale.US );
	
	/**
	 * Creates a new Fitbit instance
	 * 
	 * @param email email address used to authenticate with Fitbit website
	 * @param password password used to authenticate with Fitbit website
	 * @return Fitbit client
	 * @throws FitbitAuthenticationException
	 */
	public static Fitbit create( String email, String password ) throws FitbitAuthenticationException {
		return new Fitbit( email, password );
	}
	
	protected static HttpClient createHttpClient( ) {
		return HttpClientBuilder.create( )
			.setRedirectStrategy( new DefaultRedirectStrategy( ) {
				@Override
				public boolean isRedirected( HttpRequest request, HttpResponse response, HttpContext context ) throws ProtocolException {
					int responseCode = response.getStatusLine( ).getStatusCode( );
					return super.isRedirected( request, response, context ) || responseCode == 301 || responseCode == 302;
				}
			} )
			.setDefaultRequestConfig( RequestConfig.custom( ).setCookieSpec( CookieSpecs.BROWSER_COMPATIBILITY ).build( ) )
			.build( );
	}
	
	private HttpClient httpClient;
	
	private String userId;
	
	private String userLocale;
	
	/**
	 * Constructor attempts to authenticate based on provided credentials. If it fails,
	 * an authentication exception is thrown.
	 * 
	 * @param email address on Fitbit account
	 * @param password of Fitbit account
	 * @throws FitbitAuthenticationException if authentication fails
	 */
	public Fitbit( String email, String password ) throws FitbitAuthenticationException {
		this.httpClient = createHttpClient( );
		this.userId = authenticate( email, password );
	}
	
	/**
	 * Workaround for users with Fitbit user profile set to other other than en_US. Due
	 * to the many variations in responses, this client is only compatible handling
	 * en_US values.
	 * 
	 * @see Fitbit#restoreLocale
	 * 
	 * @return true if successful
	 */
	public boolean enableLocaleOverride( ) {
		try {
			URIBuilder builder = new URIBuilder( I18N_URL );
			builder.addParameter( "locale", "en_US" );
			HttpGet get = new HttpGet( builder.build( ).toURL( ).toString( ) );
			String result = EntityUtils.toString( this.getHttpClient( ).execute( get ).getEntity( ) ).trim( );
			return result.contains("Succeeded" );
		} catch( Exception e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	/**
	 * Provides intraday resolution calories burned on the specified date.
	 * 
	 * @param date of activity logs
	 * @return List of {@link CalorieBurn}s for date specified
	 */
	public List<CalorieBurn> getCaloriesBurned( LocalDate date ) {
		
		final List<CalorieBurn> result = new ArrayList<CalorieBurn>( );
		this.getGraphData( "intradayCaloriesBurned", date, null, new ActivityResponseHandler( ) {
			
			@Override
			protected void processDataPoint( Interval interval, JsonObject dataPoint ) {
				CalorieBurn cb = new CalorieBurn( );
				cb.setValue( dataPoint.get( "value" ).getAsInt( ) );
				cb.setInterval( interval );
				cb.setActivityLevel( dataPoint.get( "activityLevel" ).getAsString( ) );
				result.add( cb );
			}
		} );
		return result;
	}
	
	/**
	 * Provides intraday resolution floors climbed on the specified date.
	 * 
	 * @param date of activity logs
	 * @return List of {@link FloorCount}s for specified date
	 */
	public List<FloorCount> getFloorCount( LocalDate date ) {
		
		
		final List<FloorCount> result = new ArrayList<FloorCount>( );
		this.getGraphData( "intradayFloors", date, null, new ActivityResponseHandler( ) {
			
			@Override
			protected void processDataPoint( Interval interval, JsonObject dataPoint ) {
				FloorCount fc = new FloorCount( );
				fc.setValue( dataPoint.get( "value" ).getAsInt( ) );
				fc.setInterval( interval );
				result.add( fc );
			}
		} );
		return result;
	}
	
	/**
	 * Provides sleep level for time spent in bed. Sleep sessions
	 * are associated with the day session ends, not begins.
	 *  
	 * @param date of activity logs
	 * @return List of {@link SleepSession}s for date specified
	 */
	public List<SleepSession> getSleepSessions( LocalDate date ) {
		
		final List<SleepSession> result = new ArrayList<SleepSession>( );
		for ( String sessionId : getSleepSessionIds( date ) ) {
			
			SleepSession session = new SleepSession( );
			final List<SleepLevel> levels = new ArrayList<SleepLevel>( );
			session.setSleepLevels( levels );
			
			getGraphData( "intradaySleep", date, Collections.singletonMap( "arg", sessionId ), new ActivityResponseHandler( ) {
				@Override
				protected void processDataPoint( Interval interval, JsonObject dataPoint ) {
					SleepLevel sl = new SleepLevel( );
					sl.setValue( dataPoint.get( "value" ).getAsInt( ) );
					sl.setInterval( interval );
					levels.add( sl );
				}
			} );
			
			if ( !levels.isEmpty( ) ) {
				session.setInterval(
					new Interval(
						levels.get( 0 ).getInterval( ).getStart( ),
						levels.get( levels.size( ) - 1 ).getInterval( ).getEnd( )
					) 
				);
			}
			
			result.add( session );
		}
		
		return result;
	}
	
	/**
	 * Provides intraday resolution steps taken on the specified date.
	 * 
	 * @param date of activity logs
	 * @return List of {@link StepCount}s for date specified
	 */
	public List<StepCount> getStepCount( LocalDate date ) {
		
		final List<StepCount> result = new ArrayList<StepCount>( );
		this.getGraphData( "intradaySteps", date, null, new ActivityResponseHandler( ) {
			@Override
			protected void processDataPoint( Interval interval, JsonObject dataPoint ) {
				StepCount sc = new StepCount( );
				sc.setValue( dataPoint.get( "value" ).getAsInt( ) );
				sc.setInterval( interval );
				result.add( sc );
			}
		} );
	
		return result;
	}
	
	/**
	 * Provides records weight over the provided time interval
	 * 
	 * @param from date of weight interval start
	 * @param to date of weight interval end
	 * @return List of {@link Weight}s for interval specified
	 */
	public List<Weight> getWeights( LocalDate from, LocalDate to ) {
		
		WeightResponseHandler responseHandler = new WeightResponseHandler( );
		this.getGraphData( "weight", from, from, null, responseHandler );
	
		return responseHandler.getWeights( );
	}

	/**
	 * Provides access to tracking device status/info associated with Fitbit account
	 * 
	 * @return FitbitTracker linked to user account
	 */
	public FitbitTracker getTracker( ) {
		
		try {
			
			JsonArray serviceCalls = new JsonArray( );
			JsonObject getDevicesCall = new JsonObject( );
			getDevicesCall.addProperty( "name", "device" );
			getDevicesCall.addProperty( "method", "getOwnerDevices" );
			serviceCalls.add( getDevicesCall );
			
			JsonObject request = new JsonObject( );
			request.add( "serviceCalls", serviceCalls );
			
			URIBuilder builder = new URIBuilder( AJAX_API_URL );
			
			builder.addParameter( "request", request.toString( ) );
			HttpGet post = new HttpGet( builder.build( ).toURL( ).toString( ) );
			HttpResponse response = this.getHttpClient( ).execute( post );
			if ( response.getStatusLine( ).getStatusCode( ) != 200 )
				throw new FitbitExecutionException( );
			
			String result = EntityUtils.toString( response.getEntity( ) ).trim( );
			
			JsonParser parser = new JsonParser( );
			JsonArray jsonResult = parser.parse( result ).getAsJsonObject( )
				.get( "ajaxResponse" ).getAsJsonObject( )
				.get( "newResult" ).getAsJsonObject( )
				.get( "getOwnerDevices" ).getAsJsonObject( )
				.get( "result" ).getAsJsonArray( );
			
			if ( jsonResult.size( ) > 0 ) {
				
				JsonObject trackerJson = jsonResult.get( 0 ).getAsJsonObject( );
				
				FitbitTracker tracker = new FitbitTracker( );
				tracker.setBattery( trackerJson.get( "battery" ).getAsString( ) );
				tracker.setId( trackerJson.get( "id" ).getAsString( ) );
				tracker.setLastSync( new DateTime( trackerJson.get( "lastSyncTime" ).getAsLong( ) ) );
				tracker.setProductName( trackerJson.get( "productName" ).getAsString( ) );
				tracker.setType( trackerJson.get( "type" ).getAsString( ) );
				return tracker;
				
			} else {
				return null;
			}
			
		} catch( IOException e ) {
			throw new FitbitExecutionException( e );
		} catch( URISyntaxException e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	/**
	 * Each Fitbit user has a 6 digit unique user ID separate from their email address
	 * 
	 * @return unique Fitbit user ID authenticated for client
	 */
	public String getUserId( ) {
		return this.userId;
	}
	
	/**
	 * Restore user location to original value discovered during authentication. This is used to 
	 * undo the override enabled in {@link #enableLocaleOverride()}
	 * 
	 * @see #enableLocaleOverride()
	 * @return true if successful
	 */
	public boolean restoreUserLocale( ) {
		try {
			
			if ( this.userLocale != null ) {
				URIBuilder builder = new URIBuilder( I18N_URL );
				builder.addParameter( "locale", this.userLocale );
				HttpGet get = new HttpGet( builder.build( ).toURL( ).toString( ) );
				String result = EntityUtils.toString( this.getHttpClient( ).execute( get ).getEntity( ) ).trim( );
				return result.contains("Succeeded" );
			}
			
			return false;
			
		} catch( Exception e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	protected String authenticate( String email, String password ) throws FitbitAuthenticationException {

		String response = null;
		try {
			//go ahead and consume it – if on Android, we don't have #consume on EntityUtils
			EntityUtils.toString( this.getHttpClient( ).execute( new HttpGet( LOGIN_URL ) ).getEntity( ) );
			
			HttpPost loginPost = new HttpPost( LOGIN_URL );
			List<NameValuePair> parameters = new ArrayList<NameValuePair>( );
			parameters.add( new BasicNameValuePair( "email", email ) );
			parameters.add( new BasicNameValuePair( "password", password ) );
			parameters.add( new BasicNameValuePair( "login", "Log In" ) );
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity( parameters, "utf-8" );
			
			loginPost.setEntity( formEntity );
			
			HttpResponse httpResponse = this.httpClient.execute( loginPost );
			response = EntityUtils.toString( httpResponse.getEntity( ) );
			
			this.userLocale = httpResponse.getLastHeader( "Content-Language" ).getValue( );
			if ( this.userLocale != null )
				this.userLocale = this.userLocale.replace("-", "_" );
			
		} catch( Exception e ) {
			throw new FitbitExecutionException( e );
		}
		
		Matcher m = Pattern.compile( "./user/([A-Z0-9]+)" ).matcher( response );
		if ( !m.find( ) )
			throw new FitbitAuthenticationException( );
		
		return m.group( 1 );
	}
	
	protected URL buildGraphUrl( String type, LocalDate from, LocalDate to, Map<String,String> customParams ) throws MalformedURLException, URISyntaxException {
		
		URIBuilder builder = new URIBuilder( GRAPH_BASE_URL );
		builder.addParameter( "userId", this.getUserId( ) );
		builder.addParameter( "type", type );
		builder.addParameter( "apiFormat", "json" );
		builder.addParameter( "dateFrom", DATE_FORMAT.print( from ) );
		builder.addParameter( "dateTo", DATE_FORMAT.print( to ) );
		
		if ( customParams != null )
			for ( String key : customParams.keySet( ) )
				builder.addParameter( key, customParams.get( key ) );
		
		return builder.build( ).toURL( );
	}
	
	protected void getGraphData( String type, LocalDate date, Map<String,String> customParams, ResponseHandler handler ) {
		getGraphData( type, date, date, customParams, handler );
	}
	
	protected void getGraphData( String type, LocalDate from, LocalDate to, Map<String,String> customParams, ResponseHandler handler ) {
		try {
			HttpGet get = new HttpGet( buildGraphUrl( type, from, to, customParams ).toString( ) );
			HttpResponse response = this.getHttpClient( ).execute( get );
			if ( response.getStatusLine( ).getStatusCode( ) != 200 )
				throw new FitbitExecutionException( );
			
			String result = EntityUtils.toString( response.getEntity( ) ).trim( );
			handler.processResponse( result );
		} catch( IOException e ) {
			throw new FitbitExecutionException( e );
		} catch( URISyntaxException e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	protected HttpClient getHttpClient( ) {
		return this.httpClient;
	}
	
	protected List<String> getSleepSessionIds( LocalDate date ) {
		
		List<String> sessions = new ArrayList<String>( );
		try {
			HttpGet pageGet = new HttpGet( SLEEP_BASE_URL + URL_DATE_FORMAT.print( date ) );
			HttpResponse response = this.getHttpClient( ).execute( pageGet );
			if ( response.getStatusLine( ).getStatusCode( ) != 200 )
				throw new FitbitExecutionException( );
			
			String pageResult = EntityUtils.toString( response.getEntity( ) );
			Matcher m = Pattern.compile( "sleepRecord\\.([0-9]+)" ).matcher( pageResult );
			while ( m.find( ) ) {
				sessions.add( m.group( 1 ) );
			}
		} catch( IOException e ) {
			throw new FitbitExecutionException( e );
		}
		
		return sessions;
	}
}
