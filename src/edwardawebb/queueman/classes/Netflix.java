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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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
import oauth.signpost.signature.SignatureMethod;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.net.Uri;
import android.util.Log;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.apikeys.ApiKeys;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.UserHandler;



/** Singleton */
public class Netflix{
	protected User user;



	private static Netflix NETFLIX ; // the one instance, yes, a singleton

	private static final String APPLICATION_NAME = "QueueMan";
	// TODO you will need to make this class after chkecin out. 
	// See issue # 3
	// TODO !! NEVER check in the class, these need to remain private
	private static final String CONSUMER_KEY = ApiKeys.getConsumerKey();
	private static final String CONSUMER_SECRET = ApiKeys.getConsumerSecret();
	// for oAuth we van 'request_token' and then 'access_token'
	private static final String REQUEST_TOKEN_ENDPOINT_URL = "http://api.netflix.com/oauth/request_token";
	private static final String ACCESS_TOKEN_ENDPOINT_URL = "http://api.netflix.com/oauth/access_token";
	private static final String AUTHORIZE_WEBSITE_URL = "https://api-user.netflix.com/oauth/login";
	// queue types



	private OAuthConsumer oaconsumer;
	private OAuthProvider oaprovider;

	
	
	
	public static final int NF_ERROR_BAD_DEFAULT=900; // defaukl return code
	public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final int NF_ERROR_NO_MORE=903; // asking for higher start then total results
	//public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final String NF_RATING_NO_INTEREST = "not_interested";
	public static final int MOVED_OUTSIDE_CURRENT_VIEW = 299; // result code used when disc is moved outside our current range (and we need ot remove it)
	public static final int BOTTOM = 500;
	public static final int TOP=1;
	public static final int SUCCESS_FROM_CACHE = 255;
	
	synchronized
	public static Netflix getInstance(){
		// TODO add implementation and return statement
		if(NETFLIX != null){
			//  we already have the one and pnly instance created
		}else{
			// we got nothing, create the chosen one
			NETFLIX = new Netflix();
		}
			Log.d("Netflix", "NETFLIX: " + NETFLIX.toString());
			return NETFLIX;
		
	}
	/**
	 * For first time, we will need to GET request  tokens, and then request user details
	 */
	
	//@ TODO Use setters for stage.
	private Netflix( ){
		oaconsumer = new DefaultOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		oaprovider = new DefaultOAuthProvider(oaconsumer,
				REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
				AUTHORIZE_WEBSITE_URL);
		
		
	}
	
