package edwardawebb.queueman.handlers;



import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edwardawebb.queueman.classes.NetFlix;
/*
 * I enjoy quiet evenings after being called by the factory, and long walks through XML
 */
public class MoveQueueHandler extends DefaultHandler {
	private String eTag;
	
	private boolean inETag=false;
	private boolean inStatus=false;
	
	private int oldPosition;
	private boolean inPosition=false;
	private int position;
	protected int statusCode=0;
	private int subCode=0;
	private boolean inSubCode;

	public MoveQueueHandler(int oldPosition) {
		this.oldPosition=oldPosition;
	}
	
	public void startElement(String uri, String name, String qName,	Attributes atts) {
		if (name.trim().equals("status_code")){
			inStatus=true;
		}else if (name.trim().equals("etag")){
			inETag=true;
		}else if (name.trim().equals("position")){
			inPosition=true;
		}else if (name.equals("status_code")){
        	inStatus=true;
        }else if (name.equals("sub_code")){
        	inSubCode=true;
        }
	}

	//we pnly want to update the local q when downlaoing disc q.
	public void endElement(String uri, String name, String qName)throws SAXException {
		if(name.trim().equals("queue_item") && statusCode == 201){	
			//add additional format info and save movie to search q
			NetFlix.discQueue.reorder(oldPosition, position);
		}else if (name.trim().equals("status_code")){
			inStatus=false;
		}else if (name.trim().equals("etag")){
			inETag=false;
		}else if (name.trim().equals("position")){
			inPosition=false;
		}else if (name.equals("status_code")){
        	inStatus=false;
        }else if (name.equals("sub_code")){
        	inSubCode=false;
        }
	}

	public void characters(char ch[], int start, int length) {
		String chars = (new String(ch).substring(start, start + length));
		if(inETag){
			eTag=chars;
			NetFlix.discQueue.setETag(eTag);
		}else if(inPosition){
			position=Integer.parseInt(chars);
		}else if(inStatus){
			statusCode=Integer.parseInt(chars);
		}else if(inSubCode){
			subCode=Integer.parseInt(chars);
		}
	}
	/**
	 * Get netflix api subcode
	 * @return statusCode if subCode not set
	 */
	public int getSubCode(){
		if(this.subCode!=0){
			return this.subCode;
		}else{
			return this.statusCode;
		}
	}

	public int getStatusCode(){
		return this.statusCode;
	}

	

}
