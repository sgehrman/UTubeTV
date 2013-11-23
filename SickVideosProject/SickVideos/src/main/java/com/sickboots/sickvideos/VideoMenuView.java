package com.sickboots.sickvideos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.sickboots.sickvideos.misc.ToolbarIcons;

public class VideoMenuView extends View {
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  public VideoMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);

    setBackground(ToolbarIcons.icon(getContext(), ToolbarIcons.IconID.STEP_FORWARD, Color.WHITE));

    final PopupMenu popupMenu = new PopupMenu(getContext(), this);
    popupMenu.inflate(R.menu.video_menu);
    setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        popupMenu.show();
      }
    });

    popupMenu.setOnMenuItemClickListener(
    new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.video_menu_info:
            break;
          case R.id.video_menu_youtube:
            break;
          case R.id.video_menu_hide:
            break;
        }
        return true;
      }
    });
  }

}


