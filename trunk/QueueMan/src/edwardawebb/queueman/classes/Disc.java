/**
 * 
 */
package edwardawebb.queueman.classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

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
	private String shortTitle;
	private String fullTitle;
	private String boxArtUrl;
	private String synopsis;
	private String year;
	private double rating;
	private Date availableFrom;
	private Date availableUntil;
	private boolean isAvailable;
	private String availibilityText;

	private ArrayList<String> formats = new ArrayList<String>();

	public Disc(String id, String shortTitle, String fullTitle,
			String boxArtUrl, Double rating) {
		this.id = id;
		this.shortTitle = shortTitle;
		this.fullTitle = fullTitle;
		this.boxArtUrl = boxArtUrl;
		this.rating = rating;
	}

	public Disc(String id, String shortTitle, String fullTitle,
			String boxArtUrl, Double rating, String synopsis, String year,
			boolean isAvailable) {
		this.id = id;
		this.shortTitle = shortTitle;
		this.fullTitle = fullTitle;
		this.boxArtUrl = boxArtUrl;
		this.rating = rating;
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

	public String toString() {
		return shortTitle;
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

	public Double getRating() {
		return rating;
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

}
