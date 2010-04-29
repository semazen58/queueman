/**
 *     This file is part of QueueMan.
 *
 *        QueueMan is free software: you can redistribute it and/or modify
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
 /**
 * 
 */
package edwardawebb.queueman.classes;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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

import edwardawebb.queueman.handlers.QueueHandler;



/**
 * @author Eddie Webb
 * 
 * 
 */
public class Disc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1358508419381294263L;
	private static final CharSequence TINY_SIZE = "tiny";
	private static final CharSequence LARGE_SIZE = "large";
	private static final CharSequence MED_SIZE = "small";
	private String id;
	private String boxArtUrl;
	private String year;
	private String mpaaRating="";
	private Date availableFrom;
	private Date availableUntil;
	private boolean isAvailable;
	private String availibilityText;
	private boolean hasUserRating=false;
	private String uniqueId;
	private boolean isAvailableInstant=false;
	private int queueType;//self awareness for adding new titles - not used by existing discs
	
	
	protected String shortTitle;

	protected String fullTitle;

	protected ArrayList<String> formats;

	protected String synopsis;

	protected double userRating;

	protected double avgRating;

	protected int position;

	protected double suggestedRating;

	protected Date waitTime;

	protected boolean hasBluRay;

	protected boolean hasInstant;

	
	public static final String NF_RATING_NO_INTEREST = "not_interested";
	
	
	
	
	public NetflixResponse rateTitle(){
		return null;
		// TODO add implementation and return statement
	}

	/*
	 * searc
	 */
	public Disc(String id, String shortTitle, String fullTitle,
			String boxArtUrl, Double avgRating) {
		this.id = id;
		this.shortTitle = shortTitle;
		this.fullTitle = fullTitle;
		this.boxArtUrl = boxArtUrl;
		this.avgRating = avgRating;
	}

	
	/**
	 * @param id // varies  by queue type (recomends, search, etc)
	 * @param uniqueId // same across all ex. http://api.netflix.com/catalog/titles/movies/243547
	 * @param shortTitle
	 * @param fullTitle
	 * @param boxArtUrl
	 * @param rating
	 * @param synopsis
	 * @param year
	 * @param isAvailable
	 */
	public Disc(String id,String uniqueId, String shortTitle, String fullTitle,
			String boxArtUrl, Double rating, String synopsis, String year,
			boolean isAvailable) {
		this.id = id;
		this.uniqueId=uniqueId;
		this.shortTitle = shortTitle;
		this.fullTitle = fullTitle;
		this.boxArtUrl = boxArtUrl;
		this.avgRating = rating;
		this.synopsis = synopsis;
		this.year = year;
		this.setAvailable(isAvailable);
	}

	public String getSynopsis() {
		return synopsis;
	}

	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}


	public String getId() {
		return id;
	}

	public String getFullTitle() {
		return fullTitle;
	}

	public String getShortTitle() {
		return shortTitle;
	}

	public String getBoxArtUrl() {
		return boxArtUrl;
	}

	public String getBoxArtUrlMedium() {
		return boxArtUrl.replace(TINY_SIZE, MED_SIZE);
	}

	public String getBoxArtUrlLarge() {
		return boxArtUrl.replace(TINY_SIZE, LARGE_SIZE);
	}

	public Double getAvgRating() {
		return avgRating;
	}

	public void setFormats(ArrayList<String> formats) {
		this.formats = formats;
	}

	public ArrayList<String> getFormats() {
		return formats;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailibilityText(String availibilityText) {
		this.availibilityText = availibilityText;
	}

	public String getAvailibilityText() {
		return availibilityText;
	}


	/**
	 * @return the queueType
	 */
	public int getQueueType() {
		return queueType;
	}


	/**
	 * @param queueType the queueType to set
	 */
	public void setQueueType(int queueType) {
		this.queueType = queueType;
	}


	/**
	 * @param userRating the userRating to set
	 */
	public void setUserRating(double userRating) {
		if(userRating <=5 && userRating >=0 ){
			this.userRating = userRating;		
			hasUserRating=true;
		}
	}

	/**
	 * @return the userRating
	 */
	public Double getUserRating() {
		return userRating;
	}
	
	/**
	 * @return the uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}


	public Boolean hasUserRating(){
		return hasUserRating;
	}
	
	/*
	 * used by array adapter to show movie title
	 */
	@Override
	public String toString() {
		
		if(availibilityText==null){
			return shortTitle + " |" + getMpaaRating() + "|";
		}else if( availibilityText.equals("available now")){
			return shortTitle+ " |" + getMpaaRating() + "|";
		}else{
			return "Saved - " + shortTitle+ " |" + getMpaaRating() + "|";
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Disc){
			Disc mdisc=(Disc)o;
			return (this.uniqueId.equals(mdisc.uniqueId));			
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return uniqueId.hashCode();
	}


	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}


	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}


	/**
	 * @param isAvailableInstant the isAvailableInstant to set
	 */
	public void setAvailableInstant(boolean isAvailableInstant) {
		this.isAvailableInstant = isAvailableInstant;
	}


	/**
	 * @return the isAvailableInstant
	 */
	public boolean isAvailableInstant() {
		return isAvailableInstant;
	}


	/**
	 * @param mpaaRating the mpaaRating to set
	 */
	public void setMpaaRating(String mpaaRating) {
		this.mpaaRating = mpaaRating;
	}


	/**
	 * @return the mpaaRating
	 */
	public String getMpaaRating() {
		if(!mpaaRating.equals("")){
			return mpaaRating;
		}else{
			return "?";
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Post a rating to specificed title
	 * @param modifiedDisc
	 * @return SubCode, httpResponseCode or NF_ERROR_BAD_DEFAULT on exception
	 */
	public NetflixResponse setRating(Disc modifiedDisc) {
		
		
		NetflixResponse  nfResponse= new NetflixResponse(900);
		InputStream xml = null;
		try {

			// Construct data
			/*
			 * Log.d("NetFlix", "title_ref=" + URLEncoder.encode(disc.getId(),
			 * "UTF-8")); Log.d("NetFlix", "etag=" +
			 * URLEncoder.encode(NetFlixQueue.getETag(), "UTF-8"));
			 */
			URL url = new URL("http://api.netflix.com/users/" + Netflix.getInstance().getUser().getUserId()
					+ "/ratings/title/actual");
			
			
			// Log.d("NetFlix", "@URL: " + url.toString())
			HttpClient httpclient = new DefaultHttpClient();
			// Your URL
			HttpPost httppost = new HttpPost(url.toString());
			
			//pass 1-5 for rating or "not interested" String
			String rating = (modifiedDisc.getUserRating() == 0) ? NF_RATING_NO_INTEREST : String.valueOf(modifiedDisc.getUserRating().intValue()); 
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Your DATA
			nameValuePairs.add(new BasicNameValuePair("title_ref", modifiedDisc.getId()));
			nameValuePairs.add(new BasicNameValuePair("rating", rating));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			Netflix.getInstance().sign(httppost);

			HttpResponse response;
			response = httpclient.execute(httppost);
			nfResponse.setHttpCode(response.getStatusLine().getStatusCode());
			
			xml = response.getEntity().getContent();
	
			
			
			
			/* Log.d("NetFlix", "" +
			 response.getEntity().getContentType().toString()); BufferedReader
			 in = new BufferedReader(new InputStreamReader(xml)); String
			 linein = null; while ((linein = in.readLine()) != null) {
			 Log.d("NetFlix", "SetRating: " + linein); }*/
			 
			// Log.i("NetFlix", "Parsing XML Response")
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;

			sp = spf.newSAXParser();

			XMLReader xr = sp.getXMLReader();
			QueueHandler myHandler =  new QueueHandler();
			
			xr.setContentHandler(myHandler);
			xr.parse(new InputSource(xml));

			nfResponse.setNetflixCode(myHandler.getStatusCode());
			nfResponse.setNetflixSubCode(myHandler.getSubCode(0));
			nfResponse.setNetflixMessage(myHandler.getMessage());
			
			if(nfResponse.getHttpCode() == 201 || nfResponse.getNetflixCode() == 422){
				setUserRating(modifiedDisc.getUserRating());
			
			}
			
			

		} catch (IOException e) {	
			
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nfResponse;
	}
	
	
	
	
	
	
	
	
	
	
}
