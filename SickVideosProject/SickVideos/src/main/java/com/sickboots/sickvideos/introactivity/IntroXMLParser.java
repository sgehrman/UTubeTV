package com.sickboots.sickvideos.introactivity;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntroXMLParser {
  private final Activity mActivity;
  private IntroXMLParserListener mCallback;

  private IntroXMLParser(final Activity activity) {
    super();
    mActivity = activity;
  }

  public static void parseXML(Activity activity, IntroXMLParserListener callback) {
    new IntroXMLParser(activity).parseXML(callback);
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

  private static List<Map> parseReleaseTag(Context context, final XmlPullParser resourceParser) throws XmlPullParserException, IOException {
    List<Map> result = new ArrayList<Map>();
    Map<String, String> map;

    map = new HashMap<String, String>();
    map.put("title", resourceParser.getAttributeValue(null, "title"));
    result.add(map);

    map = new HashMap<String, String>();
    map.put("icon", resourceParser.getAttributeValue(null, "icon"));
    result.add(map);

    int eventType = resourceParser.getEventType();
    while (!(eventType == XmlPullParser.END_TAG && resourceParser.getName().equals("page"))) {
      if (eventType == XmlPullParser.START_TAG) {

        String name = resourceParser.getName();

        if (name.equals("text")) {
          resourceParser.next();

          map = new HashMap<String, String>();
          map.put("text", resourceParser.getText());
          result.add(map);
        }
        else if (name.equals("bullet")) {
          resourceParser.next();

          map = new HashMap<String, String>();
          map.put("bullet", resourceParser.getText());
          result.add(map);
        }
      }
      eventType = resourceParser.next();
    }

    return result;
  }

  private List<Map> getHTMLChangelog(final int resourceId, final Resources resources) {
    List<Map> result = new ArrayList();

    final XmlResourceParser xml = resources.getXml(resourceId);
    try {
      int eventType = xml.getEventType();
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if ((eventType == XmlPullParser.START_TAG) && (xml.getName().equals("page"))) {

          List<Map> list = parseReleaseTag(mActivity, xml);

          result.addAll(list);
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
    final Resources resources = mActivity.getResources();

    new Thread(new Runnable() {
      @Override
      public void run() {
        final List<Map> result = getHTMLChangelog(R.xml.quickguide, resources);

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
    public void parseXMLDone(List<Map> fieldList);
  }

}

