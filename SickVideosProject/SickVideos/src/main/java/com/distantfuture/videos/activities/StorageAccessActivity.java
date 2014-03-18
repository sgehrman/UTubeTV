package com.distantfuture.videos.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Window;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.JSONHelper;
import com.distantfuture.videos.misc.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class StorageAccessActivity extends Activity {
  private static final int REQUEST_SAVE_FILE = 1;
  private static final int REQUEST_LOAD_FILE = 2;
  private static final String LOAD_FLAG = "load_flag";

  public static void save(final Activity activity, final Uri uri, final String text, final String type) {
    Intent intent = saveIntent(activity, uri, text, type);

    if (intent != null)
      activity.startActivityForResult(intent, 233, null);
  }

  public static void load(final Activity activity, final String type) {
    Intent intent = loadIntent(activity, type);

    if (intent != null)
      activity.startActivityForResult(intent, 233, null);
  }

  private static Intent saveIntent(final Context context, final Uri uri, final String text, final String type) {
    Intent intent = null;

    if (Utils.isKitKatOrNewer()) {
      intent = new Intent()
          .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
          .setType(type)
          .setComponent(new ComponentName(context, StorageAccessActivity.class));

      if (uri != null) {
        intent.putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_SUBJECT, uri.getLastPathSegment());
      } else if (text != null)
        intent.putExtra(Intent.EXTRA_TEXT, text);

      intent.putExtra(LOAD_FLAG, false);
    }

    return intent;
  }

  private static Intent loadIntent(final Context context, final String type) {
    Intent intent = null;

    if (Utils.isKitKatOrNewer()) {
      intent = new Intent()
          .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
          .setType(type)
          .setComponent(new ComponentName(context, StorageAccessActivity.class));

      intent.putExtra(LOAD_FLAG, true);
    }

    return intent;
  }

  private static void copyStream(final InputStream inStream, final OutputStream outStream, final boolean closeOutput)
      throws IOException {
    // in case Android includes Apache commons IO in the future, this function should be replaced by IOUtils.copy
    final int bufferSize = 4096;
    final byte[] buffer = new byte[bufferSize];
    int len = 0;

    try {
      while ((len = inStream.read(buffer)) != -1) {
        outStream.write(buffer, 0, len);
      }
    } finally {
      if (outStream != null && closeOutput)
        outStream.close();
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    switch (requestCode) {
      case REQUEST_SAVE_FILE:
        if (resultCode != Activity.RESULT_OK || data == null) {
          finish();
          return;
        }
        runSaveThread(data.getData());
        break;
      case REQUEST_LOAD_FILE:
        if (resultCode != Activity.RESULT_OK || data == null) {
          finish();
          return;
        }
        runLoadThread(data.getData());
        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    setProgressBarIndeterminate(true);
    setProgressBarIndeterminateVisibility(true);

    setContentView(R.layout.dialog_progressbar);

    final Intent intent = getIntent();
    if (intent.hasExtra(LOAD_FLAG)) {
      boolean load = intent.getBooleanExtra(LOAD_FLAG, false);
      final Intent request;

      if (load) {
        request = new Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType(intent.getType())
            .addCategory(Intent.CATEGORY_OPENABLE);

        startActivityForResult(request, REQUEST_LOAD_FILE);
      } else {
        if (!(intent.hasExtra(Intent.EXTRA_STREAM) || intent.hasExtra(Intent.EXTRA_TEXT))) {
          setResult(RESULT_CANCELED);
          finish();
          return;
        }

        String filename = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
          final Uri src = intent.getParcelableExtra(Intent.EXTRA_STREAM);
          if (src.getLastPathSegment() != null)
            filename = src.getLastPathSegment();
        }

        request = new Intent(Intent.ACTION_CREATE_DOCUMENT)
            .setType(intent.getType())
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_TITLE, filename);
        startActivityForResult(request, REQUEST_SAVE_FILE);
      }

    }
  }

  private void runSaveThread(final Uri destinationUri) {
    new Thread() {
      @Override
      public void run() {
        final Intent intent = getIntent();
        if (destinationUri == null) {
          finish();
          return;
        }

        try {
          final ParcelFileDescriptor fdOut = getContentResolver().openFileDescriptor(destinationUri, "w");
          final FileOutputStream os = new FileOutputStream(fdOut.getFileDescriptor());

          if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            final Uri src = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            final ParcelFileDescriptor fdIn = getContentResolver().openFileDescriptor(src, "r");
            final FileInputStream is = new FileInputStream(fdIn.getFileDescriptor());
            copyStream(is, os, true);
          } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            os.write(intent.getStringExtra(Intent.EXTRA_TEXT).getBytes());
            os.close();
          }
          fdOut.close();
        } catch (final IOException e) {
          Debug.log("Could not save file!" + e.toString());
        }
        finish();
      }
    }.start();
  }

  private void runLoadThread(final Uri sourceUri) {
    new Thread() {
      @Override
      public void run() {
        final Intent intent = getIntent();
        if (sourceUri == null) {
          finish();
          return;
        }

        try {
          final ParcelFileDescriptor fdIn = getContentResolver().openFileDescriptor(sourceUri, "r");
          final FileInputStream in = new FileInputStream(fdIn.getFileDescriptor());


          BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
          StringBuilder responseStrBuilder = new StringBuilder();

          String inputStr;
          while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
          JSONObject jsonObj = null;

          try {
            jsonObj = new JSONObject(responseStrBuilder.toString());

            Map result = JSONHelper.toMap(jsonObj);

            Debug.log(result.toString());

          } catch (Throwable t) {
            Debug.log("exception " + t.toString());
          }


          in.close();
          fdIn.close();
        } catch (final IOException e) {
          Debug.log("Could not save file!" + e.toString());
        }
        finish();
      }
    }.start();
  }


}