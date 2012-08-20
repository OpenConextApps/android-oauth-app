package nl.surfconext.mobile.demo.surfnetconextandroidclient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
 * The Home Activity to start the application with.
 * The current settings can be shown here.
 * With the start button the Authentication flow will be started.
 * The scheme of the redirect_url will be catch by The other Activity (SchemeCaptureActivity).
 * 
 * 
 * @author jknoops @ iprofs.nl
 */
public class StartActivity extends Activity {

    private Properties demoProperties;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("demo.surfconext", "Starting Demo Application");
        
        setContentView(R.layout.activity_start);
        
        Log.d("demo.surfconext", "Loading properties");
        
        loadProperties();
        
        Log.d("demo.surfconext", "Initializing the screen.");

        EditText editTextUrl =  (EditText) findViewById(R.id.editText_autorize_url);
        editTextUrl.setText(demoProperties.getProperty("authorize_url"));

        EditText editTextResponseType =  (EditText) findViewById(R.id.editText_response_type);
        editTextResponseType.setText(demoProperties.getProperty("authorize_response_type"));
        
        EditText editTextClientId =  (EditText) findViewById(R.id.editText_client_id);
        editTextClientId.setText(demoProperties.getProperty("authorize_client_id"));
        
        EditText editTextRedirectUri =  (EditText) findViewById(R.id.editText_redirect_uri);
        editTextRedirectUri.setText(demoProperties.getProperty("authorize_redirect_uri"));
        
		Button startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				StringBuilder sb = new StringBuilder();
				// basic authorize
				sb.append(demoProperties.getProperty("authorize_url", "https://api.surfconext.nl/v1/oauth2/authorize"));
				// response type
				sb.append("?");
				sb.append("response_type=");
				sb.append(demoProperties.getProperty("authorize_response_type", "token"));
				// client_id
				sb.append("&");
				sb.append("client_id=");
				sb.append(demoProperties.getProperty("authorize_client_id", "http://jknoops_iprofs.nl"));
				// scope
				sb.append("&");
				sb.append("scope=");
				sb.append(demoProperties.getProperty("authorize_scope", "read"));
				// redirect
				sb.append("&");
				sb.append("redirect_uri=");
				sb.append(demoProperties.getProperty("authorize_redirect_uri", "testscheme://v1.test.surfconext.nl"));
				
				String url = sb.toString();

				Log.d("url-start", url);
		        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

		        startActivity(i);
			}
		});
    }

	/**
	 * Loading properties from the file system.
	 */
    private void loadProperties() {
    	
    	Resources resources = this.getResources();
    	
    	// Read from the /res/raw directory
    	try {
    	    InputStream rawResource = resources.openRawResource(R.raw.demo);
    	    demoProperties = new Properties();
    	    demoProperties.load(rawResource);
    	    Log.d("demo.surfconext","The properties are now loaded");
    	    Log.v("demo.surfconext","properties: " + demoProperties);
    	    
    	    
    	} catch (NotFoundException e) {
    		Log.e("demo.surfconext.error","Did not find raw resource: "+e);
    	} catch (IOException e) {
    		Log.e("demo.surfconext.error","Failed to open demo property file");
    	}
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_start, menu);
        return true;
    }
}
