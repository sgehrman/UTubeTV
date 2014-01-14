package com.sickboots.sickvideos;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.AppUtils;
import com.sickboots.sickvideos.misc.Debug;
import com.sickboots.sickvideos.misc.Preferences;
import com.sickboots.sickvideos.misc.StandardAnimations;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.VideoImageView;
import com.sickboots.sickvideos.youtube.ViewDecorations;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

public class YouTubeCursorAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener, View.OnClickListener {

  private static class Theme {
    float mTheme_imageAlpha;
    int mTheme_itemResId;
    int mTheme_resId;
    boolean mTheme_drawImageShadows;
    boolean mClickTextToExpand;
    int mTitleMaxLines;
    int mDescriptionMaxLines;
  }

  private static class ViewHolder {
    TextView title;
    TextView description;
    TextView duration;
    VideoImageView image;
    VideoMenuView menuButton;
  }

  private final LayoutInflater inflater;
  private int animationID = 0;
  private boolean showHidden = false;
  private Context mContext;
  private final YouTubeData mReusedData = new YouTubeData(); // avoids a memory alloc when drawing
  private Theme mTheme;
  private YouTubeServiceRequest mRequest;
  private YouTubeCursorAdapterListener mListener;
  private boolean mFadeInLoadedImages = false; // turned off for speed
  private boolean mClickAnimationsEnabled = false; // off for now
  private ViewDecorations mDecorations;

  public interface YouTubeCursorAdapterListener {
    public void handleClickFromAdapter(YouTubeData itemMap);

    public Activity accesActivity();
  }

  public static YouTubeCursorAdapter newAdapter(Context context, YouTubeServiceRequest request, YouTubeCursorAdapterListener listener) {
    Theme theme = newTheme(context);

    ViewDecorations decorations = new ViewDecorations(request.type() == YouTubeServiceRequest.RequestType.PLAYLISTS);
    decorations.setDrawShadows(theme.mTheme_drawImageShadows);

    int strokeColor = 0x88000000;
    int fillColor = 0x44000000;

    decorations.strokeAndFill(context, fillColor, strokeColor, 0.0f, 3);

    String[] from = new String[]{};
    int[] to = new int[]{};

    YouTubeCursorAdapter result = new YouTubeCursorAdapter(context, theme.mTheme_resId, null, from, to, 0);
    result.mTheme = theme;
    result.mDecorations = decorations;
    result.mRequest = request;
    result.mContext = context.getApplicationContext();
    result.mListener = listener;

    return result;
  }

  public ViewGroup rootView(ViewGroup container) {
    return (ViewGroup) inflater.inflate(mTheme.mTheme_resId, container, false);
  }

  private YouTubeCursorAdapter(Context context, int layout, Cursor c, String[] from,
                               int[] to, int flags) {
    super(context, layout, c, from, to, flags);

    inflater = LayoutInflater.from(context);
  }

