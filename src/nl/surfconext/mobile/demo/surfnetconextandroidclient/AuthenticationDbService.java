package nl.surfconext.mobile.demo.surfnetconextandroidclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.util.Log;

/**
 * A Helper class for storing/retrieving some data. The tokens are stored in a
 * Preferences file. The properties are loaded from the properties file.
 * 
 * @author jknoops @ iprofs.nl
 */
public class AuthenticationDbService {

	/**
	 * the local AuthenticationDbService.
	 */
	private static AuthenticationDbService _instance;
	private static Context _context;

	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String TOKEN_TYPE = "token_type";

	public static final String RESPONSE_TYPE_TOKEN = "token";
	public static final String RESPONSE_TYPE_CODE = "code";
	
	/**
	 * The properties from demo.properties are loaded inside this Properties.
	 */
	private Properties demoProperties;

	/**
	 * The values for the tokens are stored inside the preferences file.
	 */
	private SharedPreferences mPrefs;

	/**
	 * Static getInstance() method for Singleton pattern.
	 * 
	 * @return the AuthenticationDbService
	 */
	public static AuthenticationDbService getInstance(Context context) {
		if (_instance == null) {
			_context = context;
			_instance = new AuthenticationDbService();
		}
		return _instance;
	}

	/**
	 * Private constructor
	 */
	private AuthenticationDbService() {
		mPrefs = _context.getSharedPreferences("surfnet.demo",
				Context.MODE_PRIVATE);
		loadProperties();
	}

	/**
	 * Loading properties from the file system.
	 */
	private void loadProperties() {

		Resources resources = _context.getResources();

		// Read from the /res/raw directory
		try {
			InputStream rawResource = resources.openRawResource(R.raw.demo);
			demoProperties = new Properties();
			demoProperties.load(rawResource);
			Log.d("demo.surfconext", "The properties are now loaded");
			Log.v("demo.surfconext", "properties: " + demoProperties);

		} catch (NotFoundException e) {
			Log.e("demo.surfconext.error", "Did not find raw resource: " + e);
		} catch (IOException e) {
			Log.e("demo.surfconext.error", "Failed to open demo property file");
		}

	}

	public String getRefreshToken() {
		return mPrefs.getString(REFRESH_TOKEN, null);
	}

	public void setRefreshToken(final String token) {

		Editor editor = mPrefs.edit();
		editor.putString(REFRESH_TOKEN, token);
		editor.commit();
	}

	public String getAccessToken() {
		return mPrefs.getString(ACCESS_TOKEN, null);
	}

	public void setAccessToken(final String token) {

		Editor editor = mPrefs.edit();
		editor.putString(ACCESS_TOKEN, token);
		editor.commit();
	}

	public String getTokenType() {
		return mPrefs.getString(TOKEN_TYPE, null);
	}

	public void setTokenType(final String type) {

		Editor editor = mPrefs.edit();
		editor.putString(TOKEN_TYPE, type);
		editor.commit();
	}

	public String getAuthorize_url() {
		return demoProperties.getProperty("authorize_url");
	}

	public String getAuthorize_response_type() {
		return demoProperties.getProperty("authorize_response_type");
	}

	public String getAuthorize_grant_type() {
		return demoProperties.getProperty("authorize_grant_type");
	}

	public String getAuthorize_client_id() {
		return demoProperties.getProperty("authorize_client_id");
	}

	public String getAuthorize_scope() {
		return demoProperties.getProperty("authorize_scope");
	}

	public String getAuthorize_redirect_uri() {
		return demoProperties.getProperty("authorize_redirect_uri");
	}

	public String getToken_url() {
		return demoProperties.getProperty("token_url");
	}

	public String getToken_grant_type() {
		return demoProperties.getProperty("token_grant_type");
	}

	public String getWebservice_url() {
		return demoProperties.getProperty("webservice_url");
	}

}