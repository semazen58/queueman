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
 * QueueMan
 * Sep 24, 2009
 *  http://edwardawebb.com/
 */
package edwardawebb.queueman.classes;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * @author Edward A. Webb - http://edwardawebb.com
 * 
 */
/**
 * @author eddie
 * 
 */
public class NetFlixQueue {

	private Netflix netflix;
	private int id;
	private String etag;
	private boolean isDownloaded = false; //have we actually retrieved from NF, or just added titles
	//etag is only valid in current range, for move top/bottom we need to know where working out of bounds
	private int startIndex=0;
	private int perPage=0; 
	
	private int totalTitles=0; // total returned results
	
	/**
	 * @return the totalTitles
	 */
	public int getTotalTitles() {
		return totalTitles;
	}

	/**
	 * @param totalTitles the totalTitles to set
	 */
	public void setTotalTitles(int totalTitles) {
		this.totalTitles = totalTitles;
	}

	public static final int QUEUE_TYPE_DISC = 0;
	public static final int QUEUE_TYPE_INSTANT = 1;
	public static final int QUEUE_TYPE_SEARCH = 2;
	public static final int QUEUE_TYPE_RECOMMEND = 3;
	public static final int QUEUE_TYPE_HOME = 4;
	
	public static final String[] queueTypeText = {"disc","instant","search","recommended","at_home"};

	// A linked list gives you random access (implements List) and also
	// implements Queue
	private List<Disc> discs = new LinkedList<Disc>();

	public NetFlixQueue(int id) { // , List<Disc> initialdiscs
		// this.netflix = netflix;
		this.id = id;
		// add any other properties that you want for a queue

		// discs.addAll(initialdiscs);

	}

	public boolean isEmpty() {
		// Log.d("NetFlixQueue","Size:"+discs.size())
		return (boolean) (discs.size() == 0);
	}

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
			if (newPosition > discs.size()) {
				newPosition = discs.size();
			}
			Disc movie = discs.remove(oldPosition - 1);
			discs.add(newPosition - 1, movie);
		} else {
			FlurryAgent.onError("outOfBounds",
					"reorder: provided indices are out of bound. old:"
							+ oldPosition + ", new:" + newPosition,
					"NetFlixQueue");
		}

	}

	public void delete(Disc movie) {
		// netflix.deleteFromQueue(movie);
		discs.remove(movie);

	}

	public void add(Disc movie) {		
		discs.add(movie);
	}

	/**
	 * Adds new disc to Queue Position specified. position is 1 based.
	 * 
	 * @param position
	 * @param movie
	 */
	public void add(int position, Disc movie) {
		if(discs.contains(movie)){
			discs.remove(movie);
		}
		if (position >= 1 && position <= this.discs.size()) {
			discs.add(position - 1, movie);
		} else if (position < 1 ) {
			FlurryAgent.onError("outOfBounds",
					"Add: The provided Position was too low", "NetFlixQueue");
		} else if ( position == 500){
			//;eddie was lazy, moze to the end.(for new move to bootom feature)
			discs.add(movie);
		}
	}

	
	public int indexOf(Disc movie) {
		return discs.indexOf(movie);
	}
	
	
	/**
	 * Returns Queue Position of title (1 based)
	 * 
	 * @param movie
	 * @return
	 */
	public int positionOf(Disc movie) {
		return discs.indexOf(movie) + 1;
	}

	public List<Disc> getDiscs() {
		return Collections.unmodifiableList(discs);
	}

	public void setETag(String eTag2) {
		// TODO Auto-generated method stub
		this.etag = eTag2;
	}

	public String getETag() {
		return this.etag;
	}

	/**
	 * @param isDownloaded the isDownloaded to set
	 */
	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}

	/**
	 * @return the isDownloaded
	 */
	public boolean isDownloaded() {
		return isDownloaded;
	}

	public void purge() {
		discs.clear();

	}
	/**
	 * @return the startIndex
	 */
	public int getStartIndex() {
		return startIndex;
	}

	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @return the perPage
	 */
	public int getPerPage() {
		return perPage;
	}

	/**
	 * @param perPage the perPage to set
	 */
	public void setPerPage(int perPage) {
		this.perPage = perPage;
	}

	public void filterInstantOnly(){
		List<Disc> purges = new LinkedList<Disc>();
		
		for (Iterator<Disc> iterator = discs.iterator(); iterator.hasNext();) {
			Disc disc = (Disc) iterator.next();
			if(!disc.isAvailableInstant()) {
				purges.add(disc);
			}
		}
		
		for (Iterator<Disc> iterator = purges.iterator(); iterator.hasNext();) {
			Disc disc = (Disc) iterator.next();
			if(discs.remove(disc)){
					Log.d("NetFlix", "Disc removed");
			}
		}
		
		
	}
}
