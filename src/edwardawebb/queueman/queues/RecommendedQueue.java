package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;

import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.QueueHandler;
import edwardawebb.queueman.handlers.RecommendationsHandler;


public class RecommendedQueue extends BrowsableQueue{
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
		return  new URL("http://api.Netflix.com/users/" + user.getUserId()
				+ "/recommendations" + expanders);
	}

}

