package edwardawebb.queueman.queues;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.NetflixResponse;
import edwardawebb.queueman.handlers.QueueHandler;


public abstract class MutableQueue extends Queue{
	
	protected String eTag;
	
	public MutableQueue(Netflix netflix) {
		super(netflix);
		// TODO Auto-generated constructor stub
	}

	public NetflixResponse postTitle(){
		return null;
		// TODO add implementation and return statement
	}

	public NetflixResponse moveTitle(){
		return null;
		

	}

	public NetflixResponse deleteTitle(){
		return null;
		// TODO add implementation and return statement
	}
	
		/**
	 * @return the eTag
	 */
	public String geteTag() {
		return eTag;
	}

	/**
	 * @param eTag the eTag to set
	 */
	public void seteTag(String eTag) {
		this.eTag = eTag;
	}
	
	
	/**Collection Mutators ********************************************************/
	
	
	


	/**
		 * Moves the disc at the specified queue position to the new position.
		 * Position is 1 based
		 * 
		 * @param oldPosition
		 * @param newPosition
		 */
	public void reorder(int oldPosition, int newPosition) {
		// always good to check to make sure the indices are ok.
		// we are being passed 'postions' 1,2,3....
		// but discs is 0 based, so down shift!
		if (oldPosition >= 1 && newPosition >= 1) {
			if (newPosition > titles.size()) {
				newPosition = titles.size();
			}
			Disc movie = titles.remove(oldPosition - 1);
			titles.add(newPosition - 1, movie);
		} else {
			FlurryAgent.onError("outOfBounds",
					"reorder: provided indices are out of bound. old:"
							+ oldPosition + ", new:" + newPosition,
					"NetFlixQueue");
		}

	}
	
	
	

	

	public NetflixResponse postTitle(Disc disc) {
		// 2 choirs, send request to Netflix, and if successful update local q.
		/*OAuthConsumer postConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,
				CONSUMER_SECRET, SignatureMethod.HMAC_SHA1);
		postConsumer.setTokenWithSecret(user.getAccessToken(), user.getAccessTokenSecret());
		OAuthProvider postProvider = new DefaultOAuthProvider(postConsumer,REQUEST_TOKEN_ENDPOINT_URL, ACCESS_TOKEN_ENDPOINT_URL,AUTHORIZE_WEBSITE_URL);
		*/
		NetflixResponse nfr = new NetflixResponse(0);
		String expanders = "?expand=synopsis,formats";
		InputStream xml = null;	
		
		try {
			//etag, url, handler, 
			// Construct data
			
			if(disc.getPosition()> getTotalTitles()) {
				disc.setPosition(getTotalTitles());
			}
			
			if(eTag == "") getNewETag(disc.getPosition());

			// Log.d("Netflix", "@URL: " + url.toString())
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(getQueueUrl(netflix.getUser()).toString());
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			// Your DATA
			nameValuePairs.add(new BasicNameValuePair("title_ref", disc.getId()));
			nameValuePairs.add(new BasicNameValuePair("position", ""+disc.getPosition()));
			nameValuePairs.add(new BasicNameValuePair("etag", eTag));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			netflix.sign(httppost);				
			
			HttpResponse response;
			response = httpclient.execute(httppost);
			
			nfr = new NetflixResponse( response.getStatusLine().getStatusCode());
			nfr.setHttpMessage(response.getStatusLine().getReasonPhrase());

			Log.d("Queue","AddToQUEUE Http:" + nfr.getHttpCode());

				xml = response.getEntity().getContent();
			 /* Log.d("Netflix", "" +
			  response.getEntity().getContentType().toString()); BufferedReader
			  in = new BufferedReader(new InputStreamReader(xml)); String
			  linein = null; while ((linein = in.readLine()) != null) {
			  Log.d("Netflix", "AddMovie: " + linein); }
			 if(true) return nfr;*/
			 //^ avoids the parser since we consumed xml for debug
			if(nfr.getHttpCode()==201) isCachedLocally=false; 
			 
			// Log.i("Netflix", "Parsing XML Response")
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;

			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler =getQueueHandler();
		
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));

			nfr.setNetflixCode(myHandler.getStatusCode());
			nfr.setNetflixSubCode(myHandler.getSubCode(0));
			if( myHandler.getMessage() != null){
				//we may have an error from Netflix, check it
				nfr.setNetflixMessage(myHandler.getMessage());
			}
			
			//extra code to catch 502
			
		
			
			
		} catch (IOException e) {
			
			reportError(e);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} catch (ParserConfigurationException e) {
			
			reportError(e);
		} catch (SAXException e) {
			
			reportError(e);
		}finally{
			/*if(result == 502){
				HashMap<String, String> parameters = new HashMap<String, String>();
				parameters.put("Queue Type:", ""+NetflixQueue.queueTypeText[queueType]);
				parameters.put("HTTP Result:", ""+ lastResponseMessage);
				parameters.put("User ID:", ""+ user.getUserId());
				parameters.put("Disc ID:", ""+disc.getId() );
				parameters.put("Position:", ""+disc.getPosition());
				parameters.put("Availability:", ""+ disc.isAvailable() + ", " + disc.getAvailibilityText());
				parameters.put("URL:", ""+ url);
				FlurryAgent.onEvent("AddToQueue502", parameters);			
				
			}*/
		}
		return nfr;
	}
	
	
	
	
	
}

