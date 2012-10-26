package org.openconext.mobile.oauth;

import org.openconext.mobile.oauth.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * The Home Activity to start the application with. The current settings can be
 * shown here. With the start button the Authentication flow will be started.
 * The scheme of the redirect_url will be catch by The other Activity
 * (SchemeCaptureActivity).
 * 
 * @author jknoops @ iprofs.nl
 */
public class StartActivity extends Activity {

	private AuthenticationDbService service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("demo.SActivity", "Starting Demo Application");

		setContentView(R.layout.activity_start);

		Log.d("demo.SActivity", "Loading properties");

		service = AuthenticationDbService.getInstance(this);

		Log.d("demo.SActivity", "Initializing the screen.");

		EditText editTextUrl = (EditText) findViewById(R.id.editText_autorize_url);
		editTextUrl.setText(service.getAuthorize_url());

		EditText editTextResponseType = (EditText) findViewById(R.id.editText_response_type);
		editTextResponseType.setText(service.getAuthorize_response_type());

		EditText editTextClientId = (EditText) findViewById(R.id.editText_client_id);
		editTextClientId.setText(service.getAuthorize_client_id());

		EditText editTextRedirectUri = (EditText) findViewById(R.id.editText_redirect_uri);
		editTextRedirectUri.setText(service.getAuthorize_redirect_uri());

		Button startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

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

				Log.d("demo.SActivity", "Starting (Starting class) with url = "
						+ url);
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

				startActivity(i);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_start, menu);
		return true;
	}
}
