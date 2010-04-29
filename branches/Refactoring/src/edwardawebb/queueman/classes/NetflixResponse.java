package edwardawebb.queueman.classes;


public class NetflixResponse{

	protected String httpMessage="";

	protected int netflixCode=0;

	protected int netflixSubCode=0;

	protected String netflixMessage="";
	
	
	
	protected int httpCode;

	/**
	 * @return the httpCode
	 */
	public int getHttpCode() {
		return httpCode;
	}

	/**
	 * @param httpCode the httpCode to set
	 */
	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	/**
	 * @return the httpMessage
	 */
	public String getHttpMessage() {
		return httpMessage;
	}

	/**
	 * @param httpMessage the httpMessage to set
	 */
	public void setHttpMessage(String httpMessage) {
		this.httpMessage = httpMessage;
	}

	/**
	 * @return the netflixCode
	 */
	public int getNetflixCode() {
		return netflixCode;
	}

	/**
	 * @param netflixCode the netflixCode to set
	 */
	public void setNetflixCode(int netflixCode) {
		this.netflixCode = netflixCode;
	}

	/**
	 * @return the netflixSubCode
	 */
	public int getNetflixSubCode() {
		return netflixSubCode;
	}

	/**
	 * @param netflixSubCode the netflixSubCode to set
	 */
	public void setNetflixSubCode(int netflixSubCode) {
		this.netflixSubCode = netflixSubCode;
	}

	/**
	 * @return the netflixMessage
	 */
	public String getNetflixMessage() {
		return netflixMessage;
	}

	/**
	 * @param netflixMessage the netflixMessage to set
	 */
	public void setNetflixMessage(String netflixMessage) {
		this.netflixMessage = netflixMessage;
	}

	/**
	 * @param httpCode
	 */
	public NetflixResponse(int httpCode) {
		super();
		this.httpCode = httpCode;
	}


}

