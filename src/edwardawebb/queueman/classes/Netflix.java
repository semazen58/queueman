/**
 *     This file is part of QueueMan.
 *
 *	  QueueMan is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    QueueMan is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with QueueMan.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edwardawebb.queueman.classes;
import java.net.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.net.Uri;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.apikeys.ApiKeys;
import edwardawebb.queueman.core.QueueMan;
import edwardawebb.queueman.handlers.AddDiscQueueHandler;
import edwardawebb.queueman.handlers.AddInstantQueueHandler;
import edwardawebb.queueman.handlers.DiscETagHandler;
import edwardawebb.queueman.handlers.DiscQueueHandler;
import edwardawebb.queueman.handlers.HomeQueueHandler;
import edwardawebb.queueman.handlers.InstantETagHandler;
import edwardawebb.queueman.handlers.InstantQueueHandler;
import edwardawebb.queueman.handlers.MoveQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.RecommendationsHandler;
import edwardawebb.queueman.handlers.SearchQueueHandler;
import edwardawebb.queueman.handlers.UserHandler;
/** Singleton */
public class Netflix{
	protected User user;

	protected String mashupConsumerKey;

	protected String mashupConsumerSecret;

	private OAuthConsumer oauthConsumer;

	protected DefaultOAuthProvider oauthProvider;

	private final static Netflix NETFLIX = new Netflix(); // the one instance, yes, a singleton
	
	
	private int testLoop=0;
	private int testLoopLimit=30;
	private long[] times={0,0,0,0};
	
	private static final String APPLICATION_NAME = "QueueMan";
	// TODO you will need to make this class after chkecin out. 
	// See issue # 3
	// TODO !! NEVER check in the class, these need to remain private
	private static final String CONSUMER_KEY = ApiKeys.getConsumerKey();
	private static final String CONSUMER_SECRET = ApiKeys.getConsumerSecret();
	// for oAuth we van 'request_token' and then 'access_token'
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.Netflix.com/oauth/request_token";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.Netflix.com/oauth/access_token";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api-user.Netflix.com/oauth/login";
	// queue types

/*	public static NetflixQueue searchQueue = new NetflixQueue(
			NetflixQueue.QUEUE_TYPE_SEARCH);
	public static NetflixQueue discQueue = new NetflixQueue(
			NetflixQueue.QUEUE_TYPE_DISC);
	public static NetflixQueue instantQueue = new NetflixQueue(
			NetflixQueue.QUEUE_TYPE_INSTANT);
	public static NetflixQueue recomemendedQueue = new NetflixQueue(
			NetflixQueue.QUEUE_TYPE_RECOMMEND);
	public static NetflixQueue homeQueue = new NetflixQueue(
			NetflixQueue.QUEUE_TYPE_HOME);*/

	public static OAuthConsumer oaconsumer;
	private static OAuthProvider oaprovider;

	
	private static String resultStatus;
	public String lastResponseMessage = "none";
	public String lastNFResponseMessage = "";
	
	//For some methods they will retry themselves, got a better way?
	private int retries = 0;
	public static final int MAX_RETRIES=2;// At least 1 for that pesky 502, but there must mbe a limit!
	
	public static final int NF_ERROR_BAD_DEFAULT=900; // defaukl return code
	public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final int NF_ERROR_NO_MORE=903; // asking for higher start then total results
	//public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final String NF_RATING_NO_INTEREST = "not_interested";
	public static final int MOVED_OUTSIDE_CURRENT_VIEW = 299; // result code used when disc is moved outside our current range (and we need ot remove it)
	public static final int BOTTOM = 500;
	public static final int TOP=1;
	public static final int SUCCESS_FROM_CACHE = 255;
	
	/**
	 * For first time, we will need to GET request  tokens, and then request user details
	 */
	
	//@ TODO Use setters for stage.
	private Netflix(){
		Netflix.oaconsumer = new DefaultOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		Netflix.oaprovider = new DefaultOAuthProvider(oaconsumer,
				REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
				AUTHORIZE_WEBSITE_URL);	
		
		
	}
	
