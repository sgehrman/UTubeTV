package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.sickboots.iconicdroid.IconicFontDrawable;
import com.sickboots.iconicdroid.icon.EntypoIcon;
import com.sickboots.iconicdroid.icon.FontAwesomeIcon;
import com.sickboots.iconicdroid.icon.Icon;
import com.sickboots.sickvideos.R;

/**
 * Created by sgehrman on 11/20/13.
 */
public class ToolbarIcons {

  public static enum IconID {SOUND, STEP_FORWARD, STEP_BACK, FULLSCREEN, CLOSE};

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
        icon = FontAwesomeIcon.REMOVE_SIGN;
        break;
      case FULLSCREEN:
        icon = FontAwesomeIcon.FULLSCREEN;
        break;
      default:
        icon = EntypoIcon.NEW;  // error
        break;
    }

    IconicFontDrawable pressed = new IconicFontDrawable(context);
    pressed.setIcon(icon);
    pressed.setIconColor(context.getResources().getColor(R.color.content_background));
    pressed.setContour(Color.GRAY, 1);
    pressed.setIconPadding(8);

    IconicFontDrawable normal = new IconicFontDrawable(context);
    normal.setIcon(icon);
    normal.setIconColor(iconColor);
    normal.setContour(Color.GRAY, 1);
    normal.setIconPadding(8);

    StateListDrawable states = new StateListDrawable();
    states.addState(new int[]{android.R.attr.state_pressed}, pressed);
    states.addState(new int[]{}, normal);

    return states;
  }

}
