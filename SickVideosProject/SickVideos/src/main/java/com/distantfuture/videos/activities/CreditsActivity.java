package com.distantfuture.videos.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreditsActivity extends Activity {
  private ViewGroup mContainer;

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
        boolean alternate = true;
        int radius = (int) Utils.dpToPx(8f, CreditsActivity.this);

        for (CreditsXMLParser.CreditsPage page : newPages) {
          GradientDrawable background = null;

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
            background.setCornerRadius(radius);
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

    if (field.copyRight != null)
      textView.setText(copyRightString(field.copyRight, field.text, color));
    else {
      textView.setText(field.text);
      textView.setTextColor(color);
    }

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
    linearLayout.setPadding(0, 4, 0, 4);

    linearLayout.addView(textView);

    return linearLayout;
  }

  private SpannableString copyRightString(String copyRight, String info, int infoColor) {
    //    final StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);
    final ForegroundColorSpan mColorSpan = new ForegroundColorSpan(infoColor);

    SpannableString result = new SpannableString(copyRight + info);
    //  result.setSpan(mBoldSpan, 0, copyRight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    //    result.setSpan(mBoldSpan, copyRight.length(), copyRight.length() + info.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    result.setSpan(mColorSpan, copyRight.length(), copyRight.length() + info.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

    return result;
  }

  // ===================================================================================
  // xml parser

  private static class CreditsXMLParser {
    private final Context mContext;
    private CreditsXMLParserListener mCallback;

    public interface CreditsXMLParserListener {
      public void parseXMLDone(List<CreditsPage> pages);
    }

    private CreditsXMLParser(final Context context) {
      super();
      mContext = context.getApplicationContext();
    }

    public static void parseXML(Context context, CreditsXMLParserListener callback) {
      new CreditsXMLParser(context).parseXML(callback);
    }

    private static CreditsPage parsePageTag(Context context, final XmlPullParser resourceParser) throws XmlPullParserException, IOException {
      List<CreditsPageField> fields = new ArrayList<CreditsPageField>();

      String group = resourceParser.getAttributeValue(null, "group");
      String no_background = resourceParser.getAttributeValue(null, "no_background");

      int eventType = resourceParser.getEventType();
      while (!(eventType == XmlPullParser.END_TAG && resourceParser.getName().equals("credit"))) {
        if (eventType == XmlPullParser.START_TAG) {
          String name = resourceParser.getName();

          String topMargin = resourceParser.getAttributeValue(null, "top_margin");
          String link = resourceParser.getAttributeValue(null, "link");
          String size = resourceParser.getAttributeValue(null, "size");
          String copyRight = resourceParser.getAttributeValue(null, "copy");

          if (name.equals("header")) {
            resourceParser.next();

            String text = resourceParser.getText();

            CreditsPageField field = CreditsPageField.newField(context, text, link, size, copyRight, topMargin, CreditsPageField.FieldType.HEADER);
            fields.add(field);
          } else if (name.equals("text")) {
            resourceParser.next();

            String text = resourceParser.getText();

            CreditsPageField field = CreditsPageField.newField(context, text, link, size, copyRight, topMargin, CreditsPageField.FieldType.TEXT);
            fields.add(field);
          } else if (name.equals("group")) {
            resourceParser.next();

            String text = resourceParser.getText();

            CreditsPageField field = CreditsPageField.newField(context, text, link, size, copyRight, topMargin, CreditsPageField.FieldType.GROUP);
            fields.add(field);
          }
        }
        eventType = resourceParser.next();
      }

      return CreditsPage.newPage(group, no_background, fields);
    }

    private List<CreditsPage> getHTMLChangelog(final int resourceId, final Resources resources) {
      List<CreditsPage> result = new ArrayList();

      final XmlResourceParser xml = resources.getXml(resourceId);
      try {
        int eventType = xml.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
          if ((eventType == XmlPullParser.START_TAG) && (xml.getName().equals("credit"))) {

            CreditsPage page = parsePageTag(mContext, xml);

            result.add(page);
          }
          eventType = xml.next();
        }
      } catch (XmlPullParserException e) {
        Debug.log(e.getMessage() + e);
      } catch (IOException e) {
        Debug.log(e.getMessage() + e);
      } finally {
        xml.close();
      }

      return result;
    }

    private void parseXML(final CreditsXMLParserListener callback) {
      final Resources resources = mContext.getResources();

      new Thread(new Runnable() {
        @Override
        public void run() {
          final List<CreditsPage> result = getHTMLChangelog(R.xml.credits, resources);

          new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
              callback.parseXMLDone(result);
            }
          });
        }

      }).start();
    }

    // ======================================================================
    // CreditsPage

    public static class CreditsPage {
      public String title;
      public List<CreditsPageField> fields;
      public boolean group;
      public boolean alternating_background;

      public static CreditsPage newPage(String group, String no_background, List<CreditsPageField> fields) {
        CreditsPage result = new CreditsPage();

        result.group = group != null;
        result.alternating_background = no_background == null;
        result.fields = fields;

        return result;
      }
    }

    // ======================================================================
    // CreditsPageField

    public static class CreditsPageField {
      public String text;
      public FieldType type;
      public int topMargin;
      public String link;
      public String size;
      public String copyRight;

      public enum FieldType {TEXT, HEADER, GROUP}

      public static CreditsPageField newField(Context context, String text, String link, String size, String copyRight, String topMargin, FieldType type) {
        CreditsPageField result = new CreditsPageField();

        result.text = Utils.condenseWhiteSpace(text);  // xml file can be reformatted by the IDE to add returns
        result.type = type;
        result.link = link;
        result.size = size;

        if (copyRight != null)
          result.copyRight = "Copyright " + copyRight + " ";

        if (topMargin != null)
          result.topMargin = Integer.parseInt(topMargin);

        return result;
      }

      public boolean isText() {
        return type == FieldType.TEXT;
      }

      public boolean isHeader() {
        return type == FieldType.HEADER;
      }

      public boolean isGroupHeader() {
        return type == FieldType.GROUP;
      }
    }
  }
}