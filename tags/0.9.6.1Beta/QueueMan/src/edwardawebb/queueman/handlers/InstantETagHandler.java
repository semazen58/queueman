package edwardawebb.queueman.handlers;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.Disc;
import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class InstantETagHandler extends DefaultHandler {

	private boolean inETag=false;
	//temp variables
	private String eTag;

	
	
		
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		if (name.trim().equals("etag")){
			inETag = true;			
		}
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String name, String qName)throws SAXException {
		if (name.trim().equals("etag")){
			inETag = false;			
		}
	}
	
	//we only get an etag from  users disc queue
	@Override	
	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		if(inETag){
			eTag=chars;
			NetFlix.instantQueue.setETag(eTag);
		}
	}

}
