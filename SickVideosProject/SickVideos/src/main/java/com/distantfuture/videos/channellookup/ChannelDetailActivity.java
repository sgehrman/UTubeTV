package com.distantfuture.videos.channellookup;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.distantfuture.videos.R;
import com.distantfuture.videos.activities.CreditsActivity;

/**
 * Created by sgehrman on 3/13/14.
 */
public class ChannelDetailActivity extends Activity {

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, ChannelDetailActivity.class);
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

    setContentView(R.layout.activity_channel_detail);
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
