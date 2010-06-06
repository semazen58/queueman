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

import edwardawebb.queueman.queues.DiscQueue;

/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class AddDiscQueueHandler extends QueueHandler {

	private boolean inETag = false;

	//
	private String eTag;

	private DiscQueue queue;



	public AddDiscQueueHandler(DiscQueue queue) {
		super(queue);
		super.itemElementName = "queue_item";
		this.queue=queue;

	}

	public void startDocument() {
		// Log.d("AddDiscQueueHandler","Reading results XML")
	}

	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		super.startElement(uri, name, qName, atts);
		if (name.trim().equals("etag")){
			inETag = true;
		}
	}

	public void endElement(String uri, String name, String qName)throws SAXException {
		super.endElement(uri, name, qName);
		if (name.trim().equals("etag")){
			inETag = false;			
		}else if(name.trim().equals("queue_item")){			
			if(super.statusCode == 201){
				
				queue.add(super.position,super.tempMovie);
			}
		}
	}

	public void characters(char ch[], int start, int length) {
		super.characters(ch, start, length);
		String chars = (new String(ch).substring(start, start + length));
		if (inETag) {
			eTag = chars;
			if(eTag != null && !eTag.equals("")) queue.seteTag(eTag);
		}

	}
	
}
