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
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class DiscETagHandler extends DefaultHandler {

	private boolean inETag=false;
	private boolean inNumResults=false;
	//temp variables
	private String eTag;
	protected int numResults = 0;

	
	
		
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		if (name.equals("etag")){
			inETag = true;			
		}else if (name.equals("number_of_results")) {
			inNumResults = true;
		}
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String name, String qName)throws SAXException {
		if (name.equals("etag")){
			inETag = false;			
		}else if (name.equals("number_of_results")) {
			inNumResults = false;
		}
	}
	
	//we only get an etag from  users disc queue
	@Override	
	public void characters(char ch[], int start, int length) throws SAXException {
		String chars = (new String(ch).substring(start, start + length));
		if(inETag){
			eTag=chars;
			NetFlix.discQueue.setETag(eTag);
			//throw new SAXException("ETag Updated");
		}else if (inNumResults) {
			numResults = Integer.valueOf(chars);
			NetFlix.discQueue.setTotalTitles(numResults);
		} 
	}

}
