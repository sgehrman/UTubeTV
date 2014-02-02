package com.sickboots.sickvideos.misc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.sickboots.sickvideos.R;

public class VideoMenuView extends ImageView {
  private static Drawable sharedDrawable;

  public interface VideoMenuViewListener {
    public void showVideoInfo(Long itemId);

    public void showVideoOnYouTube(Long itemId);

    public void hideVideo(Long itemId);
  }

  public Long mId;
  private VideoMenuViewListener mListener;

  public VideoMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);

    if (sharedDrawable == null) {
      sharedDrawable = ToolbarIcons.iconBitmap(getContext(), ToolbarIcons.IconID.OVERFLOW, 0xffdddddd, 30);
      sharedDrawable.setAlpha(200);  // 0-255
    }

    setImageDrawable(sharedDrawable);

    final PopupMenu popupMenu = new PopupMenu(getContext(), this);
    popupMenu.inflate(R.menu.video_menu);
    setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        popupMenu.show();
      }
    });

    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.video_menu_youtube:
            if (mListener != null)
              mListener.showVideoOnYouTube(mId);
            break;
          case R.id.video_menu_hide:
            if (mListener != null)
              mListener.hideVideo(mId);

            break;
        }
        return true;
      }
    });
  }

  public void setListener(VideoMenuViewListener listener) {
    mListener = listener;
  }

}

