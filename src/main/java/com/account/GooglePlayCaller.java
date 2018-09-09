package com.account;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Button;


import com.Tools.Tools;
import com.android.vending.billing.IInAppBillingService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Guillaume on 05/11/13.
 * Appel aux services Google Play
 */

public class GooglePlayCaller implements ServiceConnection {

    private IInAppBillingService _mService = null;
    private String TAG = "GooglePlayCaller";
    private Context _context = null;

    private Bundle _querySkus;
    private String _packageName = null;

    private int _caller = 0;

    Handler handler1 = new Handler()
    {
        /*@Override public void handleMessage(Message msg)
        {
            mBackground1.stop();
        }*/
    };

    public GooglePlayCaller(Context context) {
        _context = context;
    }

    public void bindGooglePlayServices() {
        _context.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
    }

    public void unBindGooglePlayServices() {
        if (_context != null) {
            Log.d(TAG, "unBindGooglePlayServices");
            _context.unbindService(this);
        }
        else
            Log.d(TAG, "unBindGooglePlayServices::_context null");
    }

    public void onServiceConnected(ComponentName name, IBinder service) {
        _mService = IInAppBillingService.Stub.asInterface(service);
        if (_mService != null) {
            if (Tools.CDDEBUG)
                Log.d(TAG, "onServiceConnected:: connected !");
        }
        else {
            if (Tools.CDDEBUG)
                Log.e(TAG, "onServiceConnected:: error while connecting to Google Play...");
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (Tools.CDDEBUG)
            Log.d(TAG, "onServiceDisconnected:: disconnected !");
        _mService = null;
    }


/*
    public GooglePlayCaller(ArrayList<String> args, String packageName, Context context, int caller) {
        _packageName = packageName;
        _context = context;
        _caller = caller;
        if (caller == 1) {
            if (Tools.CDDEBUG)
                Log.d(TAG, "1: Binding service...");
            _querySkus = new Bundle();
            _querySkus.putStringArrayList("ITEM_ID_LIST", args);
            context.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), this, Context.BIND_AUTO_CREATE);
        }
    }

    public void getPurchases() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.d(TAG, "getPurchases");

                    Bundle ownedItems = _mService.getPurchases(3, _packageName, "inapp", null);

                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                        String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = (String) purchaseDataList.get(i);
                            String signature = (String) (signatureList != null ? signatureList.get(i) : null);
                            String sku = (String) (ownedSkus != null ? ownedSkus.get(i) : null);
                            Log.d(TAG, purchaseData + "-" + sku);
                            if (purchaseData != null) {
                                JSONObject purchaseObj = new JSONObject(purchaseData);
                                if (purchaseObj.has("purchaseToken")) {
                                    response = _mService.consumePurchase(3, _packageName, purchaseObj.getString("purchaseToken"));
                                    Log.d(TAG, "Purchase response:: " + response);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        try {
            if (_caller == 1 && params[0].equals("1")) {
                Bundle skuDetails = _mService.getSkuDetails(3, _packageName, "inapp", _querySkus);
                int response = skuDetails.getInt("RESPONSE_CODE");
                if (response == 0) {
                    return skuDetails.getStringArrayList("DETAILS_LIST");
                }
            }
            else if (params[0].equals("2")) {
                Log.d(TAG, "doInBackground::params 2 ! id: " + params[1]);
                Log.d(TAG, "Package name: " + _packageName + ", params1: " + params[1] + ", params2 : " + params[2]);
                Bundle buyIntentBundle = _mService.getBuyIntent(3, _packageName, params[1], "inapp", params[2]);
                Log.d(TAG, "buyIntentBundle: " + String.valueOf(buyIntentBundle.getInt("RESPONSE_CODE")));
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                Log.d(TAG, "pendingIntent: " + String.valueOf(buyIntentBundle.getInt("RESPONSE_CODE")));
                ((Candles)_context).startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<String> responseList) {
        try {
            if (responseList != null) {
                if (_caller == 1) {
                    ((Candles)_context).onGooglePlayResult(responseList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete() {
        _querySkus = null;
        _packageName = null;
        _context.unbindService(this);
        _context = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "Service ok ! calling execute.");
        _mService = IInAppBillingService.Stub.asInterface(service);
        this.execute("1");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        _mService = null;
    }

    */
}
