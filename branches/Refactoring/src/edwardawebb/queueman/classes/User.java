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
 package edwardawebb.queueman.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable{
	private String userId;
	private String firstName;
	private String accessToken;
	private String accessTokenSecret;
	private boolean canWatchInstant;
	private ArrayList<String> preferredFormats;
	
	/**
	 * Contrust new user with only tokens. we will use this user to get remaining info 
	 * @param userId
	 * @param accessToken
	 * @param accessTokenSecret
	 */
	public User(String userId, String accessToken, String accessTokenSecret){
			
		this.userId = userId;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void setPreferredFormats(ArrayList<String> preferredFormats) {
		this.preferredFormats = preferredFormats;
	}
	public String getFirstName() {
		return firstName;
	}
	public ArrayList<String> getPreferredFormats() {
		return preferredFormats;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}
	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	public boolean isCanWatchInstant() {
		return canWatchInstant;
	}
	public void setCanWatchInstant(boolean canWatchInstant) {
		this.canWatchInstant = canWatchInstant;
	}
}
