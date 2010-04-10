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
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.User;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class UserHandler extends DefaultHandler {


	private boolean inCanWatchInstant=false;
	private boolean inUserId=false;
	private boolean inFirstName=false;
	private boolean inFormats=false;
	private String canWatchInstant="";
	private String userId="";
	private String firstName="";
	private ArrayList<String> preferredFormats;
	
	private User user;


	protected final static String CAN_INSTANT_WATCH="can_instant_watch";
	protected final static String PREFERRED_FORMATS="preferred_formats";
	protected final static String USER_ID="user_id";
	protected final static String FIRST_NAME="first_name";
	protected final static String SCHEME_PREFERRED="http://api.netflix.com/categories/title_formats";

protected final static String ELEMENT_LABEL_ATT="label";
	


	
	/**
 * 
 */
public UserHandler(User user) {
	//super();
	this.user=user;
	this.preferredFormats=new ArrayList<String>();
}

	public void startElement(String uri, String element, String qName,	Attributes atts) {
		//Log.d("QueueHandler",">>>startELement:" + element);
		if (element.equals("link")){
			//evaluate one if rather than 5 for unused elee,mt - performance
		}else if (element.equals(CAN_INSTANT_WATCH)){
			inCanWatchInstant = true;
		}else if (element.equals(USER_ID)){
			inUserId = true;
		}else if (element.equals(FIRST_NAME)){
			inFirstName = true;
		}else{ 
			
			if(inFormats==true && element.equals("category")){
				//grab format label
				preferredFormats.add("" + atts.getValue(ELEMENT_LABEL_ATT));
			}else if (element.equals(PREFERRED_FORMATS)){
				inFormats = true;
			}
		}
		
	}

	public boolean getCanWatchInstant() {
		return Boolean.parseBoolean(canWatchInstant);
	}

	public ArrayList<String> getPreferredFormats() {
		return preferredFormats;
	}

	public void endElement(String uri, String element, String qName)throws SAXException {
		if (element.equals(CAN_INSTANT_WATCH)){
			inCanWatchInstant = false;
		}else if (element.equals(PREFERRED_FORMATS)){
			inFormats = false;
		}else if (element.equals(USER_ID)){
			inUserId = false;
		}else if (element.equals(FIRST_NAME)){
			inFirstName = false;
		}
	}

	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));

		if (inCanWatchInstant){
			canWatchInstant = chars;
		}else if (inUserId){
			userId=chars;
		}else if (inFirstName){
			firstName=chars;
		}
	}

public void endDocument(){
	user.setCanWatchInstant(Boolean.parseBoolean(canWatchInstant));
	user.setUserId(userId);
	user.setFirstName(firstName);
	user.setPreferredFormats(preferredFormats);
}

}
