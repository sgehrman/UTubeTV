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
    CreditsPage result = null;
    String title, icon;
    List<CreditsPageField> fields = new ArrayList<CreditsPageField>();

    int eventType = resourceParser.getEventType();
    while (!(eventType == XmlPullParser.END_TAG && resourceParser.getName().equals("credit"))) {
      if (eventType == XmlPullParser.START_TAG) {

        String name = resourceParser.getName();

        String topMargin = resourceParser.getAttributeValue(null, "top_margin");
        String append = resourceParser.getAttributeValue(null, "append");

        if (name.equals("header")) {
          resourceParser.next();

          String text = resourceParser.getText();

          CreditsPageField field = CreditsPageField.newField(context, text, topMargin, append, CreditsPageField.FieldType.HEADER);
          fields.add(field);
        } else if (name.equals("text")) {
          resourceParser.next();

          String text = resourceParser.getText();

          CreditsPageField field = CreditsPageField.newField(context, text, topMargin, append, CreditsPageField.FieldType.TEXT);
          fields.add(field);
        }
      }
      eventType = resourceParser.next();
    }

    return CreditsPage.newPage(fields);
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

  public static class CreditsPage {
    public String title;
    public List<CreditsPageField> fields;

    public static CreditsPage newPage(List<CreditsPageField> fields) {
      CreditsPage result = new CreditsPage();

      result.fields = fields;

      return result;
    }
  }

  public static class CreditsPageField {
    public String text;

    public enum FieldType {TEXT, HEADER}

    public FieldType type;
    public int topMargin;

    public static CreditsPageField newField(Context context, String text, String topMargin, String append, FieldType type) {
      CreditsPageField result = new CreditsPageField();

      result.text = Utils.condenseWhiteSpace(text);  // xml file can be reformatted by the IDE to add returns

      if (append != null && append.equals("app_name"))
        result.text += " " + Utils.getApplicationName(context);

      result.type = type;

      if (topMargin != null)
        result.topMargin = Integer.valueOf(topMargin);

      return result;
    }

    public int topMargin() {
      switch (type) {
        case TEXT:
          return 12;
        case HEADER:
          return 12;
      }

      return 0;
    }

    public boolean isText() {
      return type == FieldType.TEXT;
    }

    public boolean isHeader() {
      return type == FieldType.HEADER;
    }
  }
}