	/**
	 * second time, we will need to request access tokens from request token
	 */
	public void setRequestTokens(String requestToken, String requestSecret){
		
		Netflix.oaconsumer.setTokenWithSecret(requestToken,requestSecret);
		
	}
	
	/**
	 * We already know the user (was serioalized and saved previously), and have the required tokens
	 * @param user
	 */
	public void setExistingUser(String userId, String accessToken, String accessSecret) {
		this.user=new User(userId, accessToken, accessSecret);			
		//load name, and formats (we dont save as it might change)
		getUserDetails();
		
	}
	
	public static Netflix getInstance(){
		// TODO add implementation and return statement
		return NETFLIX;
	}

	public void authorizeUser(){
		// TODO add implementation
	}
	
	public Uri getRequestLoginUri() {
		Uri result = null;

		// we'll always need a request and access token (I think)
		// callbak only if users first time, but needed to get request token, so
		// aove may be wrong
		String callbackUrl = "flixman:///";// get url for user to link Netflix

		// Netflix.oaprovider.setOAuth10a(false);

		try {

			// Log.d("Netflix","token end:"+oaprovider.getRequestTokenEndpointUrl());
			// get url for user to lologin and request token
			String tmp = Netflix.oaprovider.retrieveRequestToken(callbackUrl);
			// Netflix.oaprovider.getResponseParameters().get("request_token");
			// Log.d("Netflix","Url:"+tmp)
			result = Uri.parse(tmp + "&application_name=" + APPLICATION_NAME
					+ "&oauth_consumer_key="+CONSUMER_KEY);
			
			
			
			Log.i("oauth", "request token:"
					+ result.getQueryParameter("oauth_token"));
			Log.i("oauth", "secret:"
					+ result.getQueryParameter("oauth_token_secret"));
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthNotAuthorizedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthCommunicationException e) {
			
			reportError(e, lastResponseMessage);
		}
		return result;
	}

	public boolean negotiateAccessToken(String requestToken) {
		Boolean result = false;
		// Log.i("oauth", "request token:" + requestToken)

		try {

			// call url to get access token and user id
			// oaprovider.retrieveAccessToken();
			// Netflix.oaconsumer.setTokenWithSecret(arg0, arg1)
			Netflix.oaprovider.retrieveAccessToken(requestToken);
			result = true;

			// dumpMap(oaprovider.getResponseParameters());
			// Log.d("oauth", "Access token: " + oaconsumer.getToken())
			// Log.d("oauth", "Token secret: " + oaconsumer.getTokenSecret())
			Log.d("oauth", "User ID:"
					+ oaprovider.getResponseParameters().get("user_id"));
			user = new User(oaprovider.getResponseParameters().get("user_id"),oaconsumer.getToken(),oaconsumer.getTokenSecret());
			
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthNotAuthorizedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthCommunicationException e) {
			
			reportError(e, lastResponseMessage);
		}
		return result;
	}




	
	/**
	 * 
	 * @param queueType
	 * @param maxResults
	 * @return HttpStatusCOde or NF_ERROR_BAD_DEFAULT for exceptions
	 *//*
	public int getRecommendationsJSON(int startIndex,String maxResults) {
		times[2]=System.currentTimeMillis();
		URL QueueUrl = null;
		int result = NF_ERROR_BAD_DEFAULT;
		if (maxResults.equals(QueueMan.ALL_TITLES_STRING)) {
			maxResults = "500";
		}
		
		
		String expanders = "?expand=synopsis,formats&output=json&start_index=" + startIndex + "&max_results=" + maxResults;
		InputStream is = null;
		try {
			// we're either rotating/task jumping OR we're starting/paging
			if (!Netflix.recomemendedQueue.isEmpty() && startIndex == recomemendedQueue.getStartIndex()){
				return 200;
			}else if(recomemendedQueue.getTotalTitles() < startIndex){
				return NF_ERROR_NO_MORE;
			}else{
				recomemendedQueue.purge();
			}
				
			QueueUrl = new URL("http://api.Netflix.com/users/" + user.getUserId()
					+ "/recommendations" + expanders);
			Log.d("Netflix",""+QueueUrl.toString());
			
			setSignPost(user.getAccessToken(), user.getAccessTokenSecret());
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			Netflix.oaconsumer.sign(request);
			request.connect();

			lastResponseMessage = request.getResponseCode() + ": "
					+ request.getResponseMessage();
			result = request.getResponseCode();
			
			is = request.getInputStream();
			
				BufferedReader in = new BufferedReader(new
					 InputStreamReader(is)); String linein = null; while ((linein =
					 in.readLine()) != null) { Log.d("NetflixResponse", "" +
					 linein); }
			//send json to be parsed and Disc objects added to r q.
			JSON2Queue.parseRecommended(is);

		} catch (IOException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "Expectation failed")
		}
		times[3]=System.currentTimeMillis();
		String high=maxResults; //per page equivilant
		if(recomemendedQueue.getPerPage()>recomemendedQueue.getTotalTitles()) high = ""+recomemendedQueue.getTotalTitles();
		Log.d("Performance","Recommendations" + "," + high +  "," + (times[1]-times[0]) +  "," + (times[3]-times[2]));
		return result;
	}
*/
	


