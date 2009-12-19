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
	private double avgRating;
	private double userRating;
	private Date availableFrom;
	private Date availableUntil;
	private boolean isAvailable;
	private String availibilityText;
	private boolean hasUserRating=false;

	private ArrayList<String> formats = new ArrayList<String>();

	public Disc(String id, String shortTitle, String fullTitle,
			String boxArtUrl, Double avgRating) {
		this.id = id;
		this.shortTitle = shortTitle;
		this.fullTitle = fullTitle;
		this.boxArtUrl = boxArtUrl;
		this.avgRating = avgRating;
	}

	public Disc(String id, String shortTitle, String fullTitle,
			String boxArtUrl, Double rating, String synopsis, String year,
			boolean isAvailable) {
		this.id = id;
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
	 * @param userRating the userRating to set
	 */
	public void setUserRating(double userRating) {
		this.userRating = userRating;
		hasUserRating=true;
	}

	/**
	 * @return the userRating
	 */
	public Double getUserRating() {
		return userRating;
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
			return shortTitle;
		}else if( availibilityText.equals("available now")){
			return shortTitle;
		}else{
			return "Saved - " + shortTitle;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Disc){
			Disc mdisc=(Disc)o;
			return (this.id.equals(mdisc.id));			
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}
}
