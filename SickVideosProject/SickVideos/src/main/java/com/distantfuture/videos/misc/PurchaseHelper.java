package com.distantfuture.videos.misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.distantfuture.videos.billing.IabHelper;
import com.distantfuture.videos.billing.IabResult;
import com.distantfuture.videos.billing.Inventory;
import com.distantfuture.videos.billing.Purchase;

import de.greenrobot.event.EventBus;

public class PurchaseHelper {
  private Context mContext;
  static final String SKU_DONATE_1 = "donate_1";
  static final int RC_REQUEST = 12001;
  IabHelper mHelper;
  private final String mPurchasePayload="purchase-payload";

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

      String sku = SKU_DONATE_1;
      if (sku == null)
        sku = "android.test.purchased";

      Purchase donation = inventory.getPurchase(sku);
      if (donation != null && verifyDeveloperPayload(donation)) {
        mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
        return;
      }

      setWaitScreen(false);
    }
  };

  // User clicked the "Buy Gas" button
  public void onDonateButtonClicked(View arg0, Activity activity) {
    setWaitScreen(true);
    Debug.log("Launching purchase flow for gas.");

    String sku = SKU_DONATE_1;
    if (sku == null)
      sku = "android.test.purchased";

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

      if (purchase.getSku().equals(SKU_DONATE_1)) {
        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
      }
    }
  };

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

  void setWaitScreen(boolean set) {
    EventBus.getDefault().post(new Events.PurchaseEvent(set ? "Please wait..." : null, null));
  }

  void showErrorAlert(String message) {
    showAlert("Error: " + message);
  }

  void showAlert(String message) {
    EventBus.getDefault().post(new Events.PurchaseEvent(null, message));
  }

  private String base64EncodedPublicKey() {
    // does breaking it up like this help?  whatever
    String result = null;

//    result += "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAscb1icDZU7808OcviDfPzFbimA0+ZKAwgs6W8HpEVaIpnRK";
//    result += "Pu4tWN1sId5cb3Ne0pENruUR27lZG9dks4qsiP5e+7R0H+DDOimt9SIpyY+fJ+/k3d5y";
//    result += "DqAGO3tpa1NiD9AkN1t5Ni9s6bmJiF0/+raT6cR1wko9OsJqp/7nFr/RRf65OWqKJk1FnieBMt6otXnnEIxnGl2";
//    result += "+8wMsBO3/N/fEi/cK23sF3QVzNq1GVBJa4Lw0svF0jrrS9uKheflsjBe67iWWUxYcVjK24BaTIJjDzUwuvmUKzz4lDW";
//    result += "zv8clIDfHXvfGiCI1LpBkYKJ8bX80G/Ywf8ccYXslPBfmMpXwIDAQAB";

    return result;
  }


}


