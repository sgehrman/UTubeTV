package com.sickboots.sickvideos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.sickboots.sickvideos.billing.IabHelper;
import com.sickboots.sickvideos.billing.IabResult;
import com.sickboots.sickvideos.billing.Inventory;
import com.sickboots.sickvideos.billing.Purchase;
import com.sickboots.sickvideos.misc.Debug;

public class PurchaseHelper {
  private Context mContext;
  private IabHelper mHelper;

  // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
  static final String SKU_PREMIUM = "premium";
  static final String SKU_GAS = "gas";

  // SKU for our subscription (infinite gas)
  static final String SKU_INFINITE_GAS = "infinite_gas";

  PurchaseHelper(Context context) {
    super();

    mContext = context.getApplicationContext();

    setupInAppPurchasing();

  }

  // called from Activities onDestroy
  void destroy() {
    if (mHelper != null) {
      mHelper.dispose();
      mHelper = null;
    }
  }

  public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    if (mHelper != null && mHelper.handleActivityResult(requestCode, resultCode, data)) {
      return true;
    }

    return false;
  }

  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
      // Have we been disposed of in the meantime? If so, quit.
      if (mHelper == null) return;

      // Is it a failure?
      if (result.isFailure()) {
        complain("Failed to query inventory: " + result);
        return;
      }

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

      // Do we have the premium upgrade?
      Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
//      mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));

      // Do we have the infinite gas plan?
      Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
//      mSubscribedToInfiniteGas = (infiniteGasPurchase != null && verifyDeveloperPayload(infiniteGasPurchase));

      // Check for gas delivery -- if we own gas, we should fill up the tank immediately
      Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
      if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
        mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
        return;
      }
    }
  };


  private void setupInAppPurchasing() {
    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAscb1icDZU7808OcviDfPzFbimA0+ZKAwgs6W8HpEVaIpnRKPu4tWN1sId5cb3Ne0pENruUR27lZG9dks4qsiP5e+7R0H+DDOimt9SIpyY+fJ+/k3d5yDqAGO3tpa1NiD9AkN1t5Ni9s6bmJiF0/+raT6cR1wko9OsJqp/7nFr/RRf65OWqKJk1FnieBMt6otXnnEIxnGl2+8wMsBO3/N/fEi/cK23sF3QVzNq1GVBJa4Lw0svF0jrrS9uKheflsjBe67iWWUxYcVjK24BaTIJjDzUwuvmUKzz4lDWzv8clIDfHXvfGiCI1LpBkYKJ8bX80G/Ywf8ccYXslPBfmMpXwIDAQAB";

    // Create the helper, passing it our context and the public key to verify signatures with
    mHelper = new IabHelper(mContext, base64EncodedPublicKey);

    // enable debug logging (for a production application, you should set this to false).
    mHelper.enableDebugLogging(true);

    // Start setup. This is asynchronous and the specified listener
    // will be called once setup completes.
    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          complain("Problem setting up in-app billing: " + result);
          return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (mHelper == null) return;

        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        mHelper.queryInventoryAsync(mGotInventoryListener);
      }
    });

  }

  private void complain(String message) {
    Debug.log("**** TrivialDrive Error: " + message);
    alert("Error: " + message);
  }

  private void alert(String message) {
    AlertDialog.Builder bld = new AlertDialog.Builder(mContext);
    bld.setMessage(message);
    bld.setNeutralButton("OK", null);
    Debug.log("Showing alert dialog: " + message);
    bld.create().show();
  }


  /**
   * Verifies the developer payload of a purchase.
   */
  boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

    return true;
  }

  // Callback for when a purchase is finished
  IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {

      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      if (result.isFailure()) {
        complain("Error purchasing: " + result);
        return;
      }
      if (!verifyDeveloperPayload(purchase)) {
        complain("Error purchasing. Authenticity verification failed.");
        return;
      }

      if (purchase.getSku().equals(SKU_GAS)) {
        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
      } else if (purchase.getSku().equals(SKU_PREMIUM)) {

      } else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {

      }
    }
  };

  // Called when consumption is complete
  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      // We know this is the "gas" sku because it's the only one we consume,
      // so we don't check which sku was consumed. If you have more than one
      // sku, you probably should check...
      if (result.isSuccess()) {
        // successfully consumed, so we apply the effects of the item in our
        // game world's logic, which in our case means filling the gas tank a bit
      } else {
        complain("Error while consuming: " + result);
      }
    }
  };


}
