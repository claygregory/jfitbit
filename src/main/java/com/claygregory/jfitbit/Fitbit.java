package com.claygregory.jfitbit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * Unofficial Fitbit client for retrieving intraday Fitbit data as displayed in dashboards on Fitbit.com.
 * 
 * <p>Note: Fitbit does not provide timezone information on timestamps,
 * so it's assumed the timezone preference set in the Fitbit user profile matches timezone
 * settings in the local environment.</p>
 * 
 * @author Clay Gregory
 *
 */
public class Fitbit {
	
	private static String GRAPH_BASE_URL = "http://www.fitbit.com/graph/getGraphData";
	
	private static String I18N_URL = "https://www.fitbit.com/i18n/switch";
	
	private static String LOGIN_URL = "https://www.fitbit.com/login";

	private static String SLEEP_BASE_URL = "http://www.fitbit.com/sleep/";
	
	private static DateTimeFormatter RESULT_DATE_FORMAT = DateTimeFormat.forPattern( "EEE, MMM dd" ).withLocale( Locale.US );

	private static DateTimeFormatter REQUEST_DATE_FORMAT = DateTimeFormat.forPattern( "yyyy-M-dd" ).withLocale( Locale.US );
	
	private static DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder( ).append( null, new DateTimeParser[ ] {
	                                                   DateTimeFormat.forPattern( "hh:mmaa" ).withLocale( Locale.US ).getParser( ),
	                                                   DateTimeFormat.forPattern( "HH:mm" ).withLocale( Locale.US ).getParser( )
	                                               } )
	                                               .toFormatter( );
	
	private static DateTimeFormatter URL_DATE_FORMAT = DateTimeFormat.forPattern(  "yyyy/MM/dd" ).withLocale( Locale.US );

	protected static abstract class ResponseHandler {
		
		/**
		* Fired on start of results on date
		* @param date of results
		*/
		protected void start( LocalDate date ) {
		//optional to implement, default noop
		}
		
		/**
		* Receives entry of results from response
		* 
		* @param interval parsed from description
		* @param value value of result entry
		* @param description description of result entry (usually including unparsed date/time)
		*/
		protected void process( Interval interval, String value, String description ) {
		//optional to implement, default noop
		}

		/**
		* Fired on completion of results on date
		* @param date of results
		*/
		protected void end( LocalDate date ) {
		//optional to implement, default noop
		}
	}
	
	private HttpClient httpClient;
	
	private String userId;
	
	private String userLocale;
	
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
	 * Provides activity level breakdown of the day. Only 
	 * available at {@link FitbitResolution#DAILY} resolution.
	 *  
	 * @param q
	 * @return List of {@link ActivityLevel}s for time range specified by query
	 */
	public List<ActivityLevel> activityLevel( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				throw new IllegalArgumentException( );
			case DAILY:
				type = "minutesActive";
				break;
		}
		
		final List<ActivityLevel> result = new ArrayList<ActivityLevel>( );
		this.execute( type, q, new ResponseHandler( ) {
			
			protected ActivityLevel level;
			
			@Override
			protected void start( LocalDate date ) {
				this.level = new ActivityLevel( );
				this.level.setInterval( new Interval( date.toDateTimeAtStartOfDay( ), date.toDateTimeAtStartOfDay( ).plusDays( 1 ).withTimeAtStartOfDay( ) ) );
			}
			
			@Override
			protected void process( Interval interval, String value, String description ) {
				Duration d = Duration.millis( ( long ) Math.round( Float.parseFloat( value ) * 60 * 60 * 1000 ) );
				if ( description.contains( "lightly" ) )
					this.level.setLightlyActive( d );
				else if ( description.contains( "fairly" ) )
					this.level.setFairlyActive( d );
				else if ( description.contains( "very" ) )
					this.level.setVeryActive( d );
			}
			
			@Override
			protected void end( LocalDate date ) {
				result.add( this.level );
			}
		} );
		return filterResults( result, q );
	}

	/**
	 * Provides activity score breakdown at either daily or intraday resolution.
	 *  
	 * @param q
	 * @return List of {@link ActivityScore}s for time range specified by query
	 */
	public List<ActivityScore> activityScore( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				type = "intradayActiveScore";
				break;
			case DAILY:
				type = "activeScore";
				break;
		}
		