  private void animateViewForClick(final View theView) {

    // disabled trying them all for now
    animationID = 1;

    switch (animationID) {
      case 0:
        StandardAnimations.dosomething(theView);
        break;
      case 1:
        StandardAnimations.upAndAway(theView);
        break;
      case 2:
        StandardAnimations.rockBounce(theView);
        break;
      case 3:
        StandardAnimations.winky(theView, mTheme.mTheme_imageAlpha);
        break;
      default:
        StandardAnimations.rubberClick(theView);
        animationID = -1;
        break;
    }

    animationID += 1;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View v, int position, long row) {
    ViewHolder holder = (ViewHolder) v.getTag();

    if (holder != null) {
      if (mClickAnimationsEnabled)
        animateViewForClick(holder.image);

      Cursor cursor = (Cursor) getItem(position);
      YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, null);

      mListener.handleClickFromAdapter(itemMap);
    } else {
      Debug.log("no holder on click?");
    }
  }

  @Override
  public void onClick(View v) {
    TextView textView = (TextView) v;

    // text bug.... mTitleMaxLines
    int maxLines = mTheme.mDescriptionMaxLines;

    textView.setMaxLines(textView.getMaxLines() < Integer.MAX_VALUE ? Integer.MAX_VALUE : maxLines);
    textView.invalidate();
  }

  private View prepareViews(View convertView, boolean multiColumns) {
    ViewHolder holder = null;

    if (convertView == null) {
      convertView = inflater.inflate(mTheme.mTheme_itemResId, null);

      holder = new ViewHolder();
      holder.image = (VideoImageView) convertView.findViewById(R.id.image);
      holder.title = (TextView) convertView.findViewById(R.id.text_view);
      holder.description = (TextView) convertView.findViewById(R.id.description_view);
      holder.duration = (TextView) convertView.findViewById(R.id.duration);
      holder.menuButton = (VideoMenuView) convertView.findViewById(R.id.menu_button);

      holder.image.setDecorations(mDecorations);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    if (holder != null) {
      boolean setClickListeners = false;

      // reset some stuff that might have been set on an animation
      if (mClickAnimationsEnabled) {
        // stops running animations?
        holder.image.setAnimation(null);

        holder.image.setAlpha(1.0f);
        holder.image.setScaleX(1.0f);
        holder.image.setScaleY(1.0f);
        holder.image.setRotationX(0.0f);
        holder.image.setRotationY(0.0f);
      }

      if (multiColumns) {
        // lowering these to get less wasted space at bottom
        int titleHeight = 1; // mTheme.mTitleMaxLines;
        int descriptionHeight = 2; // mTheme.mDescriptionMaxLines;

        holder.description.setMaxLines(descriptionHeight);
        holder.title.setMaxLines(titleHeight);

        // multiple columns must be same height
        holder.description.setMinLines(descriptionHeight);
        holder.title.setMinLines(titleHeight);
      }
      else {
        holder.description.setMaxLines(mTheme.mDescriptionMaxLines);
        holder.title.setMaxLines(mTheme.mTitleMaxLines);

        holder.description.setMinLines(0);
        holder.title.setMinLines(0);

        if (mTheme.mClickTextToExpand) {
          setClickListeners = true;
          holder.description.setOnClickListener(this);
          holder.title.setOnClickListener(this);
        }
      }

      if (!setClickListeners) {
        holder.description.setOnClickListener(null);
        holder.title.setOnClickListener(null);
      }
    }

    return convertView;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    GridView gridView = (GridView) parent;
    boolean multiColumns = gridView.getNumColumns() > 1;

   convertView = prepareViews(convertView, multiColumns);
   ViewHolder holder = (ViewHolder) convertView.getTag();

    Cursor cursor = (Cursor) getItem(position);
    YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, mReusedData);

    int defaultImageResID = 0;
    UrlImageViewHelper.setUrlDrawable(holder.image, itemMap.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

      @Override
      public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
        if (mFadeInLoadedImages && !loadedFromCache) {
          imageView.setAlpha(mTheme.mTheme_imageAlpha / 2);
          imageView.animate().setDuration(200).alpha(mTheme.mTheme_imageAlpha);
        } else
          imageView.setAlpha(mTheme.mTheme_imageAlpha);
      }

    });

    boolean hidden = false;
    if (!showHidden)
      hidden = itemMap.isHidden();

    if (hidden)
      holder.title.setText("(Hidden)");
    else
      holder.title.setText(itemMap.mTitle);

    String duration = itemMap.mDuration;
    if (duration != null) {
      holder.duration.setVisibility(View.VISIBLE);

      holder.duration.setText(duration);
    } else {

      Long count = itemMap.mItemCount;
      if (count != null) {
        holder.duration.setVisibility(View.VISIBLE);

        holder.duration.setText(count.toString() + (count == 1 ? " video" : " videos"));
      } else {
        holder.duration.setVisibility(View.GONE);
      }
    }

    // hide description if empty
    if (holder.description != null) {
      String desc = (String) itemMap.mDescription;
      if (desc != null && (desc.length() > 0)) {
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText(desc);
      } else {
        if (multiColumns) {
          holder.description.setVisibility(View.VISIBLE);
          holder.description.setText(" "); // min lines doesn't work with "", used a space
        } else
          holder.description.setVisibility(View.GONE);
      }
    }

    // set video id on menu button so clicking can know what video to act on
    if (itemMap.mVideo != null || itemMap.mPlaylist != null) {
      holder.menuButton.setVisibility(View.VISIBLE);
      holder.menuButton.setListener(this);
      holder.menuButton.mId = itemMap.mID;
    } else {
      holder.menuButton.setVisibility(View.GONE);
    }

    return convertView;
  }

  // VideoMenuViewListener
  @Override
  public void showVideoInfo(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null) {
      Debug.log("fix me");
    }
  }

  // VideoMenuViewListener
  @Override
  public void showVideoOnYouTube(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null) {
      if (videoMap.mVideo != null)
        YouTubeAPI.playMovieUsingIntent(mContext, videoMap.mVideo);
      else if (videoMap.mPlaylist != null)
        YouTubeAPI.openPlaylistUsingIntent(mListener.accesActivity(), videoMap.mPlaylist);
    }
  }

  // VideoMenuViewListener
  @Override
  public void hideVideo(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null) {
      videoMap.setHidden(!videoMap.isHidden());
      database.updateItem(videoMap);
    }
  }

  private static Theme newTheme(Context context) {
    Theme result = new Theme();

    String themeStyle = AppUtils.preferences(context).getString(Preferences.THEME_STYLE, Preferences.THEME_STYLE_DEFAULT);

    result.mClickTextToExpand = true;
    result.mTheme_imageAlpha = 1.0f;
    result.mTheme_drawImageShadows = false;
    result.mDescriptionMaxLines = context.getResources().getInteger(R.integer.description_max_lines);
    result.mTitleMaxLines = context.getResources().getInteger(R.integer.title_max_lines);

    switch (Integer.parseInt(themeStyle)) {
      case 0:
        result.mTheme_itemResId = R.layout.youtube_item_dark;
        result.mTheme_resId = R.layout.fragment_grid_dark;
        result.mTheme_drawImageShadows = true;
        result.mClickTextToExpand = false;
        break;
      case 1:
        result.mTheme_itemResId = R.layout.youtube_item_light;
        result.mTheme_resId = R.layout.fragment_grid_light;
        break;
      case 2:
        result.mTheme_itemResId = R.layout.youtube_item_card;
        result.mTheme_resId = R.layout.fragment_grid_card;
        break;
      case 3:
        result.mTheme_itemResId = R.layout.youtube_item_poster;
        result.mTheme_resId = R.layout.fragment_grid_card;
        break;
    }

    return result;
  }
}
