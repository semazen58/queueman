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

import edwardawebb.queueman.classes.NetFlix;
import edwardawebb.queueman.classes.NetFlixQueue;
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
		if (name.equals("etag")){
			inETag = true;			
		}else{
			super.startElement(uri, name, qName, atts);	
			// any addituona ifs nested here
			
			
		}
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String value, String qName)throws SAXException {
		if (value.equals("etag")){
			inETag = false;			
		}else {
			//
			super.endElement(uri, value, qName);
			if(value.equals("queue_item")){	
				super.tempMovie.setQueueType(NetFlixQueue.QUEUE_TYPE_DISC);	
				NetFlix.discQueue.add(super.tempMovie);
			}
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

	@Override
	public void endDocument(){
		//these let us know for which range our etag is valid, needed for move to bottom top
		NetFlix.discQueue.setTotalTitles(super.numResults);
		NetFlix.discQueue.setStartIndex(super.startIndex);
		NetFlix.discQueue.setPerPage(resultsPerPage);
	}
	

}
