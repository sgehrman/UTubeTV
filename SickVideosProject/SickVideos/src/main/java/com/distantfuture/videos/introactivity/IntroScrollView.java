package com.distantfuture.videos.introactivity;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class IntroScrollView extends ScrollView {

  public IntroScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setVerticalFadingEdgeEnabled(true);
    setFadingEdgeLength(100);
  }

  public int getSolidColor() {
    return Color.rgb(0x00, 0x00, 0x00);
  }

}