	/**
	 * 
	 * @param queueType
	 * @param maxResults
	 * @return HttpStatusCOde or NF_ERROR_BAD_DEFAULT for exceptions
	 */
	public int getTitleState(String titleID) {
		URL url = null;
		QueueHandler myQueueHandler = null;
		int result = NF_ERROR_BAD_DEFAULT;

		/*String expanders = "?title_refs=" + titleID;
		InputStream xml = null;
		try {
			url = new URL("http://api.Netflix.com/users/" + user.getUserId()
					+ "/title_states" + expanders);
			// myQueueHandler = new InstantQueueHandler();
			Log.d("Netflix", "" + url.toString());
			setSignPost(user.getAccessToken(), user.getAccessTokenSecret());
			HttpURLConnection request = (HttpURLConnection) url
					.openConnection();

			Netflix.oaconsumer.sign(request);
			request.connect();

			lastResponseMessage = request.getResponseCode() + ": "
					+ request.getResponseMessage();
			result = request.getResponseCode();
			xml = request.getInputStream();
//			BufferedReader in = new BufferedReader(new InputStreamReader(xml));
//			String linein = null;
//			while ((linein = in.readLine()) != null) {
//				Log.d("Netflix", "GetTitle States: " + linein);
//			}
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			xr.setContentHandler(myQueueHandler);
			xr.parse(new InputSource(xml));

		} catch (ParserConfigurationException e) {
			
			reportError(e, lastResponseMessage);
		} catch (SAXException e) {
			
			reportError(e, lastResponseMessage);
		} catch (IOException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "Expectation failed")
		}*/
		return result;
	}

	
	
	
	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Returns the users first name, watch instant rights and preferred formats
	 * requires user already set with auth tokens
	 * @return
	 */
		public boolean getUserDetails() {
			Log.d("Netflix","getUserDetails()>>>");
			URL QueueUrl = null;
			UserHandler myHandler = new UserHandler(user);
			boolean result = false;
			InputStream xml = null;
			try {
	
				QueueUrl = new URL("http://api.Netflix.com/users/" + user.getUserId());
	
				setSignPost(user.getAccessToken(), user.getAccessTokenSecret());
				HttpURLConnection request = (HttpURLConnection) QueueUrl
						.openConnection();
	
				Netflix.oaconsumer.sign(request);

				Log.d("Netflix","getUserDetails() | signed");
				request.connect();

				Log.d("Netflix","getUserDetails() | response");
				//if succesful, populate remaing user details from Netflix
				if (request.getResponseCode() == 200) {

					xml = request.getInputStream();
	
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp;
					sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
	
					xr.setContentHandler(myHandler);
					Log.d("Netflix","getUserDetails() | ready to parse");
					xr.parse(new InputSource(xml));

					Log.d("Netflix","getUserDetails() | parse complete");
					Log.d("Netflix","User Name: " + user.getFirstName());
					Log.d("Netflix","Formats: " + user.getPreferredFormats().toArray().toString());
					Log.d("Netflix","User Instant?: " + user.isCanWatchInstant());
					
				}else{
					
					BufferedReader in = new BufferedReader(new InputStreamReader(
					xml));
					String linein = null;
					while ((linein = in.readLine()) != null) {
						Log.d("Netflix", "UserDetails: " + linein);
					}
				}
	
			} catch (ParserConfigurationException e) {
				
				reportError(e, lastResponseMessage);
			} catch (SAXException e) {
				
				reportError(e, lastResponseMessage);
			} catch (IOException e) {
				
				reportError(e, lastResponseMessage);
				// Log.i("Netflix", "IO Error connecting to Netflix queue")
			} catch (OAuthMessageSignerException e) {
				
				reportError(e, lastResponseMessage);
				// Log.i("Netflix", "Unable to Sign request - token invalid")
			} catch (OAuthExpectationFailedException e) {
				
				reportError(e, lastResponseMessage);
				// Log.i("Netflix", "Expectation failed")
			}

			Log.d("Netflix","getUserDetails()>>>");
			return result;
		}

