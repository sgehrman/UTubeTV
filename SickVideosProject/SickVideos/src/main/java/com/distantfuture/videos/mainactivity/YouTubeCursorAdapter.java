package com.distantfuture.videos.mainactivity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.distantfuture.videos.R;
import com.distantfuture.videos.database.DatabaseAccess;
import com.distantfuture.videos.database.YouTubeData;
import com.distantfuture.videos.misc.AppUtils;
import com.distantfuture.videos.misc.Debug;
import com.distantfuture.videos.misc.StandardAnimations;
import com.distantfuture.videos.misc.VideoMenuView;
import com.distantfuture.videos.services.YouTubeServiceRequest;
import com.distantfuture.videos.youtube.VideoImageView;
import com.distantfuture.videos.youtube.ViewDecorations;
import com.distantfuture.videos.youtube.YouTubeAPI;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class YouTubeCursorAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener, View.OnClickListener {

  private final LayoutInflater inflater;
  private final YouTubeData mReusedData = new YouTubeData(); // avoids a memory alloc when drawing
  private int animationID = 0;
  private Context mContext;
  private Theme mTheme;
  private YouTubeServiceRequest mRequest;
  private YouTubeCursorAdapterListener mListener;
  private boolean mFadeInLoadedImages = false; // turned off for speed
  private boolean mClickAnimationsEnabled = false; // off for now
  private ViewDecorations mDecorations;
  private final PublishedDateCache mDateCache = new PublishedDateCache();

  private YouTubeCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
    super(context, layout, c, from, to, flags);

    inflater = LayoutInflater.from(context);

    DataSetObserver dataSetObserver = new DataSetObserver() {
      @Override
      public void onChanged() {
        mListener.adapterDataChanged();
      }
    };
    registerDataSetObserver(dataSetObserver);
  }

  public static YouTubeCursorAdapter newAdapter(Context context, YouTubeServiceRequest request, YouTubeCursorAdapterListener listener) {
    Theme theme = newTheme(context);

    ViewDecorations decorations = new ViewDecorations(context, request.type() == YouTubeServiceRequest.RequestType.PLAYLISTS);
    decorations.setDrawShadows(theme.mTheme_drawImageShadows);

    int strokeColor = context.getResources().getColor(R.color.card_fill_color);

    decorations.strokeAndFill(context, theme.mCardImageFillColor, strokeColor, 8, 4);

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

  private static Theme newTheme(Context context) {
    Theme result = new Theme();

    int themeStyle = AppUtils.instance(context).themeId();

    result.mClickTextToExpand = true;
    result.mTheme_drawImageShadows = false;
    result.mDescriptionMaxLines = 2;
    result.mTitleMaxLines = context.getResources().getInteger(R.integer.title_max_lines);
    result.mSupportsMenuButton = false;
    result.mCardImageFillColor = context.getResources()
        .getColor(R.color.card_image_fill); // an app can set to transparent to turn this off

    switch (themeStyle) {
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

  public ViewGroup rootView(ViewGroup container) {
    return (ViewGroup) inflater.inflate(mTheme.mTheme_resId, container, false);
  }

  private void animateViewForClick(final View theView) {
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
        StandardAnimations.winky(theView, 1.0f);
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

      if (itemMap != null) {
        // if hidden, a click unhides it
        if (itemMap.isHidden()) {
          DatabaseAccess database = new DatabaseAccess(mContext, mRequest);

          itemMap.setHidden(false);
          database.updateItem(itemMap);
        } else {
          mListener.handleClickFromAdapter(position, itemMap);
        }
      } else {
        Debug.log("no holder on click?");
      }
    }
  }

  @Override
  public void onClick(View v) {
    ViewGroup row = (ViewGroup) v.getParent();
    TextView titleView = (TextView) row.findViewById(R.id.text_view);
    TextView descriptionView = (TextView) row.findViewById(R.id.description_view);

    if (titleView != null) {
      boolean setMax = titleView.getMaxLines() < Integer.MAX_VALUE;

      titleView.setMaxLines(setMax ? Integer.MAX_VALUE : mTheme.mTitleMaxLines);
    }
    if (descriptionView != null) {
      boolean setMax = descriptionView.getMaxLines() < Integer.MAX_VALUE;

      descriptionView.setMaxLines(setMax ? Integer.MAX_VALUE : mTheme.mDescriptionMaxLines);

      if (setMax)
        descriptionView.setAutoLinkMask(Linkify.ALL);
      else
        descriptionView.setAutoLinkMask(0);

      // toggles links by setting text again
      String sequence = descriptionView.getText()
          .toString(); // getText() could return a StringSpanner, toString() gets the raw string
      descriptionView.setText(sequence);
    }
  }

  private View prepareViews(View convertView, ViewGroup parent, boolean multiColumns) {
    ViewHolder holder;

    // ## This fixes a problem with the SwipeToDismissAdapter.
    // After dismiss, the view is left in a state with a zero height
    // rather than fixing it, just refusing to reuse it.
    if (convertView != null) {
      ViewGroup.LayoutParams lp = convertView.getLayoutParams();
      if (lp != null && lp.height == 0) {
        Debug.log("not reusing dismissed view");
        convertView = null;
      }
    }

    if (convertView == null) {
      convertView = inflater.inflate(mTheme.mTheme_itemResId, parent, false);

      holder = new ViewHolder();
      holder.image = (VideoImageView) convertView.findViewById(R.id.image);
      holder.title = (TextView) convertView.findViewById(R.id.text_view);
      holder.description = (TextView) convertView.findViewById(R.id.description_view);
      holder.duration = (TextView) convertView.findViewById(R.id.duration);
      holder.pubDate = (TextView) convertView.findViewById(R.id.pub_date);
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

      // restore this, user could have clicked on it
      holder.description.setAutoLinkMask(0);

      if (multiColumns) {
        // lowering these to get less wasted space at bottom
        int titleHeight = 2; // mTheme.mTitleMaxLines;
        int descriptionHeight = 2; // mTheme.mDescriptionMaxLines;

        holder.description.setMaxLines(descriptionHeight);
        holder.title.setMaxLines(titleHeight);

        // multiple columns must be same height
        holder.description.setMinLines(descriptionHeight);
        holder.title.setMinLines(titleHeight);
      } else {
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

    convertView = prepareViews(convertView, parent, multiColumns);
    ViewHolder holder = (ViewHolder) convertView.getTag();

    Cursor cursor = (Cursor) getItem(position);
    YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, mReusedData);

    Picasso.with(mContext).load(itemMap.mThumbnail)
        //          .fit()
        //          .noFade()
        //          .resize(250, 250) // put into dimens for dp values
        .into(holder.image);

    boolean hidden = itemMap.isHidden();
    holder.image.setDrawHiddenIndicator(hidden);

    holder.title.setText(itemMap.mTitle);

    // hide description if empty
    if (holder.description != null) {
      String desc = itemMap.mDescription;
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
    if (mTheme.mSupportsMenuButton && (itemMap.mVideo != null || itemMap.mPlaylist != null)) {
      holder.menuButton.setVisibility(View.VISIBLE);
      holder.menuButton.setListener(this);
      holder.menuButton.mId = itemMap.mID;
    } else {
      holder.menuButton.setVisibility(View.GONE);
    }

    String duration = itemMap.mDuration;
    if (duration != null) {
      holder.duration.setText(duration);
    } else {
      Long count = itemMap.mItemCount;
      if (count != null) {
        holder.duration.setText(count.toString() + (count == 1 ? " video" : " videos"));
      } else {
        holder.duration.setText("");
      }
    }

    Spannable date = mDateCache.spannable(itemMap.mPublishedDate);
    if (date != null) {
      holder.pubDate.setText(date);
    } else {
      holder.pubDate.setText("");
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

  public interface YouTubeCursorAdapterListener {
    public void handleClickFromAdapter(int position, YouTubeData itemMap);

    public void adapterDataChanged();

    public Activity accesActivity();
  }

  private static class Theme {
    int mTheme_itemResId;
    int mTheme_resId;
    boolean mTheme_drawImageShadows;
    boolean mClickTextToExpand;
    int mTitleMaxLines;
    int mDescriptionMaxLines;
    boolean mSupportsMenuButton;
    int mCardImageFillColor;
  }

  private static class ViewHolder {
    TextView title;
    TextView description;
    TextView duration;
    VideoImageView image;
    VideoMenuView menuButton;
    TextView pubDate;
  }

  // cached, used as an optimization, getView() 0,0,0, muliple times, so don't keep building this over and over
  private static class PublishedDateCache {
    private final LruCache cache = new LruCache(50);
    private final PrettyTime mDateFormatter = new PrettyTime();
    private final Date mDate = new Date();  // avoiding an alloc every call, just set the time
    private final String mTitle = "Published: ";
    private final StyleSpan mBoldSpan = new StyleSpan(Typeface.BOLD);
    private final ForegroundColorSpan mColorSpan = new ForegroundColorSpan(0xffb1e2ff);

    public Spannable spannable(long date) {
      // is it cached?
      Spannable result = (Spannable) cache.get(date);

      if (result == null) {
        // avoiding an alloc during draw, reusing Date
        mDate.setTime(date);
        String content = mDateFormatter.format(mDate);

        result = new SpannableString(mTitle + content);
        result.setSpan(mBoldSpan, 0, mTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result.setSpan(mColorSpan, mTitle.length(), mTitle.length() + content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // add to cache
        cache.put(date, result);
      }

      return result;
    }

  }
}
