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
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;

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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import edwardawebb.queueman.apikeys.ApiKeys;
import edwardawebb.queueman.core.QueueMan;
import edwardawebb.queueman.handlers.AddDiscQueueHandler;
import edwardawebb.queueman.handlers.AddInstantQueueHandler;
import edwardawebb.queueman.handlers.DiscETagHandler;
import edwardawebb.queueman.handlers.DiscQueueHandler;
import edwardawebb.queueman.handlers.InstantETagHandler;
import edwardawebb.queueman.handlers.InstantQueueHandler;
import edwardawebb.queueman.handlers.MoveQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.SearchQueueHandler;
import edwardawebb.queueman.handlers.UserHandler;

/**
 * @author Edward A. Webb - http://edwardawebb.com
 * 
 */
public class NetFlix {
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

	public static NetFlixQueue searchQueue = new NetFlixQueue(
			NetFlixQueue.QUEUE_TYPE_SEARCH);
	public static NetFlixQueue discQueue = new NetFlixQueue(
			NetFlixQueue.QUEUE_TYPE_DISC);
	public static NetFlixQueue instantQueue = new NetFlixQueue(
			NetFlixQueue.QUEUE_TYPE_INSTANT);

	public static OAuthConsumer oaconsumer;
	private static OAuthProvider oaprovider;

	private static String oathAccessToken;
	private static String userID ;
	private static String oathAccessTokenSecret;
	private static String resultStatus;
	public String lastResponseMessage = "none";

