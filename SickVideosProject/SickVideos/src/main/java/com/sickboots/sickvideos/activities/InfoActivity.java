package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Utils;

/*
   <Preference android:title="@string/pref_title_privacy_policy">
        <intent android:action="rs.sickboots.sickvideos.InfoActivity">
            <extra
                android:name="infoID"
                android:value="pp"/>
        </intent>
    </Preference>

    <Preference android:title="@string/pref_title_tos">
        <intent android:action="rs.sickboots.sickvideos.InfoActivity">
            <extra
                android:name="infoID"
                android:value="tos"/>
        </intent>
    </Preference>
 */

public class InfoActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_info);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      WebView webview = (WebView) findViewById(R.id.web_view);

      String infoID = extras.getString("infoID");

      if (infoID.equals("pp")) {
        setTitle(R.string.pref_title_privacy_policy);
        webview.loadUrl("file:///android_asset/privacy_policy.html");
      } else if (infoID.equals("tos")) {
        setTitle(R.string.pref_title_tos);

        webview.loadUrl("file:///android_asset/tos.html");
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Respond to the action bar's Up/Home button
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
