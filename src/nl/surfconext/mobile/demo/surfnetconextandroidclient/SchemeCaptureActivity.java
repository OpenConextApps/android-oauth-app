package nl.surfconext.mobile.demo.surfnetconextandroidclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 
 * @author jknoops @ iprofs.nl
 */
public class SchemeCaptureActivity extends Activity {

	Map<String, String> fragments = new HashMap<String, String>();
	String output = "";
	private EditText et;
	private Properties demoProperties;
	private boolean isResponseTypeIsCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schemecapture);

		Log.d("demo.surfconext", "Loading properties");

		loadProperties();

		Button refreshButton = (Button) findViewById(R.id.button_refresh);
		refreshButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				doe();
			}

		});

		Uri data = getIntent().getData();
		Log.v("uri", data.toString());

		if (isResponseTypeIsCode) {
			retrieveQueryParamatersWithResponseTypeCode(data);
		} else {
			retrieveQueryParametersWithResponseTypeToken(data);
		}
		et = (EditText) findViewById(R.id.editText_output);
		et.setTextSize(8);
		doe();
	}

	private void retrieveQueryParametersWithResponseTypeToken(Uri data) {
		String fragment = data.getFragment();
		Log.v("uri", fragment);

		String[] pairs = fragment.split("&");
		Log.v("uri", pairs.toString());

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

				Log.v("part =", "[" + i + "," + j + "] = " + p);
				j++;
			}
			i++;
		}
	}

	private void retrieveQueryParamatersWithResponseTypeCode(Uri data) {
		String queryParameters = data.getQuery();

		Log.v("uri", queryParameters);

		String[] pairs = queryParameters.split("&");
		Log.v("uri", pairs.toString());

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

				Log.v("part =", "[" + i + "," + j + "] = " + p);
				j++;
			}
			i++;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}

	private void loadProperties() {

		Resources resources = this.getResources();

		// Read from the /res/raw directory
		try {
			InputStream rawResource = resources.openRawResource(R.raw.demo);
			demoProperties = new Properties();
			demoProperties.load(rawResource);
			Log.d("demo.surfconext", "The properties are now loaded");
			Log.v("demo.surfconext", "properties: " + demoProperties);

			String value = demoProperties.getProperty(
					"authorize_response_type", "code");
			if ("code".equalsIgnoreCase(value)) {
				this.isResponseTypeIsCode = true;
			} else {
				this.isResponseTypeIsCode = false;
			}

		} catch (NotFoundException e) {
			Log.e("demo.surfconext.error", "Did not find raw resource: " + e);
		} catch (IOException e) {
			Log.e("demo.surfconext.error", "Failed to open demo property file");
		}

	}

	private void retrieveRefreshAndAccessTokenWithResponseTypeCode() {

		if (fragments.containsKey("code")) {
			String code = fragments.get("code");
			Log.v("code", "code=" + code);

			String url = demoProperties.getProperty("token_url");

			URL tokenUrl;
			try {
				tokenUrl = new URL(url);

				HttpsURLConnection conn = (HttpsURLConnection) tokenUrl
						.openConnection();

				String param = "grant_type="
						+ URLEncoder.encode(demoProperties
								.getProperty("authorize_grant_type"), "UTF-8")
						+ "&code="
						+ URLEncoder.encode(code, "UTF-8")
						+ "&redirect_uri="
						+ URLEncoder
								.encode(demoProperties
										.getProperty("authorize_redirect_uri"),
										"UTF-8")
						+ "&client_id="
						+ URLEncoder.encode(demoProperties
								.getProperty("authorize_client_id"), "UTF-8");

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
				Log.v("response", response);

				JSONObject jo = new JSONObject(response);

				String json_access_token = jo.getString("access_token");
				int json_expires_in = jo.getInt("expires_in");
				String json_scope = jo.getString("scope");
				String json_refresh_token = jo.getString("refresh_token");
				String json_token_type = jo.getString("token_type");

				AuthenticationDbService dbService = AuthenticationDbService
						.getInstance();
				dbService.setAccessToken(json_access_token);
				dbService.setRefreshToken(json_refresh_token);
				dbService.setTokenType(json_token_type);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fragments.remove("code");
		}
	}

	private void retrieveAccessTokenWithResponseTypeCode() {
		// TODO Auto-generated method stub
		Log.d("retrieve", "retrieveAccessTokenWithResponseTypeCode");
		// AuthenticationDbService.getInstance().setRefreshToken("");

		String url = demoProperties.getProperty("token_url");

		/**
		 * grant_type=refresh_token refresh_token=
		 */
		URL tokenUrl;
		try {
			tokenUrl = new URL(url);

			HttpsURLConnection conn = (HttpsURLConnection) tokenUrl
					.openConnection();

			String param = "grant_type="
					+ URLEncoder.encode("refresh_token", "UTF-8")
					+ "&refresh_token="
					+ URLEncoder.encode(AuthenticationDbService.getInstance()
							.getRefreshToken(), "UTF-8");

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
			Log.v("response", response);

			JSONObject jo = new JSONObject(response);

			String json_access_token = jo.getString("access_token");
			int json_expires_in = jo.getInt("expires_in");
			String json_scope = jo.getString("scope");
			// String json_refresh_token = jo.getString("refresh_token");
			String json_token_type = jo.getString("token_type");

			AuthenticationDbService dbService = AuthenticationDbService
					.getInstance();
			dbService.setAccessToken(json_access_token);
			// dbService.setRefreshToken(json_refresh_token);
			dbService.setTokenType(json_token_type);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// FOR TESTING PURPOSE ONLY
		AuthenticationDbService.getInstance().setRefreshToken("");
	}

	private void retrieveDataWithAccessTokenWithResponseTypeCode() {
		HttpURLConnection conn = null;
		try {

			URL webserviceUrl = new URL(
					demoProperties.getProperty("webservice_url"));

			conn = (HttpURLConnection) webserviceUrl.openConnection();

			if (AuthenticationDbService.getInstance().getTokenType()
					.equalsIgnoreCase("bearer")) {
				conn.setRequestProperty("Authorization", "Bearer "
						+ AuthenticationDbService.getInstance()
								.getAccessToken());
			}

			Log.d("DEBUG", conn.getRequestProperties().toString());

			Log.v("conn", conn.toString());
			InputStreamReader isr = new InputStreamReader(conn.getInputStream());
			BufferedReader in = new BufferedReader(isr, 256);

			String response = "";
			StringBuilder sb_output = new StringBuilder();
			sb_output.append(et.getText());
			while ((response = in.readLine()) != null) {

				Log.v("response", "response=" + response);
				sb_output.append(response);

			}
			sb_output.append("\n");
			et.setText(sb_output.toString());

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				Log.d("error",
						"" + conn.getResponseCode() + " "
								+ conn.getResponseMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// TESTING PRUPOSE ONLY
		AuthenticationDbService.getInstance().setAccessToken("");
	}

	private void retrieveDataWithAccessTokenWithResponseTypeToken() {
		String access_token = fragments.get("access_token");
		Log.v("code", "access_token=" + access_token);

		String url = demoProperties.getProperty("webservice_url")
				+ "?access_token=" + access_token;

		Log.v("code", "url=" + url);
		BufferedReader in = null;
		try {
			URL jsonURL = new URL(url);
			Log.v("url", jsonURL.toString());
			HttpURLConnection tc = (HttpURLConnection) jsonURL.openConnection();
			Log.v("tc", tc.toString());
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
			Log.v("header", "" + tc.getResponseCode());
			sb.append("\nMessage=");
			sb.append(tc.getResponseMessage());

			for (String key : tc.getHeaderFields().keySet()) {
				Log.v("hf", "key=" + key + " and size="
						+ tc.getHeaderField(key).length());
			}
			;
			if ((output = in.readLine()) != null) {
				sb.append("\n");
				sb.append(output);
				Log.v("output", "output=" + output);
			} else {
				sb.append("\n");
				sb.append(output);
				Log.v("output", "output=" + output);
			}

			et.setText(sb.toString());
			tc.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			// throw new PopulateException();
		} finally {
			try {
				in.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private void doe() {
		disableConnectionReuseIfNecessary();

		Log.v("isResponseTypeIsCode", "isResponseTypeIsCode="
				+ isResponseTypeIsCode);
		if (isResponseTypeIsCode) {

			if (AuthenticationDbService.getInstance().getAccessToken() == null
					|| "".equalsIgnoreCase(AuthenticationDbService
							.getInstance().getAccessToken())) {
				if (AuthenticationDbService.getInstance().getRefreshToken() == null
						|| "".equalsIgnoreCase(AuthenticationDbService
								.getInstance().getRefreshToken())) {
					retrieveRefreshAndAccessTokenWithResponseTypeCode();
				} else {
					retrieveAccessTokenWithResponseTypeCode();
					// if
					// (AuthenticationDbService.getInstance().getAccessToken()
					// == null ||
					// "".equalsIgnoreCase(AuthenticationDbService.getInstance().getAccessToken()))
					// {
					//
					// StringBuilder sb = new StringBuilder();
					// // basic authorize
					// sb.append(demoProperties.getProperty("authorize_url",
					// "https://api.surfconext.nl/v1/oauth2/authorize"));
					// // response type
					// sb.append("?");
					// sb.append("response_type=");
					// sb.append(demoProperties.getProperty("authorize_response_type",
					// "token"));
					// // client_id
					// sb.append("&");
					// sb.append("client_id=");
					// sb.append(demoProperties.getProperty("authorize_client_id",
					// "http://jknoops_iprofs.nl"));
					// // scope
					// sb.append("&");
					// sb.append("scope=");
					// sb.append(demoProperties.getProperty("authorize_scope",
					// "read"));
					// // redirect
					// sb.append("&");
					// sb.append("redirect_uri=");
					// sb.append(demoProperties.getProperty("authorize_redirect_uri",
					// "testscheme://v1.test.surfconext.nl"));
					//
					// String url = sb.toString();
					//
					// Log.i("url-start", url);
					// Intent i = new Intent(Intent.ACTION_VIEW,
					// Uri.parse(url));
					//
					// startActivity(i);
					// retrieveRefreshAndAccessTokenWithResponseTypeCode();
					// } else {
					// // Retrieving a new refresh and accesstoken failed.
					// }
				}
				if (AuthenticationDbService.getInstance().getAccessToken() == null
						|| "".equalsIgnoreCase(AuthenticationDbService
								.getInstance().getAccessToken())) {

					StringBuilder sb = new StringBuilder();
					// basic authorize
					sb.append(demoProperties.getProperty("authorize_url",
							"https://api.surfconext.nl/v1/oauth2/authorize"));
					// response type
					sb.append("?");
					sb.append("response_type=");
					sb.append(demoProperties.getProperty(
							"authorize_response_type", "token"));
					// client_id
					sb.append("&");
					sb.append("client_id=");
					sb.append(demoProperties.getProperty("authorize_client_id",
							"http://jknoops_iprofs.nl"));
					// scope
					sb.append("&");
					sb.append("scope=");
					sb.append(demoProperties.getProperty("authorize_scope",
							"read"));
					// redirect
					sb.append("&");
					sb.append("redirect_uri=");
					sb.append(demoProperties.getProperty(
							"authorize_redirect_uri",
							"testscheme://v1.test.surfconext.nl"));

					String url = sb.toString();

					Log.v("url-start", url);
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

					startActivity(i);
				}
			}
			retrieveDataWithAccessTokenWithResponseTypeCode();

		} else {

			retrieveDataWithAccessTokenWithResponseTypeToken();
		}
	}

	private static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		System.setProperty("http.keepAlive", "false");
	}

}
