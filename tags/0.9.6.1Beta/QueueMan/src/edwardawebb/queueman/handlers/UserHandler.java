package edwardawebb.queueman.handlers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.Disc;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class UserHandler extends DefaultHandler {

	protected Disc tempMovie;
	
	private boolean inCanWatchInstant=false;
	private String canWatchInstant="";

	
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		//Log.d("QueueHandler",">>>startELement:" + element);
		String element = name.trim();
		if (element.equals("can_instant_watch")){
			inCanWatchInstant = true;
		}
	}

	public void endElement(String uri, String name, String qName)throws SAXException {
		String element = name.trim();
		if (element.equals("can_instant_watch")){
			inCanWatchInstant = false;
		}
	}

	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		if (inCanWatchInstant){
			canWatchInstant = chars;
		}
	}
	
	public boolean canWatch(){
		return Boolean.parseBoolean(canWatchInstant);
	}

}
