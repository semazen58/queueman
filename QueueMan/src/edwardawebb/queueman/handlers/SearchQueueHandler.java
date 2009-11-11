package edwardawebb.queueman.handlers;



import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class SearchQueueHandler extends QueueHandler {

	private ArrayList<String> mformats=new ArrayList<String>();

	public SearchQueueHandler() {
		super.itemElementName="catalog_title";
	}
	
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		super.startElement(uri, name, qName, atts);
		if(super.inAvailability && super.inCategory){
			//
			mformats.add(atts.getValue("label"));
		}
	}

	//we pnly want to update the local q when downlaoing disc q.
	@Override
	public void endElement(String uri, String name, String qName)throws SAXException {
		super.endElement(uri, name, qName);
		if(name.trim().equals("catalog_title")){	
			//add additional format info and save movie to search q
			super.tempMovie.setFormats(new ArrayList<String>(mformats));
			NetFlix.searchQueue.add(super.tempMovie);
			this.mformats.clear();
		}
	}
	


	

}
