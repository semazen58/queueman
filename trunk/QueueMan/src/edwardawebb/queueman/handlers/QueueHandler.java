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

	private boolean inResultsTotal = false;
	private boolean inResultsPerPage = false;
	private boolean inId = false;
	private boolean inRating = false;
	private boolean inTitle = false;
	private boolean inPosition = false;
	private boolean inBoxArt = false;
	private boolean inSynopsis = false;
	private boolean inFormats = false;
	protected boolean inAvailability = false;
	protected boolean inCategory = false;
	private boolean inYear = false;
	private boolean inItem = false;
	private boolean inStatus = false;
	private boolean inSubCode = false;

	// temp variables
	private String eTag;
	protected int position;
	private String stitle;
	private String ftitle;
	private String synopsis;
	private String id;
	private String boxArtUrl;
	private String year;
	private double rating;
	protected int statusCode = 0;
	private int subCode = 0;
	private boolean isAvailable = false;

	// element names (set by sub classes)
	protected String itemElementName;

	private Date availableFrom;

	private Date availableUntil;

	private String availability;
	private String discAvailabilityCategoryScheme = "http://api.netflix.com/categories/queue_availability";

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
		} else if (element.equals("delivery_formats")) {
			inFormats = true;
		} else if (element.equals("position")) {
			inPosition = true;
		} else if (element.equals("synopsis")) {
			inSynopsis = true;
		} else if (element.equals("id")) {
			inId = true;
		} else if (element.equals("release_year")) {
			inYear = true;
		} else if (element.equals("average_rating")) {
			inRating = true;
		} else if (element.equals("title")) {
			inTitle = true;
			stitle = atts.getValue("short");
			ftitle = atts.getValue("regular");
		} else if (element.equals("box_art")) {
			inBoxArt = true;
			boxArtUrl = atts.getValue("small");
		} else if (element.equals(itemElementName)) {
			inItem = true;
		} else if (element.equals("number_of_results")) {
			inResultsTotal = true;
		} else if (element.equals("results_per_page")) {
			inResultsPerPage = true;
		} else if (name.equals("status_code")) {
			inStatus = true;
		} else if (name.equals("sub_code")) {
			inSubCode = true;
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
		} else if (element.equals("delivery_formats")) {
			inFormats = false;
		} else if (element.equals("position")) {
			inPosition = false;
		} else if (element.equals("synopsis")) {
			inSynopsis = false;
		} else if (element.equals("id")) {
			inId = false;
		} else if (element.equals("release_year")) {
			inYear = false;
		} else if (element.equals("average_rating")) {
			inRating = false;
		} else if (element.equals("title")) {
			inTitle = false;
		} else if (element.equals("box_art")) {
			inBoxArt = false;
		} else if (element.equals(itemElementName)) {
			inItem = false;
			tempMovie = new Disc(id, stitle, ftitle, boxArtUrl, rating,
					synopsis, year, isAvailable);
			tempMovie.setAvailibilityText(availability);
		} else if (element.equals("number_of_results")) {
			inResultsTotal = false;
		} else if (element.equals("results_per_page")) {
			inResultsPerPage = false;
		} else if (name.equals("status_code")) {
			inStatus = false;
		} else if (name.equals("sub_code")) {
			inSubCode = false;
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
		} else if (inRating) {
			rating = Double.valueOf(chars);
		} else if (inSynopsis) {
			synopsis = (chars);
		} else if (inYear) {
			year = chars;
		} else if (inStatus) {
			statusCode = Integer.valueOf(chars);
		} else if (inSubCode) {
			subCode = Integer.valueOf(chars);

		}
		// Log.d("QueueHandler","<<<characters:" );

	}

	/**
	 * Get netflix api subcode
	 * 
	 * @return statusCode if subCode not set
	 */
	public int getSubCode() {
		if (this.subCode != 0) {
			return this.subCode;
		} else {
			return this.statusCode;
		}
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public void setDiscAvailabilityCategoryScheme(
			String discAvailabilityCategoryScheme) {
		this.discAvailabilityCategoryScheme = discAvailabilityCategoryScheme;
	}

}
