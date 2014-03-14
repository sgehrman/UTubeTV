package com.distantfuture.videos.channellookup;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.distantfuture.videos.R;

/**
 * Created by sgehrman on 3/13/14.
 */
public class ChannelDetailsActivity extends Activity {

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, ChannelDetailsActivity.class);
   // activity.startActivity(intent, opts.toBundle());
    activity.startActivity(intent, null);
  }

  @Override
  public void finish() {
    super.finish();

    // animate out
//    overridePendingTransition(R.anim.scale_out_rev, R.anim.scale_in_rev);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_channel_details);
    getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
