package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;

import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.DiscQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;

public class HomeQueue extends BrowsableQueue{

	public HomeQueue(Netflix netflix) {
		super(netflix);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected QueueHandler getQueueHandler() {
		// TODO Auto-generated method stub
		return new DiscQueueHandler();
	}

	@Override
	protected URL getQueueUrl(User user) throws MalformedURLException {
		// TODO Auto-generated method stub
		return new URL("http://api.Netflix.com/users/" + user.getUserId()
				+ "/queues/disc/available" + expanders);
	}
}

