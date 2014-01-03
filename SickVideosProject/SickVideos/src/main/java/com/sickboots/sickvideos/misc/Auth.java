package com.sickboots.sickvideos.misc;

import android.accounts.Account;
import android.accounts.AccountManager;
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
  private static String accountName;

  // Register an API key here: https://code.google.com/apis/console
  public static String devKey() {
    return "AIzaSyD0gRStgO5O0hBRp4UeAxtsLFFw9bMinOI";
  }

  public static GoogleAccountCredential getCredentials(Context ctx) {
    if (credential == null) {
      List<String> scopes = Arrays.asList(YouTubeScopes.YOUTUBE);

      credential = GoogleAccountCredential.usingOAuth2(ctx.getApplicationContext(), scopes);

      // add account name if we have it
      String accountName = accountName(ctx);

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

  public static String accountName(Context ctx) {
    if (accountName == null) {
      accountName = AppUtils.instance(ctx).getAccountName();

      AccountManager am = AccountManager.get(ctx);
      Account[] accounts = am.getAccountsByType("com.google");

      // verify that the user name still exists before trying to use it
      if (accountName != null) {
        boolean valid = false;

        for (Account account : accounts) {
          if (accountName.equals(account.name)) {
            valid = true;
            break;
          }
        }

        if (!valid)
          accountName = null;
      }

      if (accountName == null) {
        // just get first item in list, is this correct?  is there a default account name?
        if (accounts.length > 0) {
          accountName = accounts[0].name;

          // save it in the prefs
          AppUtils.instance(ctx).setAccountName(accountName);
        }
      }
    }

    return accountName;
  }

  public static void setCredentials(GoogleAccountCredential credential) {
    Auth.credential = credential;
  }
}
