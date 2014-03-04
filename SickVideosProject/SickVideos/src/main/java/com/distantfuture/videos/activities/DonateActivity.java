package com.distantfuture.videos.activities;

import android.app.Activity;
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
import com.distantfuture.videos.misc.PurchaseHelper;

import de.greenrobot.event.EventBus;


public class DonateActivity extends Activity {
  private PurchaseHelper mPurchaseHelper;
  private Spinner mSpinner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_donate);

    getActionBar().setDisplayHomeAsUpEnabled(true);
    setupSpinner();
    EventBus.getDefault().register(this);

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
    final String SKU_ONE_DOLLAR = "one_dollar";
    final String SKU_TWO_DOLLAR = "two_dollars";
    final String SKU_THREE_DOLLAR = "three_dollars";
    final String SKU_SEVEN_DOLLAR = "seven_dollars";
    final String SKU_TEN_DOLLAR = "ten_dollars";

    switch (index) {
      case 0:
        return SKU_ONE_DOLLAR;
      case 1:
        return SKU_TWO_DOLLAR;
      case 2:
        return SKU_THREE_DOLLAR;
      case 3:
        return SKU_SEVEN_DOLLAR;
      case 4:
        return SKU_TEN_DOLLAR;
    }

    return SKU_ONE_DOLLAR;
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

  // eventbus event
  public void onEvent(Events.PurchaseEvent event) {
    TextView textView = (TextView) findViewById(R.id.status_message);
    textView.setVisibility((event.message != null) ? View.VISIBLE : View.GONE);
    textView.setText(event.message);

    if (event.alert != null) {
      AlertDialog.Builder bld = new AlertDialog.Builder(this);
      bld.setMessage(event.alert);
      bld.setNeutralButton("OK", null);
      bld.create().show();
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
