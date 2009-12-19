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