	/**
	 * get token from consumer
	 * @return token string
	 */
	public String getRT(){
		return Netflix.oaconsumer.getToken();
	}

	/**
	 * get token secret from conumser
	 * @return secret string
	 */
	public String getRTS(){
		return Netflix.oaconsumer.getTokenSecret();
	}

/*	*//**
	 * 
	 * @param disc
	 * @param queueType
	 * @return SubCode, httpResponseCode or NF_ERROR_BAD_DEFAULT on exception
	 *//*
	public int addToQueue(Disc disc, int queueType) {
		lastResponseMessage="";
		lastNFResponseMessage="";
		int result = NF_ERROR_BAD_DEFAULT;
		// 2 choirs, send request to Netflix, and if successful update local q.
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
		OAuthProvider postProvider = new DefaultOAuthProvider(postConsumer,REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,AUTHORIZE_WEBSITE_URL);
		String expanders = "?expand=synopsis,formats";
		InputStream xml = null;
		NetflixQueue queue = null;
		URL QueueUrl = null;
		String eTag = null;
		URL url = null;
		try {

			// Construct data
			int queueSize=0;
			switch (queueType) {
			case NetflixQueue.QUEUE_TYPE_DISC:
				queueSize=Netflix.discQueue.getTotalTitles();
				if(queueSize==0) getNewETag(queueType);
				// @ TODO This is for issue 41
				if(disc.getPosition()> Netflix.discQueue.getTotalTitles()) {
					disc.setPosition(Netflix.discQueue.getTotalTitles());
				}
				// @ TODO   Move this to instnat once it works
				
				eTag = Netflix.discQueue.getETag();
				url = new URL("https://api.Netflix.com/users/" + user.getUserId()
						+ "/queues/disc" + expanders);
				
				break;
			case NetflixQueue.QUEUE_TYPE_INSTANT:
				eTag = Netflix.instantQueue.getETag();
				url = new URL("https://api.Netflix.com/users/" + user.getUserId()
						+ "/queues/instant" + expanders);
				break;
			}

			// Log.d("Netflix", "@URL: " + url.toString())
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs.add(new BasicNameValuePair("title_ref", disc.getId()));
			nameValuePairs.add(new BasicNameValuePair("position", ""+disc.getPosition()));
			nameValuePairs.add(new BasicNameValuePair("etag", eTag));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			postConsumer.sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);
			result = response.getStatusLine().getStatusCode();
			
			xml = response.getEntity().getContent();
			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();

			
			  Log.d("Netflix", "" +
			  response.getEntity().getContentType().toString()); BufferedReader
			  in = new BufferedReader(new InputStreamReader(xml)); String
			  linein = null; while ((linein = in.readLine()) != null) {
			  Log.d("Netflix", "AddMovie: " + linein); }
			 if(true) return 200;
			 //^ avoids the parser since we consumed xml for debug
			 
			 
			// Log.i("Netflix", "Parsing XML Response")
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;

			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler = null;
			switch (queueType) {
			case NetflixQueue.QUEUE_TYPE_DISC:
				myHandler = (AddDiscQueueHandler) new AddDiscQueueHandler();
				break;
			case NetflixQueue.QUEUE_TYPE_INSTANT:
				myHandler = (AddInstantQueueHandler) new AddInstantQueueHandler();
				break;
			}
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));

			result = myHandler.getSubCode(result);
			if( myHandler.getMessage() != null){
				//we may have an error from Netflix, check it
				lastResponseMessage+="  NF:" + result + ", " + myHandler.getMessage();
				lastNFResponseMessage = myHandler.getMessage();
			}else{
				lastNFResponseMessage= "No Message";
			}
			
			//extra code to catch 502
			
		
			
			
		} catch (IOException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (ParserConfigurationException e) {
			
			reportError(e, lastResponseMessage);
		} catch (SAXException e) {
			
			reportError(e, lastResponseMessage);
		}finally{
			if(result == 502){
				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("Queue Type:", ""+NetflixQueue.queueTypeText[queueType]);
				parameters.put("HTTP Result:", ""+ lastResponseMessage);
				parameters.put("User ID:", ""+ user.getUserId());
				parameters.put("Disc ID:", ""+disc.getId() );
				parameters.put("Position:", ""+disc.getPosition());
				parameters.put("Availability:", ""+ disc.isAvailable() + ", " + disc.getAvailibilityText());
				parameters.put("URL:", ""+ url);
				FlurryAgent.onEvent("AddToQueue502", parameters);			
				
			}
		}
		return result;
	}

	*//**
	 * moveInQueue This will post to Netflix with the new ddesired position.
	 * Disc q only 1 based index
	 * 
	 * @param disc
	 * @param oldPosition
	 * @param newPosition
	 * @param queueType
	 * @return subcode, statuscode, or httpresponse code (NF_ERROR_BAD_DEFAULT on exception)
	 *//*
	public int moveInQueue(Disc disc, int oldPosition, int newPosition, int queueType) {

		int result = NF_ERROR_BAD_DEFAULT;
		
		//getNewETag(queueType,newPosition);
		// 2 choirs, send request to Netflix, and if successful update local q.
		
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
		
		

		InputStream xml = null;
		URL url = null;
		try {

			url = new URL("http://api.Netflix.com/users/" + user.getUserId() + "/queues/" + NetflixQueue.queueTypeText[queueType]);
			Log.d("Netflix","Moving: " + url.toString());
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs
					.add(new BasicNameValuePair("title_ref", disc.getId()));
			nameValuePairs.add(new BasicNameValuePair("position", String
					.valueOf(newPosition)));
			nameValuePairs.add(new BasicNameValuePair("etag", Netflix.discQueue
					.getETag()));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			postConsumer.sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);

			xml = response.getEntity().getContent();
			
			
			result = response.getStatusLine().getStatusCode();
			
			  BufferedReader in = new BufferedReader(new
			  InputStreamReader(xml)); String linein = null; while ((linein =
			  in.readLine()) != null) { Log.d("Netflix", "Move Movie: " +
			  linein); }
			lastResponseMessage = "HTTP:" + response.getStatusLine().getStatusCode()
			+ ", " + response.getStatusLine().getReasonPhrase();
	
			if(result == 502){
				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("Queue Type:", ""+NetflixQueue.queueTypeText[queueType]);
				parameters.put("HTTP Result:", ""+ lastResponseMessage);
				parameters.put("User ID:", ""+ user.getUserId());
				parameters.put("Disc ID:", ""+disc.getId() );
				parameters.put("Positions:", ""+disc.getPosition() + " -> " + String
				.valueOf(newPosition));
				parameters.put("URL:", ""+ url);
				FlurryAgent.onEvent("MoveInQueue502", parameters);
				
					return result;
				
				
			}
			
			
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			MoveQueueHandler myHandler = new MoveQueueHandler(oldPosition);
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));
			// result=response.getStatusLine().getStatusCode();
			result = myHandler.getSubCode(result);
			
			if( myHandler.getMessage() != null ){
				//we may have an error from Netflix, check it
				lastResponseMessage+=  "  NF: " + result + ", " + myHandler.getMessage();
			}else{
				if(queueType == NetflixQueue.QUEUE_TYPE_DISC
						&& newPosition > (discQueue.getStartIndex()+discQueue.getPerPage())){
					// a disc queue and we have moved past our current viewing 
					// so we will remove it from viewing to prevnt confusion and removing mishpas (position will be lost)
					discQueue.delete(disc);
					result= MOVED_OUTSIDE_CURRENT_VIEW;
				}
			}
		} catch (IOException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (SAXException e) {
			
			reportError(e, lastResponseMessage);
		} catch (ParserConfigurationException e) {
			
			reportError(e, lastResponseMessage);
		}
		return result;
	}

	public int deleteFromQueue(Disc disc, int queueType) {
		int result = NF_ERROR_BAD_DEFAULT;
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());

		// postProvider.setOAuth10a(false);

		HttpClient httpclient = new DefaultHttpClient();

		URL QueueUrl;
		InputStream xml = null;

		try {
			QueueUrl = new URL(disc.getId());
			HttpDelete httpAction = new HttpDelete(QueueUrl.toString());
			postConsumer.sign(httpAction);

			HttpResponse response = httpclient.execute(httpAction);

			xml = response.getEntity().getContent();
			result = response.getStatusLine().getStatusCode();
			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();
			BufferedReader in = new BufferedReader(new InputStreamReader(xml));
			String linein = null;
			while ((linein = in.readLine()) != null) {
				Log.d("Netflix", "MovieMovie: " + linein);
			}
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler = new QueueHandler();
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));
			// result=response.getStatusLine().getStatusCode();
			result = myHandler.getSubCode(result);
			lastResponseMessage = "HTTP:" + response.getStatusLine().getStatusCode()
					+ ", " + response.getStatusLine().getReasonPhrase();
			
			if( myHandler.getMessage() != null ){
				//we may have an error from Netflix, check it
				lastResponseMessage+=  "  NF: " + result + ", " + myHandler.getMessage();
			}

		} catch (OAuthMessageSignerException e) {
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			reportError(e, lastResponseMessage);
		} catch (ClientProtocolException e) {
			reportError(e, lastResponseMessage);
		} catch (IOException e) {
			reportError(e, lastResponseMessage);
		} catch (SAXException e) {
			reportError(e, lastResponseMessage);
		} catch (ParserConfigurationException e) {
			reportError(e, lastResponseMessage);
		}

		
		
		 * On a successful; DELETE we remove our local recordss too
		 
		if (result == 200) {

			switch (queueType) {
			case NetflixQueue.QUEUE_TYPE_DISC:
				Netflix.discQueue.delete(disc);
				break;
			case NetflixQueue.QUEUE_TYPE_INSTANT:
				Netflix.instantQueue.delete(disc);
				break;
			}
			getNewETag(queueType);
		}
		return result;
	}*/

