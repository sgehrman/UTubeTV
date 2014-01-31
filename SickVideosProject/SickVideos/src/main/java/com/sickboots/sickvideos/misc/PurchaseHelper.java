package com.sickboots.sickvideos.misc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.sickboots.sickvideos.billing.IabHelper;
import com.sickboots.sickvideos.billing.IabResult;
import com.sickboots.sickvideos.billing.Inventory;
import com.sickboots.sickvideos.billing.Purchase;

public class PurchaseHelper {
  private Context mContext;

  boolean mIsPremium = false;
  boolean mSubscribedToInfiniteGas = false;

  static final String SKU_PREMIUM = "premium";
  static final String SKU_GAS = "gas";
  static final String SKU_INFINITE_GAS = "infinite_gas";

  static final int RC_REQUEST = 10001;

  static final int TANK_MAX = 4;
  int mTank;
  IabHelper mHelper;

  PurchaseHelper(Context context) {
    super();

    mContext = context.getApplicationContext();

    setupInAppPurchasing();
  }

  // called from Activities onDestroy, must be called
  public void destroy() {
    Debug.log("Destroying helper.");

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


  private void setupInAppPurchasing() {

    // load game data
    loadData();

    // Create the helper, passing it our context and the public key to verify signatures with
    Debug.log("Creating IAB helper.");
    mHelper = new IabHelper(mContext, base64EncodedPublicKey());

    // enable debug logging (for a production application, you should set this to false).
    mHelper.enableDebugLogging(true);

    // Start setup. This is asynchronous and the specified listener
    // will be called once setup completes.
    Debug.log("Starting setup.");
    mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
      public void onIabSetupFinished(IabResult result) {
        Debug.log("Setup finished.");

        if (!result.isSuccess()) {
          // Oh noes, there was a problem.
          complain("Problem setting up in-app billing: " + result);
          return;
        }

        // Have we been disposed of in the meantime? If so, quit.
        if (mHelper == null) return;

        // IAB is fully set up. Now, let's get an inventory of stuff we own.
        Debug.log("Setup successful. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
      }
    });
  }


  // Listener that's called when we finish querying the items and subscriptions we own
  IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
      Debug.log("Query inventory finished.");

      // Have we been disposed of in the meantime? If so, quit.
      if (mHelper == null) return;

      // Is it a failure?
      if (result.isFailure()) {
        complain("Failed to query inventory: " + result);
        return;
      }

      Debug.log("Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

      // Do we have the premium upgrade?
      Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
      mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
      Debug.log("User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

      // Do we have the infinite gas plan?
      Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
      mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
          verifyDeveloperPayload(infiniteGasPurchase));
      Debug.log("User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
          + " infinite gas subscription.");
      if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

      // Check for gas delivery -- if we own gas, we should fill up the tank immediately
      Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
      if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
        Debug.log("We have gas. Consuming it.");
        mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
        return;
      }

