package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.Observable;
import java.util.Observer;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

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
        // start drawer activity
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, DrawerActivity.class);
        startActivity(intent);

        // we are done, finish us
        ApplicationHub.instance().deleteObserver(MainActivity.this);
        finish();
      }
    }
  }

}
