package com.sickboots.sickvideos.misc;

import com.sickboots.sickvideos.content.Content;

public class Events {

  public static class ContentEvent {
    private Content mContent;

    public ContentEvent(Content content) {
      mContent = content;
    }

    public Content getContent() {
      return mContent;
    }
  }

}
