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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.flurry.android.FlurryAgent;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.ErrorProcessor;
import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.QueueHandler;
public abstract class Queue implements QueueInterface{
	
	
	protected LinkedList<Disc> titles = new LinkedList();

	protected int maxTitles=10;// aka max results, results per page

	protected int startIndex=0;

	protected int pageCount=1; //what page we on
	
	protected int totalResults=-1;	

	private int firstVisibleItem=0; // used when vewing details or lading more titles, puts user back at saame spot in queue

	protected String expanders="";

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


	public static final int QUEUE_TYPE_DISC = 1;
	public static final int QUEUE_TYPE_INSTANT = 2;
	public static final int QUEUE_TYPE_HOME = 3;
	
	
	


	
	/** require reference to netflix class\
	 * 
	 */
	public Queue(Netflix netflix){
		this.netflix=netflix;
		this.expanders = "&expand=synopsis,formats";
		
	}
	
	public void setStartIndex(int start){
		this.startIndex=start;
		isCachedLocally=false; // changing start or page value(max downlaods) invalidates current cache
	}
	public int getStartIndex(){
		return this.startIndex;
	}
	
	
	/**
	 * @param maxTitles the maxTitles to set
	 */
	public void setMaxTitles(int maxTitles) {
		this.maxTitles = maxTitles;
		isCachedLocally=false; // changing start or page value(max downlaods) invalidates current cache
	}

	/**
	 * @return the maxTitles
	 */
	public int getMaxTitles() {
		return maxTitles;
	}

	
	
	public List<Disc> retreiveQueue(){
		return retreiveQueue(startIndex, maxTitles, true);
	}
/**
	 * This returns the queue, loading from 
	 * @param start
	 * @param end
	 * @param useCached
	 * @return
	 */
	public List<Disc> retreiveQueue(int start, int maxResults, boolean useCached){
		
		// TODO add implementation
		Log.d("Queue","retrieveQueue()>>>");
		resultCode = NF_ERROR_BAD_DEFAULT;
		
		// addtional info to return 
		expanders = "?expand=synopsis,formats&start_index=" + startIndex + "&max_results=" + maxResults;
		InputStream xml = null;
		if (isCached()){
			resultCode = SUCCESS_FROM_CACHE;
			return getQueue();
		}
			
		try{
			URL QueueUrl = getQueueUrl(netflix.getUser());
			QueueHandler myQueueHandler = getQueueHandler();
			
			HttpURLConnection request = (HttpURLConnection) QueueUrl.openConnection();
			netflix.sign(request);
			Log.d("get",""+request.getURL());
			request.connect();
	
			Log.d("Netflix","getQueue() | response");
			resultCode = request.getResponseCode() ;
			Log.d("Queue","Retrieve Result" + resultCode +":" + request.getResponseMessage());
			
			
			xml = request.getInputStream();
			
			  /*BufferedReader in = new BufferedReader(new
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
				this.maxTitles=maxResults;
				this.startIndex=startIndex;
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
		}
		Log.d("Netflix","getQueue()<<<");
		return getQueue();

	}
	
	
	public boolean getNewETag(int discPosition) {
		URL QueueUrl = null;
		DefaultHandler myQueueHandler = null;
		boolean result = false;
		//start index is 0 based, so step the true position down one
		expanders = "?max_results="+ 1   + "&start_index="+ (discPosition-1);
		InputStream xml = null;
		try{
			QueueUrl = getQueueUrl(netflix.getUser());
			myQueueHandler = getQueueHandler();
				
			
			
			HttpURLConnection request = (HttpURLConnection) QueueUrl
					.openConnection();

			netflix.sign(request);
			request.connect();

			

			if (request.getResponseCode() == 200) {
				xml = request.getInputStream();

				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp;
				sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				xr.setContentHandler(myQueueHandler);
				//our custom handler will throw an exception when he gets what he want, interupting the full parse
			 ErrorProcessor errors = new ErrorProcessor(); 
			 xr.setErrorHandler(errors);
				xr.parse(new InputSource(xml));
				result = true;
			}

		} catch (ParserConfigurationException e) {
			
			reportError(e);
		} catch (SAXException e) {
			
			reportError(e);
		} catch (IOException e) {
			
			reportError(e);
			// Log.i("Netflix", "IO Error connecting to Netflix queue")
		} 
		return result;
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
	
	//must override and specify quuee handler for sax parser	
	protected abstract QueueHandler getQueueHandler();
	
	/**
	 * Must override and specify full url to resource (without expanders) See {@link #getQueueUrl(User)} as example
	 * @param user
	 * @return
	 * @throws MalformedURLException
	 */
	protected abstract URL getQueueUrl(User user) throws MalformedURLException;
	
/**
 * Returns nice string name for type of queueue
 * @return
 */
	protected abstract String getPrettyName();

	
	
	
	
	
	
	
	
	
	
	/**
 * @param firstVisibleItem the firstVisibleItem to set
 */
public void setFirstVisibleItem(int firstVisibleItem) {
	this.firstVisibleItem = firstVisibleItem;
}

/**
 * @return the firstVisibleItem
 */
public int getFirstVisibleItem() {
	return firstVisibleItem;
}

	@Override
	public boolean equals(Object o){
		if (o instanceof Queue ) ;
		else return false;
		//@ TODO  FIX THIS!! use some guid for id.
		// if the type is the same, the queue is the same, need ID, PURELY FOR STUBBING
		return ((Queue)o).getPrettyName().equals(getPrettyName());
		
	}
	
	
	
	
	
	
	
	
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




	public boolean isEmpty() {
		// Log.d("NetFlixQueue","Size:"+discs.size())
		return (boolean) (titles.size() == 0);
	}




	public void delete(Disc movie) {
		// netflix.deleteFromQueue(movie);
		titles.remove(movie);

	}


	public void add(Disc tempMovie) {
		// TODO Auto-generated method stub
		/* Filter recommendations to hide movies in our queues already **/
		titles.add(tempMovie);
	}

	/**
	 * Adds new disc to Queue Position specified. position is 1 based.
	 * 
	 * @param position
	 * @param movie
	 */
	public void add(int position, Disc movie) {
		if(titles.contains(movie)){
			titles.remove(movie);
		}
		if (position >= 1 && position <= this.titles.size()) {
			titles.add(position - 1, movie);
		} else if (position < 1 ) {
			FlurryAgent.onError("outOfBounds",
					"Add: The provided Position was too low", "NetFlixQueue");
		} else if ( position == 500){
			//;eddie was lazy, moze to the end.(for new move to bootom feature)
			titles.add(movie);
		}
	}

	public void purgeQueue(){
		if(titles!=null)titles.clear();
		this.isCachedLocally=false;
		this.pageCount=-1;
		this.totalResults=-1;
		this.startIndex=-1;
	}
	public int indexOf(Disc movie) {
		return titles.indexOf(movie);
	}
	
	
	/**
	 * Returns Queue Position of title (1 based)
	 * 
	 * @param movie
	 * @return
	 */
	public int positionOf(Disc movie) {
		return titles.indexOf(movie) + 1;
	}
	
	public int getTotalTitles() {
		// TODO Auto-generated method stub
		return totalResults;
	}
	
	
}



