package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import com.distantfuture.videos.R;

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

  public static void show(Activity activity, String contentID) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.putExtra("infoID", contentID);
    intent.setClass(activity, InfoActivity.class);
    activity.startActivity(intent, opts.toBundle());
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
    overridePendingTransition(R.anim.scale_out_rev, R.anim.scale_in_rev);
  }

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
      } else if (infoID.equals("cr")) {
        setTitle(R.string.pref_title_credits);
        webview.loadUrl("file:///android_asset/credits.html");
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
