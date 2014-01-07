package com.sickboots.sickvideos.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

/**
 * Created by sgehrman on 1/6/14.
 */
public class ChannelLookupActivity extends Activity {
  TextView mTextView;
  EditText mEditText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_channel_lookup);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    mTextView = (TextView) findViewById(R.id.result_text);
    mEditText = (EditText) findViewById(R.id.edit_text);

    mEditText.requestFocus();

    mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
          // ask youtube to look up this name and output to log and screen
          askYouTubeForChannelInfo(mEditText.getText().toString());
        }
        return true;
      }
    });
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

  private void askYouTubeForChannelInfo(final String userName) {
    (new Thread(new Runnable() {
      public void run() {

        YouTubeAPI helper = new YouTubeAPI(ChannelLookupActivity.this, new YouTubeAPI.YouTubeAPIListener() {
          @Override
          public void handleAuthIntent(final Intent authIntent) {
            Debug.log("handleAuthIntent inside update Service.  not handled here");
          }
        });

        final String result = helper.channelIdFromUsername(userName);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
          @Override
          public void run() {
            Debug.log(result);
            mTextView.setText(result);
          }
        });
      }
    })).start();
  }

}