	/**
	 * second time, we will need to request access tokens from request token
	 */
	public void setRequestTokens(String requestToken, String requestSecret){
		
		oaconsumer.setTokenWithSecret(requestToken,requestSecret);
		
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
		Log.d("Netflix", "instance: " + this.toString());
		
		try {
			//oaconsumer.setTokenWithSecret("", "");
			// Log.d("Netflix","token end:"+oaprovider.getRequestTokenEndpointUrl());
			// get url for user to lologin and request token
			String tmp = oaprovider.retrieveRequestToken(callbackUrl);
			// Netflix.oaprovider.getResponseParameters().get("request_token");
			Log.d("Netflix","Url:"+tmp);
			result = Uri.parse(tmp + "&application_name=" + APPLICATION_NAME
					+ "&oauth_callback=" + URLEncoder.encode(callbackUrl)
					+ "&oauth_consumer_key="+CONSUMER_KEY 
					);
			
			
			
			Log.i("oauth", "request token:"
					+ result.getQueryParameter("oauth_token"));
			Log.i("oauth", "secret:"
					+ result.getQueryParameter("oauth_token_secret"));
		} catch (OAuthMessageSignerException e) {
			
			reportError(e);
		} catch (OAuthNotAuthorizedException e) {
			
			reportError(e);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e);
		} catch (OAuthCommunicationException e) {
			
			reportError(e);
			e.printStackTrace();
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
			oaprovider.retrieveAccessToken(requestToken);
			result = true;

			// dumpMap(oaprovider.getResponseParameters());
			// Log.d("oauth", "Access token: " + oaconsumer.getToken())
			// Log.d("oauth", "Token secret: " + oaconsumer.getTokenSecret())
			Log.d("oauth", "User ID:"
					+ oaprovider.getResponseParameters().get("user_id"));
			user = new User(oaprovider.getResponseParameters().get("user_id"),oaconsumer.getToken(),oaconsumer.getTokenSecret());
			
		} catch (OAuthMessageSignerException e) {
			
			reportError(e);
		} catch (OAuthNotAuthorizedException e) {
			
			reportError(e);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e);
		} catch (OAuthCommunicationException e) {
			
			reportError(e);
		}
		return result;
	}




	
	

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
			url = new URL("http://api.netflix.com/users/" + user.getUserId()
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
			
			reportError(e);
		} catch (SAXException e) {
			
			reportError(e);
		} catch (IOException e) {
			
			reportError(e);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e);
			// Log.i("Netflix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e);
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
	
				QueueUrl = new URL("http://api.netflix.com/users/" + user.getUserId());
				oaconsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
				//setSignPost(user.getAccessToken(), user.getAccessTokenSecret());
				
				HttpURLConnection request = (HttpURLConnection) QueueUrl
						.openConnection();
	
				oaconsumer.sign(request);

				Log.d("get",""+request.getURL());
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
				
				reportError(e);
			} catch (SAXException e) {
				
				reportError(e);
			} catch (IOException e) {
				
				reportError(e);
				// Log.i("Netflix", "IO Error connecting to Netflix queue")
			} catch (OAuthMessageSignerException e) {
				
				reportError(e);
				// Log.i("Netflix", "Unable to Sign request - token invalid")
			} catch (OAuthExpectationFailedException e) {
				
				reportError(e);
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
		return oaconsumer.getToken();
	}

	/**
	 * get token secret from conumser
	 * @return secret string
	 */
	public String getRTS(){
		return oaconsumer.getTokenSecret();
	}


/*
	//**
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

			url = new URL("http://api.netflix.com/users/" + user.getUserId() + "/queues/" + NetflixQueue.queueTypeText[queueType]);
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
			
			reportError(e);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (OAuthMessageSignerException e) {
			
			reportError(e);
		} catch (OAuthExpectationFailedException e) {
			
			reportError(e);
		} catch (SAXException e) {
			
			reportError(e);
		} catch (ParserConfigurationException e) {
			
			reportError(e);
		}
		return result;
	}
/*
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
			reportError(e);
		} catch (OAuthExpectationFailedException e) {
			reportError(e);
		} catch (ClientProtocolException e) {
			reportError(e);
		} catch (IOException e) {
			reportError(e);
		} catch (SAXException e) {
			reportError(e);
		} catch (ParserConfigurationException e) {
			reportError(e);
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
	 * report exceptions using Flurry
	 * @param e
	 */
	protected static void reportError(Exception e){
		//FlurryAgent.onError(String errorId, String message, String errorClass)
		FlurryAgent.onError("Netflix Exception", e.getLocalizedMessage() , e.toString());
	}
	


	public boolean isConnected(){
		boolean result =false;
		try{
			URL url = new URL("http://api.netflix.com/catalog");
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();			
			urlc.connect();
			Log.d("QM","isCOnnected: " + urlc.getResponseCode() );
			if (urlc.getResponseCode() == 200 || urlc.getResponseCode()==403) {
				result = true;
			}
			urlc.disconnect();
		} catch (MalformedURLException e1) {
		        e1.printStackTrace();
		} catch (IOException e) {
		        reportError(e);
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
		/*OAuthConsumer oaconsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);*/
		oaconsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
	
		//setSignPost(user.getAccessToken(), user.getAccessTokenSecret());
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
	public void sign(HttpPut httpput) {
		// TODO Auto-generated method stub
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
	
		try {
			postConsumer.sign(httpput);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
