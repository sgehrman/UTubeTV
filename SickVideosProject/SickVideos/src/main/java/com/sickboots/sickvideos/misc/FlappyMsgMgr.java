package com.sickboots.sickvideos.misc;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.LinkedList;
import java.util.Queue;

class FlappyMsgMgr extends Handler {

  private static final int MESSAGE_DISPLAY = 0xc2007;
  private static final int MESSAGE_ADD_VIEW = 0xc20074dd;
  private static final int MESSAGE_REMOVE = 0xc2007de1;

  private static FlappyMsgMgr mInstance;

  private Queue<FlappyMsg> msgQueue;
  private Animation inAnimation, outAnimation;

  private FlappyMsgMgr() {
    msgQueue = new LinkedList<FlappyMsg>();
  }

  static synchronized FlappyMsgMgr getInstance() {
    if (mInstance == null) {
      mInstance = new FlappyMsgMgr();
    }
    return mInstance;
  }

  void add(FlappyMsg appMsg) {
    msgQueue.add(appMsg);
    if (inAnimation == null) {
      inAnimation = AnimationUtils.loadAnimation(appMsg.getActivity(), android.R.anim.fade_in);
    }
    if (outAnimation == null) {
      outAnimation = AnimationUtils.loadAnimation(appMsg.getActivity(), android.R.anim.fade_out);
    }
    displayMsg();
  }

  void clearMsg(FlappyMsg appMsg) {
    if (msgQueue.contains(appMsg)) {
      // Avoid the message from being removed twice.
      removeMessages(MESSAGE_REMOVE);
      msgQueue.remove(appMsg);
      removeMsg(appMsg);
    }
  }

  void clearAllMsg() {
    if (msgQueue != null) {
      msgQueue.clear();
    }
    removeMessages(MESSAGE_DISPLAY);
    removeMessages(MESSAGE_ADD_VIEW);
    removeMessages(MESSAGE_REMOVE);
  }

  private void displayMsg() {
    if (msgQueue.isEmpty()) {
      return;
    }
    // First peek whether the FlappyMsg is being displayed.
    final FlappyMsg appMsg = msgQueue.peek();
    // If the activity is null we throw away the FlappyMsg.
    if (appMsg.getActivity() == null) {
      msgQueue.poll();
    }
    final Message msg;
    if (!appMsg.isShowing()) {
      // Display the FlappyMsg
      msg = obtainMessage(MESSAGE_ADD_VIEW);
      msg.obj = appMsg;
      sendMessage(msg);
    } else {
      msg = obtainMessage(MESSAGE_DISPLAY);
      sendMessageDelayed(msg, appMsg.getDuration() + inAnimation.getDuration() + outAnimation.getDuration());
    }
  }

  private void removeMsg(final FlappyMsg appMsg) {
    ViewGroup parent = ((ViewGroup) appMsg.getView().getParent());
    if (parent != null) {
      outAnimation.setAnimationListener(new OutAnimationListener(appMsg));
      appMsg.getView().startAnimation(outAnimation);
      // Remove the FlappyMsg from the queue.
      msgQueue.poll();
        parent.removeView(appMsg.getView());

      Message msg = obtainMessage(MESSAGE_DISPLAY);
      sendMessage(msg);
    }
  }

  private void addMsgToView(FlappyMsg appMsg) {
    View view = appMsg.getView();
    if (view.getParent() == null) {
      appMsg.getActivity().addContentView(view, appMsg.getLayoutParams());
    }
    view.startAnimation(inAnimation);
    if (view.getVisibility() != View.VISIBLE) {
      view.setVisibility(View.VISIBLE);
    }
    final Message msg = obtainMessage(MESSAGE_REMOVE);
    msg.obj = appMsg;
    sendMessageDelayed(msg, appMsg.getDuration());
  }

  @Override
  public void handleMessage(Message msg) {
    final FlappyMsg appMsg;
    switch (msg.what) {
      case MESSAGE_DISPLAY:
        displayMsg();
        break;
      case MESSAGE_ADD_VIEW:
        appMsg = (FlappyMsg) msg.obj;
        addMsgToView(appMsg);
        break;
      case MESSAGE_REMOVE:
        appMsg = (FlappyMsg) msg.obj;
        removeMsg(appMsg);
        break;
      default:
        super.handleMessage(msg);
        break;
    }
  }

  private static class OutAnimationListener implements Animation.AnimationListener {

    private FlappyMsg appMsg;

    private OutAnimationListener(FlappyMsg appMsg) {
      this.appMsg = appMsg;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
  }
}