		final List<ActivityScore> result = new ArrayList<ActivityScore>( );
		this.execute( type, q, new ResponseHandler( ) {
			
			@Override
			protected void process( Interval interval, String value, String description ) {
				ActivityScore as = new ActivityScore( );
				as.setScore( Math.round( Float.parseFloat( value ) ) );
				as.setInterval( interval );
				result.add( as );
			}
		} );
		return filterResults( result, q );
	}
	
	/**
	 * Provides calories consumed for the day. Only 
	 * available at {@link FitbitResolution#DAILY} resolution.
	 * 
	 * @param q
	 * @return List of {@link CalorieCount}s for time range specified by query
	 */
	public List<CalorieCount> calorieCount( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				throw new IllegalArgumentException( );
			case DAILY:
				type = "caloriesConsumed";
				break;
		}
		
		final List<CalorieCount> result = new ArrayList<CalorieCount>( );
		this.execute( type, q, new ResponseHandler( ) {
			
			@Override
			protected void process( Interval interval, String value, String description ) {
				CalorieCount cc = new CalorieCount( );
				cc.setCalories( Math.round( Float.parseFloat( value ) ) );
				cc.setInterval( interval );
				result.add( cc );
			}
		} );
		return filterResults( result, q );
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
			String result = EntityUtils.toString( this.httpClient( ).execute( get ).getEntity( ) ).trim( );
			return result.contains("Succeeded" );
		} catch( Exception e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	/**
	 * Provides floor counts at either daily or intraday resolutions.
	 * 
	 * @param q
	 * @return List of {@link FloorCount}s for time range specified by query
	 */
	public List<FloorCount> floorCount( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				type = "intradayAltitude";
				break;
			case DAILY:
				type = "altitude";
				break;
		}
		
		final List<FloorCount> result = new ArrayList<FloorCount>( );
		this.execute( type, q, new ResponseHandler( ) {
			
			@Override
			protected void process( Interval interval, String value, String description ) {
				FloorCount fc = new FloorCount( );
				fc.setFloors( Math.round( Float.parseFloat( value ) ) );
				fc.setInterval( interval );
				result.add( fc );
			}
		} );
		return filterResults( result, q );
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
				String result = EntityUtils.toString( this.httpClient( ).execute( get ).getEntity( ) ).trim( );
				return result.contains("Succeeded" );
			}
			
			return false;
			
		} catch( Exception e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	/**
	 * Provides sleep level for time spent in bed. Only available at 
	 * {@link FitbitResolution#INTRADAY} resolution.
	 *  
	 * @param q
	 * @return List of {@link SleepLevel}s for time in bed during range specified by query
	 */
	public List<SleepLevel> sleepLevel( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				type = "intradaySleep";
				break;
			case DAILY:
				throw new IllegalArgumentException( );
		}
		
		final List<SleepLevel> result = new ArrayList<SleepLevel>( );
		this.executeSleep( type, q, new ResponseHandler( ) {
			
			private List<SleepLevel> dayBuffer = new ArrayList<SleepLevel>( );
			
			@Override
			protected void process( Interval interval, String value, String description ) {		
				SleepLevel sl = new SleepLevel( );
				sl.setLevel( Math.round( Float.parseFloat( value ) ) );
				sl.setInterval( interval );
				this.dayBuffer.add( sl );
			}
			
			@Override
			protected void end( LocalDate date ) {
				if ( !this.dayBuffer.isEmpty( ) ) {
					DateTime end = this.dayBuffer.get( this.dayBuffer.size( ) - 1 ).getInterval( ).getStart( );
					for ( SleepLevel s : this.dayBuffer ) {
						if ( s.getInterval( ).isAfter( end ) )
							s.setInterval( new Interval( s.getInterval( ).getStart( ).minusDays( 1 ), s.getInterval( ).getEnd( ).minusDays( 1 ) ) );
					}
				}
				
				result.addAll( this.dayBuffer );
				
				this.dayBuffer.clear( );
			}
		} );
		
		Collections.sort( result, createIntervalComparator( ) );
		return filterResults( result, q );
	}
	
	/**
	 * Provides step counts at either daily or intraday resolutions.
	 * 
	 * @param q
	 * @return List of {@link StepCount}s for time range specified by query
	 */
	public List<StepCount> stepCount( FitbitQuery q ) {
		
		String type = "";
		switch ( q.atResolution( ) ) {
			case INTRADAY:
				type = "intradaySteps";
				break;
			case DAILY:
				type = "stepsTaken";
				break;
		}
		
		final List<StepCount> result = new ArrayList<StepCount>( );
		this.execute( type, q, new ResponseHandler( ) {
			
			@Override
			protected void process( Interval interval, String value, String description ) {
				StepCount sc = new StepCount( );
				sc.setSteps( Math.round( Float.parseFloat( value ) ) );
				sc.setInterval( interval );
				result.add( sc );
			}
		} );
	
		return filterResults( result, q );
	}
	
	/**
	 * Each Fitbit user has a 6 digit unique user ID separate from their email address
	 * 
	 * @return unique Fitbit user ID authenticated for client
	 */
	public String userId( ) {
		return this.userId;
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
	
	protected static Comparator<FitbitInterval> createIntervalComparator( ) {
		return new Comparator<FitbitInterval>( ) {
			@Override
			public int compare( FitbitInterval o1, FitbitInterval o2 ) {
				if ( o1.getInterval( ).isBefore( o2.getInterval( ) ) )
					return -1;
				else if ( o1.getInterval( ).isAfter( o2.getInterval( ) ) )
					return 1;
				else
					return 0;
			}
		};
	}
	
	protected static<T extends FitbitInterval> List<T> filterResults( List<T> results, FitbitQuery query ) {
		
		//since Fitbit returns paged results by day, no need to filter locally
		if ( query.atResolution( ) == FitbitResolution.DAILY )
			return results;
		
		List<T> filteredResults = new ArrayList<T>( );
		for ( T r : results )
			if ( r.getInterval( ).getStart( ).isAfter( query.from( ) ) && r.getInterval( ).getStart( ).isBefore( query.to( ) ) )
				filteredResults.add( r );
				
		return filteredResults;
	}
	
	protected static LocalDate parseDate( LocalDate requestedDate, String date ) throws ParseException {
		LocalDate parsedDate = RESULT_DATE_FORMAT.parseLocalDate( date );
		return parsedDate.withYear( requestedDate.getYear( ) );
	}
	
	protected static DateTime parseTime( LocalDate requestedDate, String time ) throws ParseException {
		return requestedDate.toDateTimeAtStartOfDay( ).plus( TIME_FORMAT.parseLocalTime( time ).getMillisOfDay( ) );
	}
	
	protected static Interval parseDateTimeDescription( LocalDate dateContext, String description ) throws ParseException {
		
		if ( description.matches( ".* from .* to .*" ) ) {
			
			String[ ] descriptionParts = description.replaceFirst( ".* from ", "" ).split( " to " );
			DateTime start = parseTime( dateContext, descriptionParts[ 0 ] );
			DateTime end = parseTime( dateContext, descriptionParts[ 1 ] );
			
			if ( end.isBefore( start ) )
				end = end.plusHours( 24 );
			
			return new Interval( start, end );
			
		} else if ( description.matches( ".* on ..., ... \\d{1,2}" ) ) {
			
			LocalDate date = parseDate( dateContext, description.replaceFirst( ".* on ", "" ) );
			return new Interval( date.toDateTimeAtStartOfDay( ), date.toDateTimeAtStartOfDay( ).plusDays( 1 ).withTimeAtStartOfDay( ) );
			
		} else if ( description.matches( ".* at \\d{1,2}:\\d{2}.*" ) ) {
			
			DateTime start = parseTime( dateContext, description.replaceFirst( ".* at ", "" ) );
			return new Interval( start, start.plusMinutes( 1 ) );
		} else {
			throw new ParseException( "Unrecognized date/time format in description", 0 );
		}
	}
	
	protected static void parseResult( LocalDate date, String result, ResponseHandler handler ) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance( );
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder( );
			Document d = dBuilder.parse( new ByteArrayInputStream( result.getBytes( ) ) );
					
			XPath xpath = XPathFactory.newInstance( ).newXPath( );
			NodeList values = ( NodeList ) xpath.evaluate( "/settings/data/chart/graphs/graph/value", d, XPathConstants.NODESET );
			
			handler.start( date );
			for ( int i = 0; i < values.getLength( ); i++ ) {
				
				Node descriptionNode = values.item( i ).getAttributes( ).getNamedItem( "description" );
				String value = values.item( i ).getFirstChild( ).getNodeValue( );
				if ( descriptionNode != null ) {
					String description = descriptionNode.getTextContent( );
					Interval interval = parseDateTimeDescription( date, description );
					if ( interval != null )
						handler.process( interval, value, description );
				}
			}
			handler.end( date );

		} catch ( Exception e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	protected String authenticate( String email, String password ) throws FitbitAuthenticationException {

		String response = null;
		try {
			EntityUtils.consume( this.httpClient.execute( new HttpGet( LOGIN_URL ) ).getEntity( ) );
			
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
	
	protected URL buildUrl( String type, LocalDate date, Map<String,String> customParams ) throws MalformedURLException, URISyntaxException {
		
		URIBuilder builder = new URIBuilder( GRAPH_BASE_URL );
		builder.addParameter( "userId", this.userId( ) );
		builder.addParameter( "type", type );
		builder.addParameter( "version", "amchart" );
		builder.addParameter( "dataVersion", "14" );
		builder.addParameter( "chart_type", "column2d" );
		builder.addParameter( "period", "1d" );
		builder.addParameter( "dateTo", REQUEST_DATE_FORMAT.print( date ) );
		
		if ( customParams != null )
			for ( String key : customParams.keySet( ) )
				builder.addParameter( key, customParams.get( key ) );
		
		return builder.build( ).toURL( );
	}
	
	protected void execute( String type, FitbitQuery query, ResponseHandler handler ) {
		execute( type, query, handler, null );
	}
	
	protected void execute( String type, FitbitQuery query, ResponseHandler handler, Map<String,String> customParams ) {
		
		DateTime date = query.from( );
		while ( date.isBefore( query.to( ) ) ) {
			execute( type, date.toLocalDate( ), handler, customParams );
			date = date.plusDays( 1 );
		}
	}
	
	protected void execute( String type, LocalDate date, ResponseHandler handler, Map<String,String> customParams ) {
		try {
			HttpGet get = new HttpGet( buildUrl( type, date, customParams ).toString( ) );
			String result = EntityUtils.toString( this.httpClient( ).execute( get ).getEntity( ) ).trim( );
			parseResult( date, result, handler );
		} catch( IOException e ) {
			throw new FitbitExecutionException( e );
		} catch( URISyntaxException e ) {
			throw new FitbitExecutionException( e );
		}
	}
	
	//special handler for sleep -- must first fetch sleep session to get IDs.
	protected void executeSleep( String type, FitbitQuery query, ResponseHandler handler ) {
		
		DateTime date = query.from( ).withTimeAtStartOfDay( );
		while ( date.isBefore( query.to( ) ) ) {
			for ( String session : getSleepSessions( date.toLocalDate( ) ) ) {
				Map<String,String> params = new HashMap<String,String>( );
				params.put( "arg", session );
				execute( type, date.toLocalDate( ), handler, params );
			}
			date = date.plusDays( 1 );
		}
	}
	
	protected List<String> getSleepSessions( LocalDate date ) {
		
		List<String> sessions = new ArrayList<String>( );
		try {
			HttpGet pageGet = new HttpGet( SLEEP_BASE_URL + URL_DATE_FORMAT.print( date ) );
			String pageResult = EntityUtils.toString( this.httpClient( ).execute( pageGet ).getEntity( ) );
			Matcher m = Pattern.compile( "sleepRecord\\.([0-9]+)" ).matcher( pageResult );
			while ( m.find( ) ) {
				sessions.add( m.group( 1 ) );
			}
		} catch( IOException e ) {
			throw new FitbitExecutionException( e );
		}
		
		return sessions;
	}
	
	protected HttpClient httpClient( ) {
		return this.httpClient;
	}
}
