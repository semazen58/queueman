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
package edwardawebb.queueman.queues;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpRequest;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.core.QueueMan;
import edwardawebb.queueman.handlers.DiscQueueHandler;
import edwardawebb.queueman.handlers.InstantQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;
public abstract class Queue implements QueueInterface{
	protected String id;

	
	protected LinkedList<Disc> titles;

	protected int totalTitles;

	protected int startTitle;

	protected int pageCount;
	
	private int totalResults;

	protected String expanders;

	protected Netflix netflix;
	
	protected boolean isCachedLocally; //have we retieved the queue? (distiniguishes "built" queue from "downloaded"
	
	
	protected int resultCode=900; // queue result code, https most cases, use secondary variables for more detail
	protected int netflixResultCode=0; // queue result code, https most cases, use secondary variables for more detail
	protected int netflixSubCode=0; // queue result code, https most cases, use secondary variables for more detail
	protected String netflixMessage="";


	

	
	//***** Error Codes ****//
	public static final int NF_ERROR_BAD_DEFAULT=900; // defaukl return code
	public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final int NF_ERROR_NO_MORE=903; // asking for higher start then total results
	//public static final int NF_ERROR_BAD_INDEX=902; // seting rating not bewteen 1-5
	public static final String NF_RATING_NO_INTEREST = "not_interested";
	public static final int MOVED_OUTSIDE_CURRENT_VIEW = 299; // result code used when disc is moved outside our current range (and we need ot remove it)
	public static final int BOTTOM = 500;
	public static final int TOP=1;
	public static final int SUCCESS_FROM_CACHE = 255;
	
	
	


	
	/** require reference to netflix class\
	 * 
	 */
	public Queue(Netflix netflix){
		this.netflix=netflix;
	}
	
	
	/**
	 * 
	 * @param start
	 * @param end
	 * @param useCached
	 * @return
	 */
	public Collection<Disc> retreiveQueue(int start, int maxResults, boolean useCached){
		// TODO add implementation
		Log.d("Queue","retrieveQueue()>>>");
		resultCode = NF_ERROR_BAD_DEFAULT;
		
		// addtional info to return 
		String expanders = "?expand=synopsis,formats&max_results=" + maxResults;
		InputStream xml = null;
		if (!isCached()){
			resultCode = SUCCESS_FROM_CACHE;
			return getQueue();
		}
			
		try{
			URL QueueUrl = getQueueUrl(netflix.getUser());
			QueueHandler myQueueHandler = getQueueHandler();
			
			HttpURLConnection request = (HttpURLConnection) QueueUrl.openConnection();
			netflix.sign(request);
			request.connect();
	
			Log.d("Netflix","getQueue() | response");
			resultCode = request.getResponseCode() ;
			Log.d("Queue","Retrieve Result" + resultCode +":" + request.getResponseMessage());
			
			
			xml = request.getInputStream();
			
			/*  BufferedReader in = new BufferedReader(new
			  InputStreamReader(xml)); String linein = null; while ((linein =
			  in.readLine()) != null) { Log.d("Netflix", "GetQueue: " +
			  linein); }*/
			 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
	
			xr.setContentHandler(myQueueHandler);
	
			Log.d("Netflix","getQueue() | parse ready");
			xr.parse(new InputSource(xml));
	
			Log.d("Netflix","getQueue() | parse complete");
			netflixSubCode=myQueueHandler.getSubCode(0);
			
			if( myQueueHandler.getMessage() != null){
				//we may have an error from Netflix, check it
				netflixMessage = myQueueHandler.getMessage();
			}else{
				netflixMessage = "";
			}
			if(resultCode==200){
				isCachedLocally = true;
			}else if(resultCode == 502){
					HashMap<String, String> parameters = new HashMap<String, String>();
					parameters.put("Queue:", ""+this.toString());
					parameters.put("HTTP Result:", ""+ resultCode +":" + request.getResponseMessage());
					parameters.put("User ID:", ""+ netflix.getUser().getUserId());
					parameters.put("NF Message:", ""+ myQueueHandler.getMessage());
					FlurryAgent.onEvent("getQueue502", parameters);
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
		Log.d("Netflix","getQueue()<<<");
		return getQueue();

	}

	private void reportError(Exception e) {
		// TODO Auto-generated method stub
		FlurryAgent.onError("Queue Exception"
				, e.getLocalizedMessage() 
					+  "|http:" + resultCode 
					+ "|nfCde:" + netflixResultCode + "-" + netflixSubCode
					+ "|nfMsg:" + netflixMessage
					, e.toString());
			
	}


	public void applyFilter(){
		// TODO add implementation
	}

	public void removeFilter(){
		// TODO add implementation
	}

	public void nextPage(){
		// TODO add implementation
	}

	public void reset(){
		// TODO add implementation
	}
	
	
	//**** Err=or managerment, sorta ****//
	public int getResultCode(){
		return this.resultCode;
	}
	
	public int getNetflixCode(){
		return this.netflixResultCode;
	}

	public int getSubCode(){
		return this.netflixSubCode;
	}
	
	public String getNetflixMessage(){
		return this.netflixMessage;
	}
		
	
	
	
	protected List<Disc> getQueue(){
		return Collections.unmodifiableList(titles);
	}
	
	
	
	//***** private methods to encapsulate changes needed by concrete classes ***/
	
		
	protected abstract QueueHandler getQueueHandler();
	
	protected abstract URL getQueueUrl(User user) throws MalformedURLException;
	
	/**
	 * Is there a local copy to read instead of ffresh?
	 * @return false by default
	 */
	protected boolean isCached(){
		return this.isCachedLocally;
	}


	public void setTotalTitles(int totalResults) {
		// TODO Auto-generated method stub
		this.totalResults=totalResults;
	}


	public String getDiscs() {
		// TODO Auto-generated method stub
		return null;
	}


	public void add(Disc tempMovie) {
		// TODO Auto-generated method stub
		/* Filter recommendations to hide movies in our queues already **/
	
	}

}


