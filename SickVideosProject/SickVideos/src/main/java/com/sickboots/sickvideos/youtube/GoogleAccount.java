package com.sickboots.sickvideos.youtube;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;
import java.util.List;

public class GoogleAccount {
  private GoogleAccountCredential credential;

  // helper to create a YouTube credential
  // String accountName = ApplicationHub.preferences().getString(PreferenceCache.GOOGLE_ACCOUNT_PREF, null);
  public GoogleAccount(Context context, String accountName) {
    List<String> scopes = Arrays.asList(YouTubeScopes.YOUTUBE);

    credential = GoogleAccountCredential.usingOAuth2(context, scopes);
    credential.setSelectedAccountName(accountName);
    credential.setBackOff(new ExponentialBackOff());  // example code had this, no idea if needed
  }

  public GoogleAccountCredential credential() {
    return credential;
  }

}
