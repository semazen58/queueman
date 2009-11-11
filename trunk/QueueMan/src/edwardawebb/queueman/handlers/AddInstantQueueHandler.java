package edwardawebb.queueman.handlers;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class AddInstantQueueHandler extends QueueHandler {
	
	private boolean inETag = false;

	//
	private String eTag;

	private boolean inStatusCode = false;

	private Integer statusCode;
	
	public AddInstantQueueHandler() {
		super.itemElementName = "queue_item";

	}

	
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		super.startElement(uri, name, qName, atts);
		if (name.trim().equals("status_code")) {
			inStatusCode = true;
		}
	}

	public void endElement(String uri, String name, String qName)throws SAXException {
		super.endElement(uri, name, qName);
		if (name.trim().equals("etag")){
			inETag = false;			
		}else if(name.trim().equals("queue_item")){			
			if(statusCode == 201){
				NetFlix.instantQueue.add(super.position,super.tempMovie);
			}
		}else if (name.trim().equals("status_code")){
                inStatusCode = false;           
        }
	}

	public void characters(char ch[], int start, int length) {
		super.characters(ch, start, length);
		String chars = (new String(ch).substring(start, start + length));
		if (inETag) {
			eTag = chars;
			NetFlix.instantQueue.setETag(eTag);
		} else if (inStatusCode) {
			statusCode = Integer.valueOf(chars);
		}

	}


	

}
