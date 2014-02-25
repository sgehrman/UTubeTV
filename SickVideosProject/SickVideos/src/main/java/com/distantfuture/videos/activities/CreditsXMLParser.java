package com.distantfuture.videos.activities;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Looper;

import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreditsXMLParser {
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
