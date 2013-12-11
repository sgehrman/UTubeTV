package com.sickboots.sickvideos.misc;

import android.content.Context;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTubeScopes;

import java.io.IOException;
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

      // add account name if we have it
      String accountName = ApplicationHub.instance(ctx).getAccountName();
      if (accountName != null)
        credential.setSelectedAccountName(accountName);
    }

    return credential;
  }

  public HttpRequestInitializer nullCredential() {
    return new HttpRequestInitializer() {
      public void initialize(HttpRequest request) throws IOException {
      }
    };
  }

  public static void setCredentials(GoogleAccountCredential credential) {
    Auth.credential = credential;
  }
}
