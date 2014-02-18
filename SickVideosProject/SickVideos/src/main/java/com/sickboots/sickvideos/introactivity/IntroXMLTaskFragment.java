package com.sickboots.sickvideos.introactivity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import java.util.List;

public class IntroXMLTaskFragment extends Fragment {
  private List<IntroXMLParser.IntroPage> pages;
  private Callbacks mCallbacks;

  public static interface Callbacks {
    public void onNewPages();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mCallbacks = (Callbacks) activity;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Retain this fragment across configuration changes.
    setRetainInstance(true);

    IntroXMLParser.parseXML(getActivity(), new IntroXMLParser.IntroXMLParserListener() {
      @Override
      public void parseXMLDone(List<IntroXMLParser.IntroPage> newPages) {
        pages = newPages;
        mCallbacks.onNewPages();
      }
    });
  }

  public List<IntroXMLParser.IntroPage> getPages() {
    return pages;
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallbacks = null;
  }
}