	public NetFlix() {// OAuthConsumer oac, OAuthProvider oap
		// maybe pass in authentication info
		// NetFlix.oaconsumer = oac;
		// NetFlix.oaprovider = oap;
		NetFlix.oaconsumer = new DefaultOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		NetFlix.oaprovider = new DefaultOAuthProvider(oaconsumer,
				REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
				AUTHORIZE_WEBSITE_URL);

		// oaprovider.setOAuth10a(false);

		// need to call authenticate before this is usable
	}
	public NetFlix(String requestToken,String requestTokenSecret) {// OAuthConsumer oac, OAuthProvider oap
		// maybe pass in authentication info
		// NetFlix.oaconsumer = oac;
		// NetFlix.oaprovider = oap;
		NetFlix.oaconsumer = new DefaultOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		NetFlix.oaprovider = new DefaultOAuthProvider(oaconsumer,
				REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
				AUTHORIZE_WEBSITE_URL);

		NetFlix.oaconsumer.setTokenWithSecret(requestToken, requestTokenSecret);
		// oaprovider.setOAuth10a(false);

		// need to call authenticate before this is usable
	}

	
	public Uri getRequestLoginUri() {
		Uri result = null;

		// we'll always need a request and access token (I think)
		// callbak only if users first time, but needed to get request token, so
		// aove may be wrong
		String callbackUrl = "flixman:///";// get url for user to link netflix

		// NetFlix.oaprovider.setOAuth10a(false);

		try {

			// Log.d("NetFlix","token end:"+oaprovider.getRequestTokenEndpointUrl());
			// get url for user to lologin and request token
			String tmp = NetFlix.oaprovider.retrieveRequestToken(callbackUrl);
			// NetFlix.oaprovider.getResponseParameters().get("request_token");
			// Log.d("NetFlix","Url:"+tmp)
			result = Uri.parse(tmp + "&application_name=" + APPLICATION_NAME
					+ "&oauth_consumer_key="+CONSUMER_KEY);
			
			
			
			Log.i("oauth", "request token:"
					+ result.getQueryParameter("oauth_token"));
			Log.i("oauth", "secret:"
					+ result.getQueryParameter("oauth_token_secret"));
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			
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
			// NetFlix.oaconsumer.setTokenWithSecret(arg0, arg1)
			NetFlix.oaprovider.retrieveAccessToken(requestToken);
			result = true;

			// dumpMap(oaprovider.getResponseParameters());
			// Log.d("oauth", "Access token: " + oaconsumer.getToken())
			// Log.d("oauth", "Token secret: " + oaconsumer.getTokenSecret())
			Log.d("oauth", "User ID:"
					+ oaprovider.getResponseParameters().get("user_id"));

			NetFlix.oathAccessToken = oaconsumer.getToken();
			NetFlix.oathAccessTokenSecret = oaconsumer.getTokenSecret();
			NetFlix.userID = oaprovider.getResponseParameters().get("user_id");
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * this sets the instance of netflix with give user and token
	 */
	public void createUser(String userId, String accessToken,
			String accessTokenSecret) {
		// set instance variables
		NetFlix.oathAccessToken = accessToken;
		NetFlix.oathAccessTokenSecret = accessTokenSecret;
		NetFlix.userID = userId;

	}

	public String getAccessToken() {
		return oathAccessToken;
	}

	public String getUserID() {
		return userID;
	}

	public String getAccessTokenSecret() {
		return oathAccessTokenSecret;
	}

	public boolean isOnline() {
		// there mjst be a cleaner way to check for an internet connection
		boolean result = true;
		URL url;
		HttpURLConnection request;
		try {
			url = new URL("http://google.com");
			request = (HttpURLConnection) url.openConnection();
			request.connect();
		} catch (MalformedURLException e1) {
			
			e1.printStackTrace();
			result = false;
		} catch (IOException e1) {
			
			e1.printStackTrace();
			// Log.i("NetFlix", "Unable to connect!")
			result = false;
		}

		return result;
	}

	/**
	 * 
	 * @param queueType
	 * @param maxResults
	 * @return HttpStatusCOde or 900 for exceptions
	 */
	public int getQueue(int queueType, String maxResults) {
		URL QueueUrl = null;
		QueueHandler myQueueHandler = null;
		int result = 900;
		if (maxResults.equals(QueueMan.ALL_TITLES_STRING)) {
			maxResults = "500";
		}
		String expanders = "?expand=synopsis,formats&max_results=" + maxResults;
		InputStream xml = null;
		try {
			switch (queueType) {
			case NetFlixQueue.QUEUE_TYPE_INSTANT:
				if (!NetFlix.instantQueue.isEmpty())
					return 200;
				QueueUrl = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/instant" + expanders);
				myQueueHandler = new InstantQueueHandler();
				break;
			case NetFlixQueue.QUEUE_TYPE_DISC:
				if (!NetFlix.discQueue.isEmpty())
					return 200;
				QueueUrl = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/disc" + expanders);

				myQueueHandler = new DiscQueueHandler();
				break;
			}
			Log.d("netFlix", "" + QueueUrl.toString());
			setSignPost(oathAccessToken, oathAccessTokenSecret);
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			NetFlix.oaconsumer.sign(request);
			request.connect();

			lastResponseMessage = request.getResponseCode() + ": "
					+ request.getResponseMessage();
			result = request.getResponseCode();
			xml = request.getInputStream();
			/*
			 * BufferedReader in = new BufferedReader(new
			 * InputStreamReader(xml)); String linein = null; while ((linein =
			 * in.readLine()) != null) { Log.d("NetFlix", "GetQueue: " +
			 * linein); }
			 */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			xr.setContentHandler(myQueueHandler);
			xr.parse(new InputSource(xml));

		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Expectation failed")
		}
		return result;
	}

	/**
	 * 
	 * @param queueType
	 * @param maxResults
	 * @return HttpStatusCOde or 900 for exceptions
	 */
	public int getTitleState(String titleID) {
		URL url = null;
		QueueHandler myQueueHandler = null;
		int result = 900;

		String expanders = "?title_refs=" + titleID;
		InputStream xml = null;
		try {
			url = new URL("http://api.netflix.com/users/" + userID
					+ "/title_states" + expanders);
			// myQueueHandler = new InstantQueueHandler();
			Log.d("NetFlix", "" + url.toString());
			setSignPost(oathAccessToken, oathAccessTokenSecret);
			HttpURLConnection request = (HttpURLConnection) url
					.openConnection();

			NetFlix.oaconsumer.sign(request);
			request.connect();

			lastResponseMessage = request.getResponseCode() + ": "
					+ request.getResponseMessage();
			result = request.getResponseCode();
			xml = request.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(xml));
			String linein = null;
			while ((linein = in.readLine()) != null) {
				Log.d("NetFlix", "GetTitle States: " + linein);
			}
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			xr.setContentHandler(myQueueHandler);
			xr.parse(new InputSource(xml));

		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Expectation failed")
		}
		return result;
	}

	public boolean getNewETag(int queueType) {
		URL QueueUrl = null;
		DefaultHandler myQueueHandler = null;
		boolean result = false;
		String expanders = "?max_results=1";
		InputStream xml = null;
		try {
			switch (queueType) {
			case NetFlixQueue.QUEUE_TYPE_INSTANT:
				QueueUrl = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/instant" + expanders);
				myQueueHandler = new InstantETagHandler();
				break;
			case NetFlixQueue.QUEUE_TYPE_DISC:
				QueueUrl = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/disc" + expanders);

				myQueueHandler = new DiscETagHandler();
				break;
			}
			setSignPost(oathAccessToken, oathAccessTokenSecret);
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			NetFlix.oaconsumer.sign(request);
			request.connect();

			lastResponseMessage = request.getResponseCode() + ": "
					+ request.getResponseMessage();

			if (request.getResponseCode() == 200) {
				xml = request.getInputStream();

				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp;
				sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				xr.setContentHandler(myQueueHandler);
				xr.parse(new InputSource(xml));
				result = true;
			}

		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Expectation failed")
		}
		return result;
	}

	public static NetFlixQueue getSearchResults(String searchTerm) {
		searchQueue = new NetFlixQueue(NetFlixQueue.QUEUE_TYPE_SEARCH);
		setSignPost(oathAccessToken, oathAccessTokenSecret);

		InputStream xml = null;
		try {
			String encSearchTerm = URLEncoder.encode(searchTerm);
			setSignPost(oathAccessToken, oathAccessTokenSecret);
			String expanders = "&expand=synopsis,formats";
			URL QueueUrl = null;

			QueueUrl = new URL("http://api.netflix.com/catalog/titles?term="
					+ encSearchTerm + expanders);
			// Log.d("NetFlix",""+QueueUrl.toString())
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			NetFlix.oaconsumer.sign(request);
			request.connect();

			if (request.getResponseCode() == 200) {
				// Log.d("NetFlix", request.getContentType())
				// //Log.d("NetFlix",request.getInputStream().toString())
				// return xml xmldoc
				xml = request.getInputStream();

				/*BufferedReader in = new BufferedReader(new InputStreamReader(
						xml));
				String linein = null;
				while ((linein = in.readLine()) != null) {
					Log.d("NetFlix", "SearchMovie: " + linein);
				}*/
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp;

				sp = spf.newSAXParser();

				XMLReader xr = sp.getXMLReader();
				// SearchResultsHandler myHandler = new
				// SearchResultsHandler(this);
				SearchQueueHandler myHandler = new SearchQueueHandler();
				xr.setContentHandler(myHandler);
				xr.parse(new InputSource(xml));
			}

		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		}

		return searchQueue;
	}

	/**
	 * 
	 * @param disc
	 * @param queueType
	 * @return SubCode, httpResponseCode or 900 on exception
	 */
	public int addToQueue(Disc disc, int queueType) {
		int result = 900;
		// 2 choirs, send request to netflix, and if successful update local q.
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(oathAccessToken, oathAccessTokenSecret);
		OAuthProvider postProvider = new DefaultOAuthProvider(postConsumer,
				REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
				AUTHORIZE_WEBSITE_URL);
		// postProvider.setOAuth10a(false);

		String expanders = "?expand=synopsis,formats";
		InputStream xml = null;
		NetFlixQueue queue = null;
		URL QueueUrl = null;
		String eTag = null;
		try {

			// Construct data
			/*
			 * Log.d("NetFlix", "title_ref=" + URLEncoder.encode(disc.getId(),
			 * "UTF-8")); Log.d("NetFlix", "etag=" +
			 * URLEncoder.encode(NetFlixQueue.getETag(), "UTF-8"));
			 */
			URL url = null;
			switch (queueType) {
			case NetFlixQueue.QUEUE_TYPE_DISC:
				url = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/disc" + expanders);
				eTag = NetFlix.discQueue.getETag();
				break;
			case NetFlixQueue.QUEUE_TYPE_INSTANT:
				url = new URL("http://api.netflix.com/users/" + userID
						+ "/queues/instant" + expanders);
				eTag = NetFlix.instantQueue.getETag();
				break;
			}

			// Log.d("NetFlix", "@URL: " + url.toString())
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			postConsumer.setTokenWithSecret(oathAccessToken,
					oathAccessTokenSecret);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs
					.add(new BasicNameValuePair("title_ref", disc.getId()));
			nameValuePairs.add(new BasicNameValuePair("position", "1"));
			nameValuePairs.add(new BasicNameValuePair("etag", eTag));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			postConsumer.sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);

			xml = response.getEntity().getContent();
			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();

			/*
			 * Log.d("NetFlix", "" +
			 * response.getEntity().getContentType().toString()); BufferedReader
			 * in = new BufferedReader(new InputStreamReader(xml)); String
			 * linein = null; while ((linein = in.readLine()) != null) {
			 * Log.d("NetFlix", "AddMovie: " + linein); }
			 */
			// Log.i("NetFlix", "Parsing XML Response")
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;

			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler = null;
			switch (queueType) {
			case NetFlixQueue.QUEUE_TYPE_DISC:
				myHandler = (AddDiscQueueHandler) new AddDiscQueueHandler();
				break;
			case NetFlixQueue.QUEUE_TYPE_INSTANT:
				myHandler = new AddInstantQueueHandler();
				break;
			}
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));

