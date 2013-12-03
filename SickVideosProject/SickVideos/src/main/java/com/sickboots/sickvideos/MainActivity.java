package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.sickboots.sickvideos.misc.ApplicationHub;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ApplicationHub.init(this);

    ApplicationHub.instance().addObserver(this);
  }

  public void update(Observable observable, Object data) {
    if (data instanceof String) {
      String input = (String) data;

      if (input.equals(ApplicationHub.APPLICATION_READY_NOTIFICATION)) {
        DrawerActivity.start(this);

        // we are done, finish us
        ApplicationHub.instance().deleteObserver(MainActivity.this);
        finish();
      }
    }
  }

}