      updateUi();
      setWaitScreen(false);
      Debug.log("Initial inventory query finished; enabling main UI.");
    }
  };

  // User clicked the "Buy Gas" button
  public void onBuyGasButtonClicked(View arg0, Activity activity) {
    Debug.log("Buy gas button clicked.");

    if (mSubscribedToInfiniteGas) {
      complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
      return;
    }

    if (mTank >= TANK_MAX) {
      complain("Your tank is full. Drive around a bit!");
      return;
    }

    // launch the gas purchase UI flow.
    // We will be notified of completion via mPurchaseFinishedListener
    setWaitScreen(true);
    Debug.log("Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    mHelper.launchPurchaseFlow(activity, SKU_GAS, RC_REQUEST,
        mPurchaseFinishedListener, payload);
  }

  // User clicked the "Upgrade to Premium" button.
  public void onUpgradeAppButtonClicked(View arg0, Activity activity) {
    Debug.log("Upgrade button clicked; launching purchase flow for upgrade.");
    setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    mHelper.launchPurchaseFlow(activity, SKU_PREMIUM, RC_REQUEST,
        mPurchaseFinishedListener, payload);
  }

  // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
  // flow for subscription.
  public void onInfiniteGasButtonClicked(View arg0, Activity activity) {
    if (!mHelper.subscriptionsSupported()) {
      complain("Subscriptions not supported on your device yet. Sorry!");
      return;
    }

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    setWaitScreen(true);
    Debug.log("Launching purchase flow for infinite gas subscription.");
    mHelper.launchPurchaseFlow(activity,
        SKU_INFINITE_GAS, IabHelper.ITEM_TYPE_SUBS,
        RC_REQUEST, mPurchaseFinishedListener, payload);
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
      Debug.log("Purchase finished: " + result + ", purchase: " + purchase);

      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      if (result.isFailure()) {
        complain("Error purchasing: " + result);
        setWaitScreen(false);
        return;
      }
      if (!verifyDeveloperPayload(purchase)) {
        complain("Error purchasing. Authenticity verification failed.");
        setWaitScreen(false);
        return;
      }

      Debug.log("Purchase successful.");

      if (purchase.getSku().equals(SKU_GAS)) {
        // bought 1/4 tank of gas. So consume it.
        Debug.log("Purchase is gas. Starting gas consumption.");
        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
      } else if (purchase.getSku().equals(SKU_PREMIUM)) {
        // bought the premium upgrade!
        Debug.log("Purchase is premium upgrade. Congratulating user.");
        alert("Thank you for upgrading to premium!");
        mIsPremium = true;
        updateUi();
        setWaitScreen(false);
      } else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {
        // bought the infinite gas subscription
        Debug.log("Infinite gas subscription purchased.");
        alert("Thank you for subscribing to infinite gas!");
        mSubscribedToInfiniteGas = true;
        mTank = TANK_MAX;
        updateUi();
        setWaitScreen(false);
      }
    }
  };

  // Called when consumption is complete
  IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
      Debug.log("Consumption finished. Purchase: " + purchase + ", result: " + result);

      // if we were disposed of in the meantime, quit.
      if (mHelper == null) return;

      // We know this is the "gas" sku because it's the only one we consume,
      // so we don't check which sku was consumed. If you have more than one
      // sku, you probably should check...
      if (result.isSuccess()) {
        // successfully consumed, so we apply the effects of the item in our
        // game world's logic, which in our case means filling the gas tank a bit
        Debug.log("Consumption successful. Provisioning.");
        mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
        saveData();
        alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
      } else {
        complain("Error while consuming: " + result);
      }
      updateUi();
      setWaitScreen(false);
      Debug.log("End consumption flow.");
    }
  };

  // Drive button clicked. Burn gas!
  public void onDriveButtonClicked(View arg0) {
    Debug.log("Drive button clicked.");
    if (!mSubscribedToInfiniteGas && mTank <= 0)
      alert("Oh, no! You are out of gas! Try buying some!");
    else {
      if (!mSubscribedToInfiniteGas) --mTank;
      saveData();
      alert("Vroooom, you drove a few miles.");
      updateUi();
      Debug.log("Vrooom. Tank is now " + mTank);
    }
  }

  // updates UI to reflect model
  public void updateUi() {
    Debug.log("updating UI");

//      // update the car color to reflect premium status or lack thereof
//      ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);
//
//      // "Upgrade" button is only visible if the user is not premium
//      findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);
//
//      // "Get infinite gas" button is only visible if the user is not subscribed yet
//      findViewById(R.id.infinite_gas_button).setVisibility(mSubscribedToInfiniteGas ?
//          View.GONE : View.VISIBLE);
//
//      // update gas gauge to reflect tank status
//      if (mSubscribedToInfiniteGas) {
//        ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
//      }
//      else {
//        int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
//        ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
//      }
  }

  // Enables or disables the "please wait" screen.
  void setWaitScreen(boolean set) {
    Debug.log("waiting: " + (set ? "yes" : "no"));

//      findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//      findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
  }

  void complain(String message) {
    alert("Error: " + message);
  }

  void alert(String message) {
//      AlertDialog.Builder bld = new AlertDialog.Builder(mContext);
//      bld.setMessage(message);
//      bld.setNeutralButton("OK", null);
    Debug.log("Showing alert dialog: " + message);
//      bld.create().show();
  }

  void saveData() {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

//      SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
//      spe.putInt("tank", mTank);
//      spe.commit();
    Debug.log("Saved data: tank = " + String.valueOf(mTank));
  }

  void loadData() {
//      SharedPreferences sp = getPreferences(MODE_PRIVATE);
//      mTank = sp.getInt("tank", 2);
    Debug.log("Loaded data: tank = " + String.valueOf(mTank));
  }

  private String base64EncodedPublicKey() {
    // does breaking it up like this help?  whatever
    String result = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAscb1icDZU7808OcviDfPzFbimA0+ZKAwgs6W8HpEVaIpnRK";
    result += "Pu4tWN1sId5cb3Ne0pENruUR27lZG9dks4qsiP5e+7R0H+DDOimt9SIpyY+fJ+/k3d5y";
    result += "DqAGO3tpa1NiD9AkN1t5Ni9s6bmJiF0/+raT6cR1wko9OsJqp/7nFr/RRf65OWqKJk1FnieBMt6otXnnEIxnGl2";
    result += "+8wMsBO3/N/fEi/cK23sF3QVzNq1GVBJa4Lw0svF0jrrS9uKheflsjBe67iWWUxYcVjK24BaTIJjDzUwuvmUKzz4lDW";
    result += "zv8clIDfHXvfGiCI1LpBkYKJ8bX80G/Ywf8ccYXslPBfmMpXwIDAQAB";

    return result;
  }


}


