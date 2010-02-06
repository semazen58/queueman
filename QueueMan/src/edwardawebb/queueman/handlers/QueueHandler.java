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
 package edwardawebb.queueman.handlers;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.Disc;

/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class QueueHandler extends DefaultHandler {

	protected Disc tempMovie;

	private boolean inId = false;
	private boolean inRating = false;
	private boolean inUserRating = false;
	private boolean inPosition = false;
	private boolean inSynopsis = false;
	protected boolean inAvailability = false;
	protected boolean inCategory = false;
	private boolean inYear = false;
	private boolean inStatus = false;
	private boolean inSubCode = false;
	private boolean inNumResults = false;
	private boolean inStartIndex = false;
	private boolean inResultsPerPage = false;
	private boolean inMessage = false; //in case or error

	// temp variables
	private String eTag;
	protected int position;
	private String stitle;
	private String ftitle;
	private String mpaaRating="";
	private String synopsis;
	private String id;
	private String uniqueID;
	private String boxArtUrl;
	private String year;
	private double rating;
	private double userRating;
	protected int statusCode = 0;
	private int subCode = 0;
	private boolean isAvailable = false;
	protected int numResults = 0;
	protected int startIndex=0;
	protected int resultsPerPage=0;
	private String message = "";

	// element names (set by sub classes)
	protected String itemElementName;

	private Date availableFrom;

	private Date availableUntil;

	private String availability;
	private String discAvailabilityCategoryScheme = "http://api.netflix.com/categories/queue_availability";
	private String discMpaaRatingScheme = "http://api.netflix.com/categories/mpaa_ratings";
	private String discTvRatingScheme = "http://api.netflix.com/categories/tv_ratings";
	
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		// Log.d("QueueHandler",">>>startELement:" + element);
		String element = name.trim();
		if (element.equals("category")) {
			inCategory = true;
			if (atts.getValue("scheme").equals(discAvailabilityCategoryScheme)) {
				availability = atts.getValue("term");
				if (availability.equals("saved")) {
					isAvailable = false;
				} else {
					isAvailable = true;
				}
			}else if (atts.getValue("scheme").equals(discMpaaRatingScheme)
					|| atts.getValue("scheme").equals(discTvRatingScheme)) {
				mpaaRating = atts.getValue("label");				
			}
		} else if (element.equals("availability")) {
			inAvailability = true;
			/*
			 * if(!isAvailable){ //if not available, find out when it was / is
			 * //availableFrom = new Date(atts.getValue("available_from"));
			 * //availableUntil = new Date(atts.getValue("available_until"));
			 * 
			 * }
			 */
		} else if(element.equals("link") && atts.getValue("title").equals("synopsis")){
			//very poor way, but only way i could find to compare discs ascross queus.
			String href=atts.getValue("href");
			uniqueID=(String) href.subSequence(0,href.lastIndexOf("/") )   ;
		} else if (element.equals("position")) {
			inPosition = true;
		} else if (element.equals("synopsis")) {
			inSynopsis = true;
		} else if (element.equals("id")) {
			inId = true;
		} else if (element.equals("release_year")) {
			inYear = true;
		} else if (element.equals("number_of_results")) {
			inNumResults = true;
		} else if (element.equals("average_rating")) {
			inRating = true;
		}else if (element.equals("user_rating")) {
			inUserRating = true;
		} else if (element.equals("title")) {
			stitle = atts.getValue("short");
			ftitle = atts.getValue("regular");
		} else if (element.equals("box_art")) {			
			boxArtUrl = atts.getValue("small");
		} else if (element.equals("start_index")) {
			inStartIndex = true;
		} else if (element.equals("results_per_page")) {
			inResultsPerPage = true;
		} else if (name.equals("status_code")) {
			inStatus = true;
		} else if (name.equals("sub_code")) {
			inSubCode = true;
		}else if (name.equals("message")) {
			inMessage = true;
		}
		// Log.d("QueueHandler","<<<startELement:" + element);
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {
		String element = name.trim();
		if (element.equals("category")) {
			inCategory = false;
		} else if (element.equals("availability")) {
			inAvailability = false;
		} else if (element.equals("position")) {
			inPosition = false;
		} else if (element.equals("synopsis")) {
			inSynopsis = false;
		} else if (element.equals("id")) {
			inId = false;
		} else if (element.equals("release_year")) {
			inYear = false;
		} else if (element.equals("number_of_results")) {
			inNumResults = false;
		} else if (element.equals("average_rating")) {
			inRating = false;
		} else if (element.equals("user_rating")) {
			inUserRating = false;
		} else if (element.equals(itemElementName)) {
			tempMovie = new Disc(id,uniqueID, stitle, ftitle, boxArtUrl, rating,
					synopsis, year, isAvailable);
			tempMovie.setAvailibilityText(availability);
			tempMovie.setMpaaRating(new String(mpaaRating));
			mpaaRating="";
		} else if (element.equals("start_index")) {
			inStartIndex = false;
		} else if (element.equals("results_per_page")) {
			inResultsPerPage = false;
		} else if (name.equals("status_code")) {
			inStatus = false;
		} else if (name.equals("sub_code")) {
			inSubCode = false;
		}else if (name.equals("message")) {
			inMessage = false;
		}
		// Log.d("QueueHandler","<<<endELement:" + element);

	}

	public void characters(char ch[], int start, int length) {
		// Log.d("QueueHandler",">>>characters:" );

		String chars = (new String(ch).substring(start, start + length));

		if (inPosition) {
			position = Integer.parseInt(chars);

		} else if (inId) {
			id = chars;
			// Log.d("QueueHandler","Id: " + id);
		}  else if (inRating) {
			rating = Double.valueOf(chars);
		}else if (inUserRating) {
			userRating = Double.valueOf(chars);
		} else if (inSynopsis) {
			synopsis = (chars);
		} else if (inYear) {
			year = chars;
		} else if (inNumResults) {
			numResults = Integer.valueOf(chars);
		} else if (inStartIndex) {
			startIndex = Integer.valueOf(chars);
		} else if (inResultsPerPage) {
			resultsPerPage = Integer.valueOf(chars);
		} else if (inStatus) {
			statusCode = Integer.valueOf(chars);
		} else if (inSubCode) {
			subCode = Integer.valueOf(chars);

		} else if (inMessage) {
			message= chars;
		}
		// Log.d("QueueHandler","<<<characters:" );

	}

	/**
	 * Get netflix api subcode, if set, otherwise keep current value
	 * 
	 * @return statusCode if subCode not set
	 */
	public int getSubCode(int currentCode) {
		if (this.subCode != 0) {
			return this.subCode;
		} else if(this.statusCode != 0) {
			return this.statusCode;
		}else{
			return currentCode;
		}
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public void setDiscAvailabilityCategoryScheme(
			String discAvailabilityCategoryScheme) {
		this.discAvailabilityCategoryScheme = discAvailabilityCategoryScheme;
	}

	/**
	 * @return the message returned by Netflix
	 */
	public String getMessage() {
		return message;
	}

}
