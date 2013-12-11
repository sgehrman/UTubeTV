package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // start drawer activity
    Intent intent = new Intent();
    intent.setClass(this, DrawerActivity.class);
    this.startActivity(intent);

    finish();
  }
}
