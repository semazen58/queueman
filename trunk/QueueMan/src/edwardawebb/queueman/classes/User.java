package edwardawebb.queueman.classes;

public class User {
	private String userId;
	private String accessToken;
	private String accessTokenSecret;
	private boolean canWatchInstant;
	
	
	public User(String userId, String accessToken, String accessTokenSecret,
			boolean canWatchInstant) {
		this.userId = userId;
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
		this.canWatchInstant = canWatchInstant;
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
