package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // This activity is set to no show apps name on launch
    // better experience than flickering actionbar

    // start drawer activity
    Intent intent = new Intent();
    intent.setClass(this, DrawerActivity.class);
    this.startActivity(intent);

    finish();
  }
}
