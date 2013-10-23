package com.sickboots.sickvideos;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Arrays;
import java.util.List;

public class GoogleAccount {
  public interface GoogleAccountDelegate {
    public Activity getActivity();
    public void credentialIsReady();
  }

  private final String ACCOUNT_KEY = "account-name";
  private GoogleAccountCredential credential;
  private List<String> scopes;
  private final int REQUEST_ACCOUNT_PICKER = 33008;
  private final String ACCOUNT_FRAGMENT_NAME = "AccountFragment";
  private GoogleAccountDelegate delegate;

  // helper to create a YouTube credential
  public static GoogleAccount newYouTube(GoogleAccountDelegate d) {
    GoogleAccount result = new GoogleAccount();

    result.scopes = Arrays.asList(YouTubeScopes.YOUTUBE);
    result.delegate = d;

    return result;
  }

  public GoogleAccountCredential credential(boolean askUser) {
    if (credential == null) {
      setupCredential();
    }

    // return null if no name set
    if (credential.getSelectedAccountName() != null) {
      return credential;
    }

    if (askUser) {
      chooseAccount(delegate.getActivity());
    }

    return null;
  }

  private void setupCredential() {
    Activity activity = delegate.getActivity();

    credential = GoogleAccountCredential.usingOAuth2(activity, scopes);

    // example code had this, no idea if needed
    credential.setBackOff(new ExponentialBackOff());

    loadAccount(activity);
  }

  private void loadAccount(Activity activity) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
    String accountName = sp.getString(ACCOUNT_KEY, null);

    if (accountName != null) {
      credential.setSelectedAccountName(accountName);
    }
  }

  private void saveAccount(Activity activity) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
    sp.edit().putString(ACCOUNT_KEY, credential.getSelectedAccountName()).commit();
  }

  private void chooseAccount(Activity activity) {

    // create temporary fragment so we can get the result
    FragmentManager fragmentManager = activity.getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    Fragment fragment = new AccountFragment();
    fragmentTransaction.add(fragment, ACCOUNT_FRAGMENT_NAME);
    fragmentTransaction.commit();
  }

  public void chooseAccountResult(String result) {
    credential.setSelectedAccountName(result);

    Activity a = delegate.getActivity();

    saveAccount(a);

    delegate.credentialIsReady();

    // tell the activity that the credential is ready to rock
    FragmentManager fragmentManager = a.getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.remove(a.getFragmentManager().findFragmentByTag(ACCOUNT_FRAGMENT_NAME));
    fragmentTransaction.commit();
  }

  // fragment just used to get onActivityResult
  class AccountFragment extends Fragment {
    @Override
    public void onAttach(Activity activity) {
      super.onAttach(activity);

      startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
        case REQUEST_ACCOUNT_PICKER:
          String accountName = null;

          if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
            accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
          }

          // must be called for OK and Cancel
          chooseAccountResult(accountName);

          break;
      }
    }

  }

}
