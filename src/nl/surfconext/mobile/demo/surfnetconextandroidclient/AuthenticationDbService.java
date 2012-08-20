package nl.surfconext.mobile.demo.surfnetconextandroidclient;

/**
 * A Helper class for storing/retrieving some data.
 * For now it will be stored in memory, but the refreshtoken will be stored in a secure datasource  [TODO]
 * 
 * @author jknoops @ iprofs.nl
 */
public class AuthenticationDbService {

	/**
	 * the local AuthenticationDbService.
	 */
	private static AuthenticationDbService _instance;
	
	
	private String refreshToken;
	private String accessToken;
	private String tokenType;
	
	/**
	 * Static getInstance() method for Singleton pattern.
	 * @return the AuthenticationDbService
	 */
	public static AuthenticationDbService getInstance() {
		if (_instance == null) {
			_instance = new AuthenticationDbService();
		}
		return _instance;
	}
	
	/**
	 * Private constructor
	 */
	private AuthenticationDbService() {
		
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
}
