package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;

import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.RecommendationsHandler;


public class RecommendedQueue extends BrowsableQueue{
	public RecommendedQueue(Netflix netflix) {
		super(netflix);
		// TODO Auto-generated constructor stub
	}

	protected String category;

	public void changeCategory(){
		// TODO add implementation
	}

	@Override
	protected QueueHandler getQueueHandler() {
		// TODO Auto-generated method stub
		return new RecommendationsHandler(this);
	}

	@Override
	protected URL getQueueUrl(User user) throws MalformedURLException {
		// TODO Auto-generated method stub
		return  new URL("http://api.netflix.com/users/" + user.getUserId()
				+ "/recommendations" + expanders + "&start_index=" + startIndex + "&max_results=" + maxTitles);
	}

	@Override
	protected String getPrettyName() {
		// TODO Auto-generated method stub
		return "Recommendations";
	}

}

