package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.sickboots.iconicdroid.IconicFontDrawable;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;
import com.sickboots.sickvideos.R;

public class ToolbarIcons {

  public static enum IconID {SOUND, STEP_FORWARD, STEP_BACK, FULLSCREEN, CLOSE, OVERFLOW, VIDEO_PLAY}

  ;

  public static Drawable icon(Context context, IconID iconID, int iconColor) {
    Icon icon = null;

    switch (iconID) {
      case SOUND:
        icon = FontAwesomeIcon.VOLUME_UP;
        break;
      case STEP_BACK:
        icon = FontAwesomeIcon.STEP_BACKWARD;
        break;
      case STEP_FORWARD:
        icon = FontAwesomeIcon.STEP_FORWARD;
        break;
      case CLOSE:
        icon = FontAwesomeIcon.TIMES_CIRCLE;
        break;
      case FULLSCREEN:
        icon = FontAwesomeIcon.ARROWS_ALT;
        break;
      case VIDEO_PLAY:
        icon = FontAwesomeIcon.PLAY_CIRCLE;
        break;
      case OVERFLOW:
        // icon = null, makes icon from drawable further down
        break;
      default:
        break;
    }

    Drawable pressed = null;
    Drawable normal = null;

    if (icon != null) {
      IconicFontDrawable fpressed = new IconicFontDrawable(context);
      fpressed.setIcon(icon);
      fpressed.setIconColor(context.getResources().getColor(R.color.content_background));
      fpressed.setContour(Color.GRAY, 1);
      fpressed.setIconPadding(8);

      IconicFontDrawable fnormal = new IconicFontDrawable(context);
      fnormal.setIcon(icon);
      fnormal.setIconColor(iconColor);
      fnormal.setContour(Color.GRAY, 1);
      fnormal.setIconPadding(8);

      normal = fnormal;
      pressed = fpressed;
    } else {
      Drawable drawable = null;

      switch (iconID) {
        case SOUND:
        case STEP_BACK:
        case STEP_FORWARD:
        case CLOSE:
        case FULLSCREEN:
          Util.log("WTF, toolbar iconID bad?");
          break;
        case OVERFLOW:
          drawable = context.getResources().getDrawable(R.drawable.ic_action_overflow);
          normal = drawable;
          pressed = drawable;

          break;
        default:
          break;
      }
    }

    StateListDrawable states = new StateListDrawable();
    states.addState(new int[]{android.R.attr.state_pressed}, pressed);
    states.addState(new int[]{}, normal);

    return states;
  }

  // this doesn't have the states like above. used to convert an icon to a simple bitmap, assuming things like animations will be faster with a bitmap over a text based drawable
  public static BitmapDrawable iconBitmap(Context context, IconID iconID, int iconColor, int size) {
    BitmapDrawable result = null;

    Drawable iconDrawable = icon(context, iconID, iconColor);

    Bitmap map = Util.drawableToBitmap(iconDrawable, size);

    return new BitmapDrawable(context.getResources(), map);
  }

}
