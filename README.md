# DAuth SDK for Native Android Clients

This library is used for authenticating users and fetching authorization code / access token which will be used by android apps for auto login.


[![forthebadge](https://forthebadge.com/images/badges/built-for-android.svg)](https://delta.nitt.edu)


# Integrating Dauth SDK into your app
1. Add reference to the project's `build.gradle` file:
 ```gradle
  implementation "com.dauth.androidsdk:1.0.0"
 ```
2. Register at http://auth.delta.nitt.edu/ to get `client id` , `client secret`, `redirectURI`
3. Now add these credentials to your `client-creds.json` like this :
```json
{
  "client_id": "CLIENT_ID",
  "redirect_uri": "REDIRECT_URI",
  "client_secret": "CLIENT_SECRET"
}
```
# Sample Code

Checkout [the sample project](sampleApp)

# Contributing
 - Fork and Clone the repository and add delta/DAuth-Android-SDK as remote upstream.
 - Create new branch for features.
 - Strictly follow the commit conventions mentioned [here](http://karma-runner.github.io/latest/dev/git-commit-msg.html).
 - Pull latest changes from upstream before pushing your code or creating a new feature branch.
 - Send a PR to delta/DAuth-Android-SDK for review and merging

**NOTE:**
Never push directly to main repository (upstream). Only push to your forked repo (origin) and send a pull request to the main repository
