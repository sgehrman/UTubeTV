package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.imageutils.ToolbarIcons;
import com.sickboots.sickvideos.misc.Utils;

public class IntroPageFragment extends Fragment {

  public IntroPageFragment() {
    super();
  }

  public static IntroPageFragment newInstance(int sectionNumber) {
    IntroPageFragment fragment = new IntroPageFragment();
    Bundle args = new Bundle();
    args.putInt("sectionNumber", sectionNumber);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_intro_page, container, false);

    int sectionNumber = getArguments().getInt("sectionNumber");

    ActivityAccess access = (ActivityAccess) getActivity();
    IntroXMLParser.IntroPage page = access.pageAtIndex(sectionNumber);


    TextView titleView = (TextView) rootView.findViewById(R.id.title);
    TextView messageView = (TextView) rootView.findViewById(R.id.message);
    ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);


    if (page != null) {
      String message = "";

      String title = page.title;

      Drawable icon = null;
      int iconSize = 64;
      int color = getActivity().getResources().getColor(R.color.intro_drawable_color);

      if (page.icon.equals("info"))
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.UPLOADS, color, iconSize);
      else if (page.icon.equals("youtube"))
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.YOUTUBE, color, iconSize);
      else
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.HEART, color, iconSize);

      titleView.setText(title);
      messageView.setText(message);
      imageView.setImageDrawable(icon);

      // insert the fields
      ViewGroup fieldContainer = (ViewGroup) rootView.findViewById(R.id.field_container);

      for (IntroXMLParser.IntroPageField field : page.fields) {
        fieldContainer.addView(createFieldView(field));
      }

      // gets the content top centered
      View spacer = (View) rootView.findViewById(R.id.spacer_view);
      Display display = getActivity().getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      int offset = (int) (((float) size.y) * .2f);

      spacer.getLayoutParams().height = offset;
      spacer.setLayoutParams(spacer.getLayoutParams());
    } else
      titleView.setText("wtf?");

    return rootView;
  }

  public interface ActivityAccess {
    IntroXMLParser.IntroPage pageAtIndex(int position);

  }

  private View createFieldView(IntroXMLParser.IntroPageField field) {
    TextView textView = new TextView(getActivity());
    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    textView.setLayoutParams(params);
    textView.setAutoLinkMask(Linkify.ALL);
    textView.setTextSize(18);
    textView.setText(field.text);

    if (field.isHeader()) {
      textView.setTextSize(20);
      textView.setAllCaps(true);
      textView.setTextColor(0xff9988dd);
      textView.setTypeface(Typeface.DEFAULT_BOLD);
    }

      ImageView imageView = null;
    if (field.isBullet()) {
      imageView = new ImageView(getActivity());
      imageView.setImageResource(R.drawable.white_circle);
      int imageSize = (int) Utils.dpToPx(12, getActivity());

      LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(imageSize, imageSize);
      int leftMarginPx = (int) Utils.dpToPx(12, getActivity());
      int rightMarginPx = (int) Utils.dpToPx(6, getActivity());
      int topMarginPx = (int) Utils.dpToPx(7, getActivity());
      imageParams.setMargins(leftMarginPx,topMarginPx,rightMarginPx,0);
      imageView.setLayoutParams(imageParams);



//      int textPaddingPx = (int) Utils.dpToPx(12, getActivity());
//      imageView.setPadding(textPaddingPx, 0, 0, 0);

    }

    LinearLayout linearLayout = new LinearLayout(getActivity());
    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    if (imageView != null)
      linearLayout.addView(imageView);
    linearLayout.addView(textView);

    return linearLayout;
  }

}

