package com.distantfuture.videos.donate;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Events;

import de.greenrobot.event.EventBus;


public class DonateActivity extends Activity {
  private PurchaseHelper mPurchaseHelper;
  private Spinner mSpinner;
  private DonateThanksHelper mThanksHelper;

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, DonateActivity.class);
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

    setContentView(R.layout.activity_donate);

    getActionBar().setDisplayHomeAsUpEnabled(true);
    setupSpinner();
    EventBus.getDefault().register(this);
    mThanksHelper = new DonateThanksHelper(DonateActivity.this);

    Button button = (Button) findViewById(R.id.donate_button);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
               String sku =  skuForIndex(mSpinner.getSelectedItemPosition());

                mPurchaseHelper.onDonateButtonClicked(null, DonateActivity.this, sku);
      }
    });

    mPurchaseHelper = new PurchaseHelper(this);
  }

  private String skuForIndex(int index) {
    switch (index) {
      case 0:
        return  "one_dollar";
      case 1:
        return  "two_dollars";
      case 2:
        return  "three_dollars";
      case 3:
        return "five_dollars";
      case 4:
        return "seven_dollars";
      case 5:
        return "ten_dollars";
    }

    return "one_dollar";
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

  // We're being destroyed. It's important to dispose of the helper here!
  @Override
  public void onDestroy() {
    EventBus.getDefault().unregister(this);

    // very important:
    if (mPurchaseHelper != null) {
      mPurchaseHelper.destroy();
      mPurchaseHelper = null;
    }

    super.onDestroy();
  }

  private void setMessage(CharSequence message) {
    TextView textView = (TextView) findViewById(R.id.status_message);
    textView.setVisibility((message != null) ? View.VISIBLE : View.GONE);
    textView.setText(message);
  }

  // eventbus event
  public void onEvent(Events.PurchaseEvent event) {
    setMessage(event.message);

    if (event.alert != null) {
      AlertDialog.Builder bld = new AlertDialog.Builder(this);
      bld.setMessage(event.alert);
      bld.setNeutralButton("OK", null);
      bld.create().show();
    }

    if (event.successfulDonation) {
      mThanksHelper.install(DonateActivity.this);
      setMessage(getText(R.string.donate_thanks));
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    if (mPurchaseHelper != null && mPurchaseHelper.handleActivityResult(requestCode, resultCode, data)) {
      // handled by helper
    } else {
    }
  }

  private void setupSpinner() {
    mSpinner = (Spinner) findViewById(R.id.spinner);
    // Create an ArrayAdapter using the string array and a default spinner layout
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.donations_array, android.R.layout.simple_spinner_item);

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    mSpinner.setAdapter(adapter);

    mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
      }

      public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
      }

    });
  }


}
