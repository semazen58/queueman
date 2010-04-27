package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;

import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.InstantQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;


public class InstantQueue extends MutableQueues{

	@Override
	protected QueueHandler getQueueHandler() {
		// TODO Auto-generated method stub
		return new InstantQueueHandler();
	}

	@Override
	protected URL getQueueUrl(User user) throws MalformedURLException {
		// TODO Auto-generated method stub
		return new URL("http://api.Netflix.com/users/" + user.getUserId()
				+ "/queues/instant" + expanders);;
	}
	
}