			result = myHandler.getSubCode();

		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * moveInQueue This will post to netflix with the new ddesired position.
	 * Disc q only 1 based index
	 * 
	 * @param disc
	 * @param newPosition
	 * @return subcode, statuscode, or httpresponse code (900 on exception)
	 */
	public int moveInQueue(Disc disc, int oldPosition, int newPosition) {

		int result = 900;
		// 2 choirs, send request to netflix, and if successful update local q.
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(oathAccessToken, oathAccessTokenSecret);
		/*
		 * OAuthProvider postProvider = new DefaultOAuthProvider(postConsumer,
		 * REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,
		 * AUTHORIZE_WEBSITE_URL);
		 */
		// postProvider.setOAuth10a(false);

		InputStream xml = null;
		try {

			URL url = new URL(disc.getId());

			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			postConsumer.setTokenWithSecret(oathAccessToken,
					oathAccessTokenSecret);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs
					.add(new BasicNameValuePair("title_ref", disc.getId()));
			nameValuePairs.add(new BasicNameValuePair("position", String
					.valueOf(newPosition)));
			nameValuePairs.add(new BasicNameValuePair("etag", NetFlix.discQueue
					.getETag()));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			postConsumer.sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);

			xml = response.getEntity().getContent();

			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();

			/*
			 * BufferedReader in = new BufferedReader(new
			 * InputStreamReader(xml)); String linein = null; while ((linein =
			 * in.readLine()) != null) { Log.d("NetFlix", "MovieMovie: " +
			 * linein); }
			 */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			MoveQueueHandler myHandler = new MoveQueueHandler(oldPosition);
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));
			// result=response.getStatusLine().getStatusCode();
			result = myHandler.getSubCode();

		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		}
		return result;
	}

	public int deleteFromQueue(Disc disc, int queueType) {
		int result = 900;
		OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(oathAccessToken, oathAccessTokenSecret);

		// postProvider.setOAuth10a(false);

		HttpClient httpclient = new DefaultHttpClient();

		URL QueueUrl;
		InputStream xml = null;

		int statusCode = 0;
		try {
			QueueUrl = new URL(disc.getId());
			HttpDelete httpAction = new HttpDelete(QueueUrl.toString());
			postConsumer.sign(httpAction);

			HttpResponse response = httpclient.execute(httpAction);

			xml = response.getEntity().getContent();
			statusCode = response.getStatusLine().getStatusCode();
			lastResponseMessage = response.getStatusLine().getStatusCode()
					+ ": " + response.getStatusLine().getReasonPhrase();
			BufferedReader in = new BufferedReader(new InputStreamReader(xml));
			String linein = null;
			while ((linein = in.readLine()) != null) {
				Log.d("NetFlix", "MovieMovie: " + linein);
			}

		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		result = statusCode;

		if (statusCode == 200) {

			switch (queueType) {
			case NetFlixQueue.QUEUE_TYPE_DISC:
				NetFlix.discQueue.delete(disc);
				break;
			case NetFlixQueue.QUEUE_TYPE_INSTANT:
				NetFlix.instantQueue.delete(disc);
				break;
			}
			getNewETag(queueType);
		}
		return result;
	}

	private static void setSignPost(String token, String tokenSecret) {
		// Log.i("NetApi", "Prepping SignPosT class..")

		NetFlix.oaconsumer.setTokenWithSecret(token, tokenSecret);
		// oaprovider.
	}

	public void purgeQueue(int queueType) {
		switch (queueType) {
		case NetFlixQueue.QUEUE_TYPE_DISC:
			discQueue.purge();
			break;
		case NetFlixQueue.QUEUE_TYPE_INSTANT:
			instantQueue.purge();
			break;
		}
	}

	public static void setResultStatus(String status) {
		NetFlix.resultStatus = status;
	}

	public boolean getWatchInstant() {
		URL QueueUrl = null;
		UserHandler myHandler = new UserHandler();
		boolean result = false;
		InputStream xml = null;
		try {

			QueueUrl = new URL("http://api.netflix.com/users/" + userID);

			setSignPost(oathAccessToken, oathAccessTokenSecret);
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			NetFlix.oaconsumer.sign(request);
			request.connect();

			if (request.getResponseCode() == 200) {
				xml = request.getInputStream();

				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp;
				sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				xr.setContentHandler(myHandler);
				xr.parse(new InputSource(xml));
				result = myHandler.canWatch();
			}

		} catch (ParserConfigurationException e) {
			
			e.printStackTrace();
		} catch (SAXException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "IO Error connecting to NetFlix queue")
		} catch (OAuthMessageSignerException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Unable to Sign request - token invalid")
		} catch (OAuthExpectationFailedException e) {
			
			e.printStackTrace();
			// Log.i("NetFlix", "Expectation failed")
		}
		return result;
	}

	public String getRT(){
		return NetFlix.oaconsumer.getToken();
	}
	public String getRTS(){
		return NetFlix.oaconsumer.getTokenSecret();
	}
}
