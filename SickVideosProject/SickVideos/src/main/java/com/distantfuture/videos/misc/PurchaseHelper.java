package com.distantfuture.videos.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.distantfuture.videos.billing.IabHelper;
import com.distantfuture.videos.billing.IabResult;
import com.distantfuture.videos.billing.Inventory;
import com.distantfuture.videos.billing.Purchase;

import java.util.List;

import de.greenrobot.event.EventBus;

public class PurchaseHelper {
  static final int RC_REQUEST = 12001;
  private final String mPurchasePayload = "purchase-payload";
  IabHelper mHelper;
  // Called when consumption is complete
  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
      // if we were disposed of in the meantime, quit.
      if (mHelper == null)
        return;

      if (result.isSuccess()) {
        // successfully consumed, so we apply the effects of the item in our

      } else {
        showErrorAlert("Error while consuming: " + result);
      }

      setWaitScreen(false);
      Debug.log("End consumption flow.");
    }
  };
  // Callback for when a purchase is finished
  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
      Debug.log("Purchase finished: " + result + ", purchase: " + purchase);

      // if we were disposed of in the meantime, quit.
      if (mHelper == null)
        return;

      if (result.isFailure()) {
        showErrorAlert("Error purchasing: " + result);
        setWaitScreen(false);
        return;
      }
      if (!verifyDeveloperPayload(purchase)) {
        showErrorAlert("Error purchasing. Authenticity verification failed.");
        setWaitScreen(false);
        return;
      }

      mHelper.consumeAsync(purchase, mConsumeFinishedListener);
    }
  };
  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
      // Have we been disposed of in the meantime? If so, quit.
      if (mHelper == null)
        return;

      // Is it a failure?
      if (result.isFailure()) {
        showErrorAlert("Failed to query inventory: " + result);
        return;
      }

      List<Purchase> purchases = inventory.getAllPurchases();

      for (Purchase purchase : purchases) {
        if (verifyDeveloperPayload(purchase)) {
          mHelper.consumeAsync(purchase, mConsumeFinishedListener);
        }
      }

      setWaitScreen(false);
    }
  };
  private Context mContext;

  public PurchaseHelper(Context context) {
    super();

    mContext = context.getApplicationContext();

    setupInAppPurchasing();
  }

  // call from Activities onDestroy, must be called
  public void destroy() {
    if (mHelper != null) {
      mHelper.dispose();
      mHelper = null;
    }
  }

  // call from Activity, must be called
  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    return (mHelper != null && mHelper.handleActivityResult(requestCode, resultCode, data));
  }

  private void setupInAppPurchasing() {
    // Create the helper, passing it our context and the public key to verify signatures with
    mHelper = new IabHelper(mContext, base64EncodedPublicKey());

    mHelper.enableDebugLogging(Debug.isDebugBuild());

    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          showErrorAlert("Problem setting up in-app billing: " + result);
          return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (mHelper == null)
          return;

        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        mHelper.queryInventoryAsync(mGotInventoryListener);
      }
    });
  }

  public void onDonateButtonClicked(View arg0, Activity activity, String sku) {
    setWaitScreen(true);

    mHelper.launchPurchaseFlow(activity, sku, RC_REQUEST, mPurchaseFinishedListener, mPurchasePayload);
  }

  /**
   * Verifies the developer payload of a purchase.
   */
  boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();

    // lame security check but not needed for donation
    if (!payload.equals(mPurchasePayload)) {
      Debug.log("mPurchasePayload didn't match?");
      return false;
    }

    return true;
  }

  void setWaitScreen(boolean show) {
    EventBus.getDefault().post(new Events.PurchaseEvent(show ? "Please wait..." : null, null));
  }

  void showErrorAlert(String message) {
    showAlert("Error: " + message);
  }

  void showAlert(String message) {
    EventBus.getDefault().post(new Events.PurchaseEvent(null, message));
  }

  private String base64EncodedPublicKey() {
    String result = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxbbmCXO30gPiPWdQwzhnj5I/6S4a4OmQp7f+2qg" +
        "jczTNBMa01lrAQB/uiwfQToQgdkaBTgFw3epZpH4rOBp/p4lfRIfpspL1+qWLJHmD+zUkEhZPq798cLNrXfoiLgnL5s46P" +
        "KEyO8WVHBD3cOnmbQ7NyU1vN0/qJbEwe1MsDyzsHWOBO4TvVrWL14hnQNiTFbZavMtolNjyyZDZC7yRiqz9J0bzCAT1uW9B" +
        "B5+uBMxjLAF/PolgnZfupI9s2smSQuiakEY0ZdWumxYzM8NUGj1A/byBtKUOl+7K92o//k141Gd9vylt6kTwC86Ik4am5EBJcB4makCWbCxP5KyiRwIDAQAB";

    return result;
  }

}


