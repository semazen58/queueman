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



import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edwardawebb.queueman.classes.NetFlix;
import edwardawebb.queueman.classes.NetFlixQueue;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class HomeQueueHandler extends QueueHandler {

	
	public HomeQueueHandler() {
		super.itemElementName="at_home_item";
	}
	
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		super.startElement(uri, name, qName, atts);
		
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String name, String qName)throws SAXException {
		super.endElement(uri, name, qName);
		if(name.trim().equals("at_home_item")){	
			//add additional format info and save movie to search q
			super.tempMovie.setQueueType(NetFlixQueue.QUEUE_TYPE_HOME);
			NetFlix.homeQueue.add(super.tempMovie);
			
		}
	}
	
	

}
