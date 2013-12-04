package com.sickboots.sickvideos.youtube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.common.AccountPicker;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;

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
