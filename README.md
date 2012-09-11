# SURFnet's Android client for use with OAuth 2.0 (rev 31)

The mobile app is able to connect to a secure webservice which is secured in a OAuth 2.0 way. 
The current specification for OAuth 2.0 can be found at http://tools.ietf.org/html/draft-ietf-oauth-v2-31

## Tools used for developing/building the app.

* Android SDK
* Eclipse
* Android ADT (plugin for Eclipse)

## Building
Building the android app requires a [git](http://git-scm.com/) client, the android sdk, Eclipse

    git clone git://github.com/OpenConextApps/android-oauth-app.git

Import the project in Eclipse.
(See Android ADT documentation for running Android apps from Eclipse)

## Components

### Properties

In the properties file demo.properties is configured the following properties.

# Summerschool demo
    authorize_url=https://frko.surfnetlabs.nl/php-oauth/authorize.php
    authorize_response_type=code
    authorize_grant_type=authorization_code
    authorize_client_id= oauth-mobile-app
    authorize_scope=grades
    authorize_redirect_uri=oauth-mobile-app://callback
    token_url=https://frko.surfnetlabs.nl/php-oauth/token.php
    token_grant_type=authorization_code
    webservice_url=https://frko.surfnetlabs.nl/php-summerschool/api.php/grades/@me


### Start Activity

In the screen StartActivity some of the properties are shown to the user.
After pressing the the login button the app will try to connect to the authentication server
to retrieve the requested redirect-uri and an authorization code.

### Scheme Capture Activity

This activity is registered with a scheme to capture (in the AndroidManifest.xml).
After the activity starts (to be continued...)



