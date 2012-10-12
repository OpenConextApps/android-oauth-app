# Android OAuth 2.0 Demo Application

## Introduction
This mobile application is able to connect to web service secured with OAuth 
2.0.

## Requirements

* Android SDK
* Eclipse
* Eclipse Android Plugin

More information can be found on Google's Android SDK page: 
https://developer.android.com/sdk/installing/index.html

## Building
Retrieve the code from GitHub (the `$` indicates the user prompt):

    $ git clone git://github.com/OpenConextApps/android-oauth-app.git

Import the project in Eclipse:

* File -> New -> Other
* Choose "Android Project from Existing Code"
* Browse... to the path where the Git repository is located
* Finish

This will load the project. Now make sure to select an environment that you
installed in using the SDK tools:

* Right click on the project in the 
* Click "Properties"
* Choose "Android"
* Select an installed "Project Build Target"

Now the source should be compiled correctly.

## OAuth Properties

The properties file located at `res/raw/demo.properties` contains the OAuth 
configuration parameters:

    authorize_url=https://frko.surfnetlabs.nl/workshop/php-oauth/authorize.php
    authorize_response_type=code
    authorize_grant_type=authorization_code
    authorize_client_id=oauth-mobile-app
    authorize_scope=grades
    authorize_redirect_uri=oauth-mobile-app://callback
    token_url=https://frko.surfnetlabs.nl/workshop/php-oauth/token.php
    token_grant_type=authorization_code
    webservice_url=https://frko.surfnetlabs.nl/workshop/php-oauth-grades-rs/api.php/grades/@me

You can modify them for instance to use your own environment.

## Activities

### Start Activity
In the screen `StartActivity` some of the properties are shown to the user.
After clicking the the login button the application will try to connect to the 
authorization server (AS) to retrieve the authorization code.

### Scheme Capture Activity
This activity is registered with a scheme to "capture", this is done in 
`AndroidManifest.xml`. It will take care of handling the response from the 
authorization server to the redirect URI containing the registered "scheme".

#### Response Type "code"
When the response type "code" is used the application receives the 
authorization code from response coming from the authorization server.
This authorization code can only be used once.

The application will use this authorization code to request an access token, 
and will get an access token, and optionally a refresh token. After receiving 
the tokens the access token will be used for retrieving the data from the 
web service.

If available the application will store the date/time when the access token will 
expire. This will prevent the application from using an access token that 
expired.

If available in the response, the application will store the refresh token as 
well. The refresh token will be used when the access token expired or when the 
access token is invalid for some other reason, maybe revoked. If the refresh 
token is not valid anymory, possibly also revoked, the browser will be opened 
again and possibly the user will be involved again in granting the application 
permission to request the data, like in the initial flow.

### Storage
The access token, and optional refresh token, and expiry times will be stored
in a local storage of the application. According to the Android specification 
this will be only be accessable by the application itself. If a token is not 
valid anymore it will be removed from the local storage, and possibly be 
replaced with a valid token.
