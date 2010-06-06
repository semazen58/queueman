/**
 *     This file is part of QueueMan.
 *
 *        QueueMan is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    QueueMan is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with QueueMan.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 package edwardawebb.queueman.handlers;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.util.Log;
import edwardawebb.queueman.queues.MutableQueue;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class AddInstantQueueHandler extends QueueHandler {
	
	private boolean inETag = false;

	//
	private String eTag;

	private boolean inStatusCode = false;

	private Integer statusCode;

	private MutableQueue queue;
	
	public AddInstantQueueHandler(MutableQueue queue) {
		super(queue);
		super.itemElementName = "queue_item";
		this.queue=queue;

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
				queue.add(super.position,super.tempMovie);
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
			queue.seteTag(eTag);
		} else if (inStatusCode) {
			statusCode = Integer.valueOf(chars);
			Log.d("AddInstantHandler","statusCode:" + chars);
			
		}

	}


	

}