	private static void setSignPost(String token, String tokenSecret) {
		// Log.i("NetApi", "Prepping SignPosT class..")

		Netflix.oaconsumer.setTokenWithSecret(token, tokenSecret);
		// oaprovider.
	}

	
/*	
	*//**
	 * purge local copy of the specified queuetype, this will allow a fresh download next time it is requested
	 * @param queueType
	 *//*
	public void purgeQueue(int queueType) {
		switch (queueType) {
		case NetflixQueue.QUEUE_TYPE_DISC:
			discQueue.purge();
			break;
		case NetflixQueue.QUEUE_TYPE_INSTANT:
			instantQueue.purge();
			break;
		case NetflixQueue.QUEUE_TYPE_RECOMMEND:
			recomemendedQueue.purge();
			break;
		}
	}*/
	
	/**
	 * Post a rating to specificed title
	 * @param modifiedDisc
	 * @return SubCode, httpResponseCode or NF_ERROR_BAD_DEFAULT on exception
	 *//*
	public int setRating(Disc modifiedDisc) {
		
		int result = NF_ERROR_BAD_DEFAULT;
		// 2 choirs, send request to Netflix, and if successful update local q.
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
		
		// postProvider.setOAuth10a(false);
		InputStream xml = null;
		try {

			// Construct data
			
			 * Log.d("Netflix", "title_ref=" + URLEncoder.encode(disc.getId(),
			 * "UTF-8")); Log.d("Netflix", "etag=" +
			 * URLEncoder.encode(NetflixQueue.getETag(), "UTF-8"));
			 
			URL url = new URL("https://api.Netflix.com/users/" + user.getUserId()
						+ "/ratings/title/actual");
				

			// Log.d("Netflix", "@URL: " + url.toString())
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());

			String rating = (modifiedDisc.getUserRating() == 0) ? NF_RATING_NO_INTEREST : String.valueOf(modifiedDisc.getUserRating().intValue()); 
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Your DATA
			nameValuePairs.add(new BasicNameValuePair("title_ref", modifiedDisc.getId()));
			nameValuePairs.add(new BasicNameValuePair("rating", rating));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			postConsumer.sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);

			xml = response.getEntity().getContent();
			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();
			result=response.getStatusLine().getStatusCode();
			
			 Log.d("Netflix", "" +
			 response.getEntity().getContentType().toString()); BufferedReader
			 in = new BufferedReader(new InputStreamReader(xml)); String
			 linein = null; while ((linein = in.readLine()) != null) {
			 Log.d("Netflix", "SetRating: " + linein); }
			 
			// Log.i("Netflix", "Parsing XML Response")
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;

			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler =  new QueueHandler();
			
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));

			if(result == 201 || result == 422){
				switch(modifiedDisc.getQueueType()){
					//could be rating froms earch, recommends, instant, discm, or at home
				case NetflixQueue.QUEUE_TYPE_RECOMMEND:
					((Disc)recomemendedQueue.getDiscs().get(recomemendedQueue.indexOf(modifiedDisc))).setUserRating(modifiedDisc.getUserRating());
					break;
				case NetflixQueue.QUEUE_TYPE_INSTANT:
					((Disc)instantQueue.getDiscs().get(instantQueue.indexOf(modifiedDisc))).setUserRating(modifiedDisc.getUserRating());
					break;
				case NetflixQueue.QUEUE_TYPE_DISC:
					((Disc)discQueue.getDiscs().get(discQueue.indexOf(modifiedDisc))).setUserRating(modifiedDisc.getUserRating());
					break;
				}
			}
			
			lastNFResponseMessage = "NF: "+myHandler.getMessage();
			result = myHandler.getSubCode(result);

		} catch (IOException e) {
			
			reportError(e, lastResponseMessage);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e, lastResponseMessage);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e, lastResponseMessage);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			reportError(e, lastResponseMessage);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			reportError(e, lastResponseMessage);
		} 
		return result;
	}

	*/
/**
	 * report exceptions using Flurry
	 * @param e
	 */
	protected static void reportError(Exception e, String responseLine){
		//FlurryAgent.onError(String errorId, String message, String errorClass)
		FlurryAgent.onError("Netflix Exception", e.getLocalizedMessage() +  "|| But check this, I'm also looking at a " + responseLine + " here for an HTTP status. I'm just sayin", e.toString());
	}
	


	public boolean isConnected(){
		boolean result =false;
		try{
			URL url = new URL("http://api.Netflix.com/");
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.connect();
			Log.d("QM","isCOnnected: " + urlc.getResponseCode() );
			if (urlc.getResponseCode() == 200 || urlc.getResponseCode()==403) {
				result = true;
			}
			//urlc.disconnect();
		} catch (MalformedURLException e1) {
		        e1.printStackTrace();
		} catch (IOException e) {
		        reportError(e, lastResponseMessage);
		}
		return result;
	}

	/**
	 * Does NOT return signed request, but mutates the referenced object.
	 * 
	 * @param request unsignedRequest to be mutated
	 */
	public void sign(HttpURLConnection request) {
		// TODO Auto-generated method stub
		Log.d("Netflix","sign() >>>");
		setSignPost(user.oauthToken, user.oauthTokenSecret);
		try {
			oaconsumer.sign(request);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("Netflix","sign() <<<");
		
	}
	
	public void sign(HttpPost httpPost){
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
	
		try {
			postConsumer.sign(httpPost);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
