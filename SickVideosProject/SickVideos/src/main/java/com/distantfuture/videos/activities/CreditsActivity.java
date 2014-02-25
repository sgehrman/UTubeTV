package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Utils;

import java.util.List;

public class CreditsActivity extends Activity {
  ViewGroup mContainer;

  public static void show(Activity activity) {
    // add animation, see finish below for the back transition
    ActivityOptions opts = ActivityOptions.makeCustomAnimation(activity, R.anim.scale_in, R.anim.scale_out);

    Intent intent = new Intent();
    intent.setClass(activity, CreditsActivity.class);
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

    setContentView(R.layout.activity_credits);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    mContainer = (ViewGroup) findViewById(R.id.credits_container);

    CreditsXMLParser.parseXML(this, new CreditsXMLParser.CreditsXMLParserListener() {
      @Override
      public void parseXMLDone(List<CreditsXMLParser.CreditsPage> newPages) {
        boolean alternate=true;

        for (CreditsXMLParser.CreditsPage page : newPages) {
          GradientDrawable background=null;

          if (page.alternating_background) {
            int color;
            if (page.group) {
              color = 0x88000000;
            } else {
              alternate = !alternate;
              if (alternate) {
                color = 0x05000000;
              } else {
                color = 0x10000000;
              }
            }

            background = new GradientDrawable();
            background.setStroke(1, 0x10000000);
            background.setCornerRadius(12);
            background.setColor(color);
          }

          LinearLayout linearLayout = new LinearLayout(CreditsActivity.this);
          linearLayout.setOrientation(LinearLayout.VERTICAL);
          LinearLayout.LayoutParams duhParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

          int hMargin = (int) Utils.dpToPx(12f, CreditsActivity.this);
          int vMargin = (int) Utils.dpToPx(3f, CreditsActivity.this);
          duhParams.setMargins(hMargin, vMargin, hMargin, vMargin);
          linearLayout.setPadding(hMargin, vMargin, hMargin, vMargin);

          linearLayout.setLayoutParams(duhParams);
          linearLayout.setBackground(background);

          for (CreditsXMLParser.CreditsPageField field : page.fields)
            linearLayout.addView(createFieldView(field, page.group));

          mContainer.addView(linearLayout);
        }
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

  private View createFieldView(CreditsXMLParser.CreditsPageField field, boolean group) {
      int textSize = 16;

    if (field.isHeader())
      textSize = 20;

    if (field.size != null)
      textSize = Integer.parseInt(field.size);

    TextView textView = new TextView(this);
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    textView.setLayoutParams(params);
    textView.setTextSize(textSize);
    textView.setText(field.text);

    int color = 0xaa000000;
    if (field.link != null) {
      color = 0xff0000ff;

      final Uri uri = Uri.parse(field.link);
      textView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Utils.openWebPage(CreditsActivity.this, uri);
        }
      });
    }

    if (group)
      color = 0xaaffffff;

    textView.setTextColor(color);

    if (field.isHeader()) {
      textView.setTextSize(textSize);
      textView.setTypeface(Typeface.DEFAULT_BOLD);
    }
    textView.setGravity(Gravity.CENTER);

    LinearLayout linearLayout = new LinearLayout(this);
    LinearLayout.LayoutParams duhParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    int topMarginPx = (int) Utils.dpToPx(field.topMargin, this);
    duhParams.setMargins(0, topMarginPx, 0, 0);
    linearLayout.setLayoutParams(duhParams);
    linearLayout.setPadding(0,4,0,4);

    linearLayout.addView(textView);

    return linearLayout;
  }
}
