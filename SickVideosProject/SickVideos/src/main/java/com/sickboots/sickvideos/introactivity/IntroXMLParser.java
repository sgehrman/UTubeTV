package com.sickboots.sickvideos.introactivity;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.misc.Debug;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IntroXMLParser {
  private final Context mContext;
  private IntroXMLParserListener mCallback;

  private IntroXMLParser(final Context context) {
    super();
    mContext = context.getApplicationContext();
  }

  public static void parseXML(Context context, IntroXMLParserListener callback) {
    new IntroXMLParser(context).parseXML(callback);
  }

  private static String parseDate(final Context context, final String dateString) {
    final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    try {
      final Date parsedDate = dateFormat.parse(dateString);
      return DateFormat.getDateFormat(context).format(parsedDate);
    } catch (ParseException ignored) {
      return dateString;
    }
  }

  private static IntroPage parsePageTag(Context context, final XmlPullParser resourceParser) throws XmlPullParserException, IOException {
    IntroPage result = null;
    String title, icon;
    List<IntroPageField> fields = new ArrayList<IntroPageField>();

    title = resourceParser.getAttributeValue(null, "title");
    icon = resourceParser.getAttributeValue(null, "icon");

    int eventType = resourceParser.getEventType();
    while (!(eventType == XmlPullParser.END_TAG && resourceParser.getName().equals("page"))) {
      if (eventType == XmlPullParser.START_TAG) {

        String name = resourceParser.getName();

        if (name.equals("header")) {
          resourceParser.next();

          String text = resourceParser.getText();

          IntroPageField field = IntroPageField.newField(text, IntroPageField.FieldType.HEADER);
          fields.add(field);
        } else if (name.equals("text")) {
            resourceParser.next();

            String text = resourceParser.getText();

            IntroPageField field = IntroPageField.newField(text, IntroPageField.FieldType.TEXT);
            fields.add(field);
        } else if (name.equals("bullet")) {
          resourceParser.next();

          String text = resourceParser.getText();

          IntroPageField field = IntroPageField.newField(text, IntroPageField.FieldType.BULLET);
          fields.add(field);
        }
      }
      eventType = resourceParser.next();
    }

    return IntroPage.newPage(title, icon, fields);
  }

  private List<IntroPage> getHTMLChangelog(final int resourceId, final Resources resources) {
    List<IntroPage> result = new ArrayList();

    final XmlResourceParser xml = resources.getXml(resourceId);
    try {
      int eventType = xml.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if ((eventType == XmlPullParser.START_TAG) && (xml.getName().equals("page"))) {

          IntroPage page = parsePageTag(mContext, xml);

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

  private void parseXML(final IntroXMLParserListener callback) {
    final Resources resources = mContext.getResources();

    new Thread(new Runnable() {
      @Override
      public void run() {
        final List<IntroPage> result = getHTMLChangelog(R.xml.quickguide, resources);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
            callback.parseXMLDone(result);
          }
        });
      }

    }).start();
  }

  public interface IntroXMLParserListener {
    public void parseXMLDone(List<IntroPage> pages);
  }

  public static class IntroPage {
    public String title;
    public String icon;
    public List<IntroPageField> fields;

    public static IntroPage newPage(String title, String icon, List<IntroPageField> fields) {
      IntroPage result = new IntroPage();

      result.title = title;
      result.icon = icon;
      result.fields = fields;

      return result;
    }
  }

  public static class IntroPageField {
    public String text;
    public enum FieldType {TEXT, HEADER, BULLET}
    public FieldType type;

    public static IntroPageField newField(String text, FieldType type) {
      IntroPageField result = new IntroPageField();

      result.text = text;
      result.type = type;

      return result;
    }

    public boolean isText() {
      return type == FieldType.TEXT;
    }
    public boolean isBullet() {
      return type == FieldType.BULLET;
    }
    public boolean isHeader() {
      return type == FieldType.HEADER;
    }
  }
}

