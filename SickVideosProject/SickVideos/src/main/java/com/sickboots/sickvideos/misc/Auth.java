package com.sickboots.sickvideos.misc;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;
import java.util.List;


public class Auth {
  private static GoogleAccountCredential credential;

  // Register an API key here: https://code.google.com/apis/console
  public static String devKey() {
    return "AIzaSyD0gRStgO5O0hBRp4UeAxtsLFFw9bMinOI";
  }

  public static GoogleAccountCredential getCredentials(Context ctx) {
    if (credential == null) {
      List<String> scopes = Arrays.asList(YouTubeScopes.YOUTUBE);

      credential = GoogleAccountCredential.usingOAuth2(ctx.getApplicationContext(), scopes);
    }
    return credential;
  }

  public static void setCredentials(GoogleAccountCredential credential) {
    Auth.credential = credential;
  }
}
