package com.distantfuture.videos.imageutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.distantfuture.iconicdroid.IconicFontDrawable;
import com.distantfuture.iconicdroid.icon.EntypoIcon;
import com.distantfuture.iconicdroid.icon.FontAwesomeIcon;
import com.distantfuture.iconicdroid.icon.Icon;
import com.distantfuture.videos.R;
import com.distantfuture.videos.misc.Utils;

public class ToolbarIcons {

  public static Drawable icon(Context context, IconID iconID, int iconColor, int sizeInDP) {
    StateListDrawable result = null;

    Icon icon = null;

    switch (iconID) {
      case NONE:
        break;
      case SOUND:
        icon = FontAwesomeIcon.VOLUME_UP;
        break;
      case STEP_BACK:
        icon = FontAwesomeIcon.STEP_BACKWARD;
        break;
      case STEP_FORWARD:
        icon = FontAwesomeIcon.STEP_FORWARD;
        break;
      case HEART:
        icon = FontAwesomeIcon.HEART;
        break;
      case CLOSE:
        icon = FontAwesomeIcon.TIMES_CIRCLE;
        break;
      case ADD:
        icon = FontAwesomeIcon.PLUS_CIRCLE;
        break;
      case REMOVE:
        icon = FontAwesomeIcon.MINUS_CIRCLE;
        break;
      case FULLSCREEN:
        icon = FontAwesomeIcon.ARROWS_ALT;
        break;
      case VIDEO_PLAY:
        icon = FontAwesomeIcon.YOUTUBE_PLAY;
        break;
      case LIST:
        icon = FontAwesomeIcon.LIST_UL;
        break;
      case OVERFLOW:
        icon = FontAwesomeIcon.ELLIPSIS_V;
        break;
      case ABOUT:
        icon = FontAwesomeIcon.INFO_CIRCLE;
        break;
      case PLAYLISTS:
        icon = FontAwesomeIcon.FILM;
        break;
      case YOUTUBE:
        icon = FontAwesomeIcon.YOUTUBE_SQUARE;
        break;
      case SEARCH:
        icon = EntypoIcon.SEARCH;
        break;
      case SEARCH_PLUS:
        icon = FontAwesomeIcon.SEARCH_PLUS;
        break;
      case UPLOADS:
        icon = FontAwesomeIcon.UPLOAD;
        break;
      case CHECK:
        icon = FontAwesomeIcon.CHECK_SQUARE;
        break;
      case FILM:
        icon = FontAwesomeIcon.FILM;
        break;
      case QUESTION_MARK:
        icon = FontAwesomeIcon.QUESTION_CIRCLE;
        break;
      case COMMENTS:
        icon = FontAwesomeIcon.COMMENTS_O;
        break;
      case COGS:
        icon = FontAwesomeIcon.COGS;
        break;
      case SITE_MAP:
        icon = FontAwesomeIcon.SITEMAP;
        break;
      case SETTINGS:
        icon = FontAwesomeIcon.COG;
        break;
      default:
        break;
    }

    if (icon != null) {
      Drawable pressed = null;
      Drawable normal = null;

      if (icon != null) {
        int padding = (int) Utils.dpToPx(4, context);

        IconicFontDrawable fpressed = new IconicFontDrawable(context);
        fpressed.setIcon(icon);
        fpressed.setIconColor(context.getResources().getColor(R.color.holo_blue));
        fpressed.setContour(Color.GRAY, 1);
        fpressed.setIconPadding(padding);

        IconicFontDrawable fnormal = new IconicFontDrawable(context);
        fnormal.setIcon(icon);
        fnormal.setIconColor(iconColor);
        fnormal.setContour(Color.GRAY, 1);
        fnormal.setIconPadding(padding);

        int size = (int) Utils.dpToPx(sizeInDP, context);
        fnormal.setIntrinsicWidth(size);
        fnormal.setIntrinsicHeight(size);
        fpressed.setIntrinsicWidth(size);
        fpressed.setIntrinsicHeight(size);

        normal = fnormal;
        pressed = fpressed;
      }

      result = new StateListDrawable();
      result.addState(new int[]{android.R.attr.state_pressed}, pressed);
      result.addState(new int[]{}, normal);
    }

    return result;
  }

  // this doesn't have the states like above. used to convert an icon to a simple bitmap, assuming things like animations will be faster with a bitmap over a text based drawable
  public static BitmapDrawable iconBitmap(Context context, IconID iconID, int iconColor, int sizeInDP) {
    Drawable iconDrawable = icon(context, iconID, iconColor, sizeInDP);

    if (iconDrawable != null) {
      int size = (int) Utils.dpToPx(sizeInDP, context);

      Bitmap map = Utils.drawableToBitmap(iconDrawable, size, size);

      return new BitmapDrawable(context.getResources(), map);
    }

    return null;
  }

  public static enum IconID {NONE, SOUND, ADD, REMOVE, COMMENTS, SITE_MAP, COGS, SEARCH_PLUS, STEP_FORWARD, QUESTION_MARK, STEP_BACK, FULLSCREEN, FILM, LIST, CLOSE, HEART, OVERFLOW, VIDEO_PLAY, ABOUT, UPLOADS, PLAYLISTS, YOUTUBE, CHECK, SEARCH, SETTINGS}

}
