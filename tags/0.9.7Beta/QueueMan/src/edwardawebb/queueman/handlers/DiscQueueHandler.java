package edwardawebb.queueman.handlers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class DiscQueueHandler extends QueueHandler {

	private boolean inQueue=false;
	private boolean inETag=false;
	//temp variables
	private String eTag;

	
	
	
	public DiscQueueHandler() {
		super.itemElementName="queue_item";
	}

	
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		super.startElement(uri, name, qName, atts);
		if (name.trim().equals("etag")){
			inETag = true;			
		}else if(name.trim().equals("queue")){
			inQueue = true;
		}
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String name, String qName)throws SAXException {
		super.endElement(uri, name, qName);
		if (name.trim().equals("etag")){
			inETag = false;			
		}else if(name.trim().equals("queue_item")){			
			NetFlix.discQueue.add(super.tempMovie);
		}
	}
	
	//we only get an etag from  users disc queue
	@Override	
	public void characters(char ch[], int start, int length) {
		super.characters(ch, start, length);
		String chars = (new String(ch).substring(start, start + length));
		if(inETag){
			eTag=chars;
			NetFlix.discQueue.setETag(eTag);
		}
	}

	

}
