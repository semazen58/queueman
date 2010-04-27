package edwardawebb.queueman.queues;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;
import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.NetFlixQueue;
import edwardawebb.queueman.classes.Netflix;

public abstract class BrowsableQueue extends Queue{

	
	public BrowsableQueue(Netflix netflix) {
		// TODO Auto-generated constructor stub
		super(netflix);
		
		
	}
	
	/**
	 * trims recommended queue to instant titles
	 */

	public void filterInstantOnly(){
		List<Disc> purges = new LinkedList<Disc>();
		
		for (Iterator<Disc> iterator = titles.iterator(); iterator.hasNext();) {
			Disc disc = (Disc) iterator.next();
			if(!disc.isAvailableInstant()) {
				purges.add(disc);
			}
		}
		
		for (Iterator<Disc> iterator = purges.iterator(); iterator.hasNext();) {
			Disc disc = (Disc) iterator.next();
			if(titles.remove(disc)){
					Log.d("BrowsableQueue", "Disc removed");
			}
		}
		
		
	}
}

