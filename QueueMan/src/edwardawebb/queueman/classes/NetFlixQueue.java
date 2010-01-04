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
import java.util.LinkedList;
import java.util.List;

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

	private NetFlix netflix;
	private int id;
	private String etag;
	public static final int QUEUE_TYPE_DISC = 2;
	public static final int QUEUE_TYPE_INSTANT = 3;
	public static final int QUEUE_TYPE_SEARCH = 4;
	public static final int QUEUE_TYPE_RECOMMEND = 5;
	public static final int QUEUE_TYPE_HOME = 6;

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
		if (position >= 1) {
			discs.add(position - 1, movie);
		} else {
			FlurryAgent.onError("outOfBounds",
					"Add: The provided Position was too low", "NetFlixQueue");
		}
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

	public void purge() {
		discs.clear();

	}
	
}
