package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.distantfuture.castcompanionlibrary.lib.utils.CastUtils;
import com.distantfuture.videos.R;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.youtube.YouTubeAPI;

import java.util.List;

public class ChannelLookupActivity extends Activity {
  TextView mTextView;
  EditText mEditText;
  String mCurrentQuery="";
  ChannelLookupListFragment fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_channel_lookup);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    mTextView = (TextView) findViewById(R.id.result_text);
    mEditText = (EditText) findViewById(R.id.edit_text);
    fragment = (ChannelLookupListFragment) getFragmentManager().findFragmentById(R.id.channel_list_fragment);

    mEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        Debug.log(s.toString());

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            newQuery();
          }
        }, 200);

      }
    });

    mEditText.requestFocus();

    mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_ACTION_DONE) {
          // return key
          newQuery();
        }
        return true;
      }
    });
  }

  private void newQuery() {
    String newQuery = mEditText.getText().toString();

    if (!newQuery.equals(mCurrentQuery)) {
      mCurrentQuery = newQuery;

      fragment.query(mCurrentQuery);
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
