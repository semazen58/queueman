package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.SearchQueueHandler;

public class SearchQueue extends BrowsableQueue{
	protected String keywords="";

	
	
	public SearchQueue(Netflix netflix){
		super(netflix);
		this.expanders="&expand=synopsis,formats";
		
	}
	
	/** This will specify the serach term and add to expanders */
	public void applyKeyword(String keyword){
		// TODO add implementation
		 this.keywords=URLEncoder.encode(keyword);
		
		
	}

	@Override
	protected QueueHandler getQueueHandler() {
		// TODO Auto-generated method stub
		return new SearchQueueHandler(this);
	}

	@Override
	protected URL getQueueUrl(User user) throws MalformedURLException {
		// TODO Auto-generated method stub
		return new URL(netflix.sign("http://api.netflix.com/catalog/titles?term="
				+ keywords + expanders));
	}

	@Override
	protected String getPrettyName() {
		// TODO Auto-generated method stub
		return "Search Results";
	}

}

