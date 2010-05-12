package edwardawebb.queueman.queues;

import java.net.MalformedURLException;
import java.net.URL;

import edwardawebb.queueman.classes.Netflix;
import edwardawebb.queueman.classes.User;
import edwardawebb.queueman.handlers.InstantQueueHandler;
import edwardawebb.queueman.handlers.QueueHandler;


public class InstantQueue extends MutableQueue{

	public InstantQueue(Netflix netflix) {
		super(netflix);
		// TODO Auto-generated constructor stub
	}



	@Override
	protected QueueHandler getQueueHandler() {
		// TODO Auto-generated method stub
		return new InstantQueueHandler(this);
	}



	@Override
	protected URL getQueueUrl(User user)
			throws MalformedURLException {	
		
		return new URL("http://api.netflix.com/users/" + user.getUserId()
				+ "/queues/instant" + expanders);
	}



	@Override
	protected String getPrettyName() {
		// TODO Auto-generated method stub
		return "Instant Watch";
	}
	
}

