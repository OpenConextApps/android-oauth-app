package nl.surfconext.mobile.demo.surfnetconextandroidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * The Scheme Capture Activity will catch the configured scheme of the
 * redirect_url. The logic of retrieving the refresh/access token will also be
 * done in the class. With a valid access token the configured webservices will be
 * requested to get the data.
 * 
 * @author jknoops @ iprofs.nl
 */
public class SchemeCaptureActivity extends Activity {

	Map<String, String> fragments = new HashMap<String, String>();
	private EditText et;
	private boolean isResponseTypeIsCode;

	private AuthenticationDbService service = AuthenticationDbService
			.getInstance(this);

	private RetrieveDataResponseTypeCodeTask retrieveDataResponseTypeCodeTask;
	private RetrieveRefreshAndAccessTokenTask retrieveRefreshAndAccessTokenTask;
	private RetrieveAccessTokenTask retrieveAccessTokenTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_schemecapture);

		isResponseTypeIsCode = AuthenticationDbService.RESPONSE_TYPE_CODE
				.equals(service.getAuthorize_response_type());

		Button refreshButton = (Button) findViewById(R.id.button_refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				refreshData();
			}

		});

		Uri data = getIntent().getData();
		Log.v("demo.SCActivity", "Uri = " + data.toString());

		if (isResponseTypeIsCode) {
			retrieveQueryParamatersWithResponseTypeCode(data);
		} else {
			retrieveQueryParametersWithResponseTypeToken(data);
		}
		et = (EditText) findViewById(R.id.editText_output);
		et.setTextSize(10);
		refreshData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}

	/**
	 * Retrieve the parameters from the response data. 
	 * This method should only be used when the response type is "token".
	 * When the response type is "token" the response is inside the fragment.
	 * The data from the fragment will be stored in the local fragments Dictionary.
	 * 
	 * @param data
	 *            - the data to split into key/values
	 */
	private void retrieveQueryParametersWithResponseTypeToken(Uri data) {
		String fragment = data.getFragment();
		Log.v("demo.SCActivity", "Fragement (Token) = " + fragment);

		String[] pairs = fragment.split("&");
		Log.v("demo.SCActivity", "Pairs (Token) = " + pairs.toString());

		int i = 0;
		String key = "";
		String value = "";

		StringBuilder sb = new StringBuilder();

		while (pairs.length > i) {
			int j = 0;
			String[] part = pairs[i].split("=");

			while (part.length > j) {
				String p = part[j];
				if (j == 0) {
					key = p;
					sb.append(key + " = ");
				} else if (j == 1) {
					value = p;
					fragments.put(key, value);
					sb.append(value + "\n");
				}

				Log.v("demo.SCActivity", "[" + i + "," + j + "] = " + p);
				j++;
			}
			i++;
		}
		storeTokenFromFragments();
	}

	/**
	 * Retrieve the parameters from the response data.
	 * This method should only be used when the response type is "code".
	 * When the response type is "code" the response is in the queryparameters.
	 * The data from the queryparameters will be stored in the local fragments Dictionary.
	 * 
	 * @param data
	 *            - the data to split into key/values
	 */
	private void retrieveQueryParamatersWithResponseTypeCode(Uri data) {
		String queryParameters = data.getQuery();

		Log.v("demo.SCActivity", "Queryparameters (Code) = " + queryParameters);

		String[] pairs = queryParameters.split("&");
		Log.v("demo.SCActivity", "Pairs (Code) = " + pairs.toString());

		int i = 0;
		String key = "";
		String value = "";

		StringBuilder sb = new StringBuilder();

		while (pairs.length > i) {
			int j = 0;
			String[] part = pairs[i].split("=");

			while (part.length > j) {
				String p = part[j];
				if (j == 0) {
					key = p;
					sb.append(key + " = ");
				} else if (j == 1) {
					value = p;
					fragments.put(key, value);
					sb.append(value + "\n");
				}

				Log.v("demo.SCActivity", "[" + i + "," + j + "] = " + p);
				j++;
			}
			i++;
		}
	}

	/**
	 * A task for retrieving the access and (optional) refresh token.
	 * The task will be created when needed.
	 * The task will be executed in a separate Thread.
	 * This method should only be used when the response type is "code".
	 * The authorization code is needed to retrieve the requested tokens.
	 * This code should be available in the local dictionary fragments.
	 */
	private void retrieveRefreshAndAccessTokenWithResponseTypeCode() {

		if (retrieveRefreshAndAccessTokenTask == null) {
			retrieveRefreshAndAccessTokenTask = new RetrieveRefreshAndAccessTokenTask();
		}
		Log.d("TASK-1", retrieveRefreshAndAccessTokenTask.toString());

		if (retrieveRefreshAndAccessTokenTask.getStatus() == Status.FINISHED) {
			retrieveRefreshAndAccessTokenTask = new RetrieveRefreshAndAccessTokenTask();
			Log.d("TASK-1-execute1", "RetrieveRefreshAndAccessTokenTask");
			retrieveRefreshAndAccessTokenTask.execute();
		} else if (retrieveRefreshAndAccessTokenTask.getStatus() == Status.RUNNING) {
			// log
			Log.d("TASK-1-wait", "RetrieveRefreshAndAccessTokenTask");
			this.logUI("Please wait...");

		} else {
			Log.d("TASK-1-execute2", "RetrieveRefreshAndAccessTokenTask");
			retrieveRefreshAndAccessTokenTask.execute();
		}
	}

	/**
	 * A task for retrieving the access token with a current refresh token.
	 * The task will be created when needed.
	 * The task will be executed in a separate Thread.
	 * This method should only be used when the response type is "code".
	 * The refresh token should be available in the local DBService
	 */
	private void retrieveAccessTokenWithResponseTypeCode() {

		Log.v("demo.surfconext", "retrieveAccessTokenWithResponseTypeCode");

		if (retrieveAccessTokenTask == null) {
			retrieveAccessTokenTask = new RetrieveAccessTokenTask();
		}
		Log.d("TASK-2", retrieveAccessTokenTask.toString());

		if (retrieveAccessTokenTask.getStatus() == Status.FINISHED) {
			retrieveAccessTokenTask = new RetrieveAccessTokenTask();
			Log.d("TASK-2-execute1", "RetrieveAccessTokenTask");
			retrieveAccessTokenTask.execute();
		} else if (retrieveAccessTokenTask.getStatus() == Status.RUNNING) {
			// log
			Log.d("TASK-2-wait", "RetrieveAccessTokenTask");
			this.logUI("Please wait...");

		} else {
			Log.d("TASK-2-execute2", "RetrieveAccessTokenTask");
			retrieveAccessTokenTask.execute();
		}
	}

	/**
	 * A task for retrieving the data with a current access token.
	 * The task will be created when needed.
	 * The task will be executed in a separate Thread.
	 * This method should only be used when the response type is "code".
	 * The access token should be available in the local DBService
	 */
	private void retrieveDataWithAccessTokenWithResponseTypeCode() {

		if (retrieveDataResponseTypeCodeTask == null) {
			retrieveDataResponseTypeCodeTask = new RetrieveDataResponseTypeCodeTask();
		}
		Log.d("TASK-3", retrieveDataResponseTypeCodeTask.toString());

		if (retrieveDataResponseTypeCodeTask.getStatus() == Status.FINISHED) {
			retrieveDataResponseTypeCodeTask = new RetrieveDataResponseTypeCodeTask();
			Log.d("TASK-3-execute1", "RetrieveDataResponseTypeCodeTask");
			retrieveDataResponseTypeCodeTask.execute();
		} else if (retrieveDataResponseTypeCodeTask.getStatus() == Status.RUNNING) {
			// log
			Log.d("TASK-3-wait", "RetrieveDataResponseTypeCodeTask");
			this.logUI("Please wait...");

		} else {
			Log.d("TASK-3-execute2", "RetrieveDataResponseTypeCodeTask");
			retrieveDataResponseTypeCodeTask.execute();
		}
	}

	private void logUI(String text) {

		Log.v("text-added", text);
		if (et.getText() == null) {
			et.setText("" + text);
		} else {
			et.setText("" + et.getText() + "\n" + text);
		}
	}

	/**
	 * A task for retrieving the data with a current access token.
	 * The task will be created when needed.
	 * The task will be executed in a separate Thread.
	 * This method should only be used when the response type is "token".
	 * The access token should be available in the local DBService
	 */
	private void retrieveDataWithAccessTokenWithResponseTypeToken() {
		String access_token = fragments.get("access_token");
		Log.v("demo.surfconext", "access_token=" + access_token);

		String url = service.getWebservice_url();

		Log.v("demo.surfconext", "url=" + url);
		BufferedReader in = null;
		try {
			URL jsonURL = new URL(url);
			Log.v("demo.surfconext", jsonURL.toString());
			HttpsURLConnection tc = (HttpsURLConnection) jsonURL.openConnection();
			tc.addRequestProperty("Authorization", "Bearer " + access_token);
			Log.v("demo.surfconext", tc.toString());
			InputStreamReader isr = new InputStreamReader(tc.getInputStream());
			in = new BufferedReader(isr, 256);

			StringBuilder sb = new StringBuilder();
			sb.append(et.getText());
			sb.append("\nLenght=");
			sb.append(tc.getContentLength());
			sb.append("\nType=");
			sb.append(tc.getContentType());
			sb.append("\nCode=");
			sb.append(tc.getResponseCode());
			Log.v("demo.surfconext", "" + tc.getResponseCode());
			sb.append("\nMessage=");
			sb.append(tc.getResponseMessage());

			for (String key : tc.getHeaderFields().keySet()) {
				Log.v("demo.surfconext", "key=" + key + " and size="
						+ tc.getHeaderField(key).length());
			}
			String output = "";
			if ((output = in.readLine()) != null) {
				sb.append("\n");
				sb.append(output);
				Log.v("demo.surfconext", "output=" + output);
			} else {
				sb.append("\n");
				sb.append(output);
				Log.v("demo.surfconext", "output=" + output);
			}

			et.setText(sb.toString());
			tc.disconnect();
		} catch (Exception e) {
			Log.e("demo.surfconext.error",
					"retrieveDataWithAccessTokenWithResponseTypeToken", e);
			
			doAuthentication();
		} finally {
			try {
				in.close();

			} catch (IOException e) {
				Log.e("demo.surfconext.error",
						"retrieveDataWithAccessTokenWithResponseTypeToken", e);
				doAuthentication();
			}
		}
	}

	/**
	 * The default flow for retrieving data.
	 * If needed, retrieve refresh and access token from the authorization code.
	 * If needed, retrieve new access token from the refresh token 
	 * If needed, retrieve new authorization code.
	 * retrieve the data.
	 */
	private void refreshData() {
		disableConnectionReuseIfNecessary();

		if (isResponseTypeIsCode) {

			Log.v("demo.surfconext", "refreshdata: responseType = code");
			String accessToken = service.getAccessToken();
			if (accessToken == null || "".equalsIgnoreCase(accessToken)) {

				if (service.getRefreshToken() == null
						|| "".equalsIgnoreCase(service.getRefreshToken())) {
					retrieveRefreshAndAccessTokenWithResponseTypeCode();
				} else {
					retrieveAccessTokenWithResponseTypeCode();
				}

			} else {
				retrieveDataWithAccessTokenWithResponseTypeCode();
			}

		} else {

			retrieveDataWithAccessTokenWithResponseTypeToken();
		}
	}

	/**
	 * For use with android 2.2 (FROYO) the property http.keepAlive needs to be
	 * set on false. After this the connection pooling won't be used. The
	 */
	private static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo

		if (Build.VERSION.SDK_INT == 8) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/**
	 * The properties from the tokenString will be stored in the local DBService.
	 * The access token and the token type are required.
	 */
	private void storeTokens(String tokenString) throws JSONException {

		JSONObject jo = new JSONObject(tokenString);

		if ((!jo.has("access_token")) && (!jo.has("token_type"))) {
			// Nothing to store
			return;
		}

		service.setAccessToken(jo.getString("access_token"));
		service.setTokenType(jo.getString("token_type"));

		if (jo.has("refresh_token")) {
			service.setRefreshToken(jo.getString("refresh_token"));
		}

		if (jo.has("expires_in")) {
			service.setExpiresIn(jo.getInt("expires_in"));
		}

		if (jo.has("scope")) {
			service.setScope(jo.getString("scope"));
		}
	}

	/**
	 * The properties from the local fragments will be stored in the DBService.
	 */
	private void storeTokenFromFragments() {
		
		if (fragments.containsKey("access_token")) {
			service.setAccessToken(fragments.get("access_token"));
		} else {
			service.setAccessToken("");
		}
		
		if (fragments.containsKey("token_type")) {
			service.setTokenType(fragments.get("toke_type"));
		} else {
			service.setTokenType("");
		}
		
	}
	
	/**
	 * When the user is required to authenticate again, the refresh and access tokens will
	 * be removed from the DBService.
	 * The right URL will be made from the authentication properties.
	 * A activity will be activated to execute the created URL.
	 * Because the of the configured scheme configuration, this application will
	 * capture the response from the OAuth server.
	 */
	public void doAuthentication() {

		service.setAccessToken("");
		service.setRefreshToken("");
		
		StringBuilder sb = new StringBuilder();
		// basic authorize
		sb.append(service.getAuthorize_url());
		// response type
		sb.append("?");
		sb.append("response_type=");
		sb.append(service.getAuthorize_response_type());
		// client_id
		sb.append("&");
		sb.append("client_id=");
		sb.append(service.getAuthorize_client_id());
		// scope
		sb.append("&");
		sb.append("scope=");
		sb.append(service.getAuthorize_scope());
		// redirect
		sb.append("&");
		sb.append("redirect_uri=");
		sb.append(service.getAuthorize_redirect_uri());

		String url = sb.toString();

		Log.v("demo.surfconext", "Starting (Scheme Class) with url = " + url);
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

		startActivity(i);
	}

	/**
	 * A asynctask for retrieving the data from the configured webservice with a access token.
	 * This task should only be used when the response type is "code".
	 * If needed the access token will be retrieved again.
	 * If the postExecute the data retrieved from the webservice will be put in a component on the screen.
	 * 
	 * @author jknoops @ iprofs.nl
	 */
	private class RetrieveDataResponseTypeCodeTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String result = "";

			HttpURLConnection conn = null;
			try {

				URL webserviceUrl = new URL(service.getWebservice_url());

				conn = (HttpURLConnection) webserviceUrl.openConnection();

				if (service.getTokenType().equalsIgnoreCase("bearer")) {
					conn.setRequestProperty("Authorization", "Bearer "
							+ service.getAccessToken());
				}

				Log.d("demo.surfconext", conn.getRequestProperties().toString());

				Log.v("demo.surfconext", conn.toString());
				InputStreamReader isr = new InputStreamReader(
						conn.getInputStream());
				BufferedReader in = new BufferedReader(isr, 256);

				String response = "";
				StringBuilder sb_output = new StringBuilder();

				while ((response = in.readLine()) != null) {

					Log.v("demo.surfconext", "response=" + response);
					sb_output.append(response);

				}
				sb_output.append("\n");
				result = sb_output.toString();

			} catch (MalformedURLException e) {
				Log.e("demo.surfconext.error",
						"retrieveDataWithAccessTokenWithResponseTypeCode", e);
			} catch (IOException e) {

				try {
					Log.d("demo.surfconext.error", "" + conn.getResponseCode()
							+ " " + conn.getResponseMessage());

					int responseCode = conn.getResponseCode();
					if (responseCode == 401) {
						// token invalid
						StringBuilder sb_output = new StringBuilder();
						sb_output.append("\n");
						sb_output
								.append("Oops the token is invalid, let me try again!\n");

						result = sb_output.toString();

						retrieveAccessTokenWithResponseTypeCode();
						
					} else {

						// something else
						StringBuilder sb_output = new StringBuilder();
						sb_output.append("\n");
						sb_output.append("Oops something happend!\n");
						sb_output.append("HTTP response code = " + responseCode
								+ "\n");
						sb_output.append("HTTP response msg  = "
								+ conn.getResponseMessage() + "\n");

						result = sb_output.toString();
					}

				} catch (IOException e1) {
					Log.e("demo.surfconext.error",
							"RetrieveDataResponseTypeCodeTask", e);
				}
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			Log.d("DEBUG-RetrieveDataResponseTypeCodeTask", "onPostExecute = "
					+ result);
			logUI(result);
		}
	}

	/**
	 * A asynctask for retrieving the access and (optional) refresh token from the configured OAuth server.
	 * This task should only be used when the response type is "code".
	 * The authorization code can only be used once. After using the authorization code, the code will be removed from
	 * the local DBService.
	 * If the postExecute the tokens will be stored and the task for retrieving the data will be executed.
	 * 
	 * @author jknoops @ iprofs.nl
	 */
	private class RetrieveRefreshAndAccessTokenTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String result = "";

			if (fragments.containsKey("code")) {
				String code = fragments.get("code");
				Log.v("demo.surfconext", "code=" + code);

				String url = service.getToken_url();
				URL tokenUrl;

				try {
					tokenUrl = new URL(url);

					HttpsURLConnection conn = (HttpsURLConnection) tokenUrl
							.openConnection();

					String param = "grant_type="
							+ URLEncoder.encode(
									service.getAuthorize_grant_type(), "UTF-8")
							+ "&code="
							+ URLEncoder.encode(code, "UTF-8")
							+ "&redirect_uri="
							+ URLEncoder.encode(
									service.getAuthorize_redirect_uri(),
									"UTF-8")
							+ "&client_id="
							+ URLEncoder.encode(
									service.getAuthorize_client_id(), "UTF-8");
					
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					conn.setFixedLengthStreamingMode(param.getBytes().length);
					conn.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");

					// send the POST out
					PrintWriter out = new PrintWriter(conn.getOutputStream());
					out.print(param);
					out.close();

					// build the string to store the response text from the
					// server
					String response = "";
					Log.d("DEBUG",""+conn.getResponseCode());
					Log.d("DEBUG",""+conn.getResponseMessage());

					// start listening to the stream
					Scanner inStream = new Scanner(conn.getInputStream());

					// process the stream and store it in StringBuilder
					while (inStream.hasNextLine()) {
						response += (inStream.nextLine());
					}
					Log.v("demo.surfconext", response);
					result = response;

				} catch (MalformedURLException e) {
					Log.e("demo.surfconext.error",
							"retrieveRefreshAndAccessTokenWithResponseTypeCode",
							e);
				} catch (IOException e) {
					Log.e("demo.surfconext.error",
							"retrieveRefreshAndAccessTokenWithResponseTypeCode",
							e);
				}

				fragments.remove("code");
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null && !"".equals(result)) {
				try {
					storeTokens(result);
					retrieveDataWithAccessTokenWithResponseTypeCode();
				} catch (JSONException e) {

					doAuthentication();
				}
			} else {

				doAuthentication();
			}
		}
	}

	/**
	 * A asynctask for retrieving the access token from the configured OAuth server with a refresh token.
	 * This task should only be used when the response type is "code".
	 * The refresh token can only be used multiple times.
	 * If the postExecute the tokens will be stored and the task for retrieving the data will be executed.
	 * 
	 * @author jknoops @ iprofs.nl
	 */
	private class RetrieveAccessTokenTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			String result = "";

			String url = service.getToken_url();
			URL tokenUrl;
			HttpsURLConnection conn = null;
			try {
				tokenUrl = new URL(url);

				conn = (HttpsURLConnection) tokenUrl
						.openConnection();

				String param = "grant_type="
						+ URLEncoder.encode("refresh_token", "UTF-8")
						+ "&refresh_token="
						+ URLEncoder.encode(service.getRefreshToken(), "UTF-8");

				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setFixedLengthStreamingMode(param.getBytes().length);
				conn.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");

				// send the POST out
				PrintWriter out = new PrintWriter(conn.getOutputStream());
				out.print(param);
				out.close();

				// build the string to store the response text from the server
				String response = "";

				// start listening to the stream
				Scanner inStream = new Scanner(conn.getInputStream());

				// process the stream and store it in StringBuilder
				while (inStream.hasNextLine()) {
					response += (inStream.nextLine());
				}
				Log.v("demo.surfconext", response);
				result = response;

			} catch (MalformedURLException e) {
				Log.e("demo.surfconext.error",
						"retrieveAccessTokenWithResponseTypeCode", e);
			} catch (IOException e) {
				
				try {
					Log.d("demo.surfconext.error", "" + conn.getResponseCode()
							+ " " + conn.getResponseMessage());

					int responseCode = conn.getResponseCode();
					if (responseCode == 400) {
						Log.v("demo.SCActivity.error", "Bad request, authenticate again. Refresh token is not valid anymore.");
						return "";

					} else {

						Log.v("demo.SCActivity.error", "Bad request, authenticate again. Refresh token is not valid anymore.");
						// something else
						StringBuilder sb_output = new StringBuilder();
						sb_output.append("Oops something happend!\n");
						sb_output.append("HTTP response code = " + responseCode
								+ "\n");
						sb_output.append("HTTP response msg  = "
								+ conn.getResponseMessage() + "\n");
						
						Log.v("demo.SCActivity.error", sb_output.toString());
						return "";
					}

				} catch (IOException e1) {
					Log.e("demo.surfconext.error",
							"RetrieveDataResponseTypeCodeTask", e);
				}

			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null && !"".equals(result)) {
				logUI("Retrieved new Token(s)");
				try {
					storeTokens(result);
					retrieveDataWithAccessTokenWithResponseTypeCode();
				} catch (JSONException e) {

					doAuthentication();
				}
				
			} else {

				doAuthentication();
			}
		}
	}
}
