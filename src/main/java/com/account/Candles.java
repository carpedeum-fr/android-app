package com.account;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Tools.LogInFile;
import com.Tools.Tools;
import com.android.vending.billing.IInAppBillingService;
import com.billing.IabHelper;
import com.billing.IabResult;
import com.billing.Inventory;
import com.billing.Purchase;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 04/11/13.
 * Gestion des cierges virtuels
 */

public class Candles extends Activity implements ServiceConnection, ApiCaller {

    int _resId = 0;
    static final String TAG = "Candles";
    private IInAppBillingService _mService = null;

    //TODO pool de threads ?
    private Thread _thread1 = null;
    private Thread _thread2 = null;
    private Thread _thread3 = null;
    private Thread _thread4 = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_account_candles);
        Intent myIntent = getIntent();
        if (myIntent.getExtras() != null) {
            setLayoutTheme(myIntent.getExtras().getInt("resId"));
            findViewById(R.id.imageView_headerLogo).setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.CANDLES));
            TextView candlesRemainingTV = (TextView)findViewById(R.id.textView_candles_remain_com_account_candles);
            candlesRemainingTV.setText(getString(R.string.ACCOUNT_CANDLES_d_REMAINING_FEW, UserConnected.getInstance().get_num_candles()));
        }

        Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        intent.setPackage("com.android.vending");
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    private void setLayoutTheme(int resId) {
        _resId = resId;
        if (resId == 0 || resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        }
        else if (resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        }
        else if (resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        }
        else if (resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        }
        else if (resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        }
        else if (resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        }
        else if (resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        }
        else if (resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        }
        else if (resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
        }
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Candles.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        _mService = IInAppBillingService.Stub.asInterface(service);
        if (_mService != null) {
            if (Tools.CDDEBUG) {
                Log.d(TAG, "onServiceConnected: connected !");
                LogInFile.getInstance().WriteLog(TAG + " onServiceConnected: connected !", true);
                downloadAvailablesPurchasesFromGooglePlay();
                downloadMyPurchasesFromGooglePlay();
            }
        }
        else {
            if (Tools.CDDEBUG) {
                Log.d(TAG, "onServiceConnected: error while connecting !");
                LogInFile.getInstance().WriteLog(TAG + " onServiceConnected: error while connecting !", true);
            }
        }

    }

    private void downloadMyPurchasesFromGooglePlay() {
        _thread2 = new Thread(new Runnable() {
            public void run() {
                try {
                    Message myMsg = handler2.obtainMessage();
                    Bundle ownedItems = _mService.getPurchases(3, getPackageName(), "inapp", null);
                    myMsg.setData(ownedItems);
                    handler2.sendMessage(myMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        _thread2.start();
    }

    private void downloadAvailablesPurchasesFromGooglePlay() {
        Thread _mBackground = new Thread(new Runnable() {
            public void run() {
                try {
                    ArrayList<String> skuList = new ArrayList<String>();
                    skuList.add("seven_candles");
                    skuList.add("fifteen_candles");
                    skuList.add("thirty_candles");
                    Bundle querySkus = new Bundle();
                    querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
                    Message myMsg = handler1.obtainMessage();
                    Bundle skuDetails = _mService.getSkuDetails(3, getPackageName(), "inapp", querySkus);
                    myMsg.setData(skuDetails);
                    handler1.sendMessage(myMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        _mBackground.start();
    }

    Handler handler1 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle skuDetails = msg.getData();
            if (skuDetails != null) {
                int response = skuDetails.getInt("RESPONSE_CODE");
                if (Tools.CDDEBUG)
                    Log.d(TAG, "response: " + response);
                if (response == 0) {
                    ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                    if (responseList != null) {
                        for (String thisResponse : responseList) {
                            try {
                                JSONObject object = new JSONObject(thisResponse);
                                if (Tools.CDDEBUG)
                                    Log.d(TAG, "handleMessage::" + object.toString());
                                displayButtonsOnClickListeners(object.getString("productId"), object.getString("description"), object.getString("price"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            else {
                Log.d(TAG, "error with Bundle !");
            }
            if (_thread1 != null) {
                _thread1.interrupt();
                _thread1 = null;
            }
        }
    };

    Handler handler2 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle ownedItems = msg.getData();
            if (ownedItems != null) {
                int response = ownedItems.getInt("RESPONSE_CODE");
                if (response == 0) {
                    ArrayList ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE");
                    String continuationToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");
                    if (purchaseDataList != null) {
                        if (Tools.CDDEBUG)
                            Log.d(TAG, "handleMessage2::purchaseDataList size : " + purchaseDataList.size());
                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = (String)purchaseDataList.get(i);

                            if (signatureList != null) {
                                String signature = (String)signatureList.get(i);
                            }
                            if (ownedSkus != null && purchaseData != null) {
                                String sku = (String)ownedSkus.get(i);
                                Log.d(TAG, "Purchase data: " + purchaseData + ", sku : " + sku);
                                try {
                                    JSONObject purchaseObj = new JSONObject(purchaseData);
                                    consumePurchase(purchaseObj.getString("purchaseToken"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "handleMessage2::purchaseDataList null.");
                    }
                }
            }
            if (_thread2 != null) {
                _thread2.interrupt();
                _thread2 = null;
            }
        }
    };

    private void displayButtonsOnClickListeners(final String productId, String description, String price) {
        LinearLayout candlesLL = (LinearLayout)findViewById(R.id.linearLayout_candles_com_account_candles);
        Button candleBtn = new Button(this);
        candleBtn.setTextSize(15);
        candleBtn.setText(description + " (" + price + ")");
        candleBtn.setTextColor(getResources().getColor(R.color.black));
        candlesLL.addView(candleBtn);
        candleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyCandles(productId);
            }
        });
    }

    private void buyCandles(final String productId) {
        _thread3 = new Thread(new Runnable() {
            public void run() {
                try {
                    Bundle buyIntentBundle = _mService.getBuyIntent(3, getPackageName(), productId, "inapp", getDeveloperPayload());
                    if (Tools.CDDEBUG) Log.d(TAG, "buyIntentBundle: " + String.valueOf(buyIntentBundle.getInt("RESPONSE_CODE")));
                    if (buyIntentBundle.getInt("RESPONSE_CODE") == 0) {
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        Log.d(TAG, "pendingIntent: " + String.valueOf(buyIntentBundle.getInt("RESPONSE_CODE")));
                        if (buyIntentBundle.getInt("RESPONSE_CODE") == 0 && pendingIntent != null) {
                            startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                        }
                        else {
                            if (Tools.CDDEBUG) Log.e(TAG, "buyCandles::pendingIntent null or RESPONSE_CODE::" + buyIntentBundle.getInt("RESPONSE_CODE"));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        _thread3.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Tools.CDDEBUG)
            Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (Tools.CDDEBUG) {
                Log.d(TAG, "responseCode::" + responseCode);
                Log.d(TAG, "purchaseData::" + purchaseData);
                Log.d(TAG, "dataSignature::" + dataSignature);
                Log.d(TAG, "resultCode::" + resultCode);
            }
            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    if (Tools.CDDEBUG) {
                        Log.d(TAG, "Purchased: " + jo.toString());
                        LogInFile.getInstance().WriteLog(TAG + " Purchased : " + jo.toString(), true);
                    }
                    String sku = jo.getString("productId");
                    /*consumePurchase(jo.getString("purchaseToken"));
                    if (Tools.CDDEBUG)
                        Toast.makeText(this, TAG + "Vous venez d'acheter " + sku, Toast.LENGTH_SHORT).show();
                        */
                    callApiForUpdateAccount(sku, jo.getString("purchaseToken"));
                }
                catch (JSONException e) {
                    if (Tools.CDDEBUG) {
                        LogInFile.getInstance().WriteLog(TAG + " Failed to parse purchase data", true);
                        Log.d(TAG, "Failed to parse purchase data");
                    }
                    e.printStackTrace();
                }
            }
            if (_thread3 != null) {
                _thread3.interrupt();
                _thread3 = null;
            }
        }
    }

    private void callApiForUpdateAccount(String productId, String token) {
        List<NameValuePair> args = new ArrayList<NameValuePair>();
        String apiCall;
        int type;
        args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        args.add(new BasicNameValuePair("product", productId));
        args.add(new BasicNameValuePair("token", token));
        if (isCandle(productId)) {
            apiCall = Tools.CDACCOUNTCANDLEBUY;
            type = 0;
        }
        else {
            apiCall = Tools.CDACCOUNTPREMIUMBUY;
            type = 1;
        }
        if (Tools.CDDEBUG) Log.d(TAG, "CALL API : " + Tools.API + apiCall + ", product : " + productId + ", token: " + token);
        HttpApiCall apiCaller = new HttpApiCall(this, args, type);
        apiCaller.execute(Tools.API + apiCall);
    }

    private boolean isCandle(String productId) {
        for (int i = 0; i < Tools.CDCANDLEBUYS.size(); ++i) {
            if (Tools.CDCANDLEBUYS.get(i).equals(productId))
                return true;
        }
        return false;
    }

    private void consumePurchase(final String purchaseToken) {
        final Thread _mBackground = new Thread(new Runnable() {
            public void run() {
                try {
                    int response = _mService.consumePurchase(3, getPackageName(), purchaseToken);
                    Message myMessage = handler3.obtainMessage();
                    Bundle messageBundle = new Bundle();
                    messageBundle.putInt(String.valueOf(response), 1);
                    myMessage.setData(messageBundle);
                    handler3.sendMessage(myMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        _mBackground.start();
    }

    Handler handler3 = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.getData() != null) {
                int response = msg.getData().getInt("response");
                if (Tools.CDDEBUG) {
                    Log.d(TAG, "handler3:: response : " + response);
                    //Toast.makeText(getApplicationContext(), TAG + "Vous venez de consommer vos cierges ! ", Toast.LENGTH_SHORT).show();
                    LogInFile.getInstance().WriteLog(TAG + " handler3:: response : " + response, true);
                }
            }
            else {
                if (Tools.CDDEBUG) {
                    Log.e(TAG, "handler3, getData null");
                    LogInFile.getInstance().WriteLog(TAG + " handler3, getData null", true);
                }
            }
        }
    };

    public String getDeveloperPayload() throws NoSuchAlgorithmException {
        String password = UserConnected.getInstance().get_uid();
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(password.getBytes());
        byte byteData[] = md.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++)
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        return sb.toString();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        _mService = null;
        Log.d(TAG, "onServiceDisconnected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(this);
        Log.d(TAG, "onDestroy::service unbind !");
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        Log.d(TAG, "Result of buy: -> " + result);
        JSONObject resObj = new JSONObject(result);
        if (type == 0 && resObj.has("ok") && resObj.getString("ok").equals("1")) {
            consumePurchase(resObj.getString("token"));
            getCandlesFromApi();
        }
        else if (type == 2) {
            if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                UserConnected.getInstance().set_num_candles(resObj.getInt("num_available"));
                if (Tools.CDDEBUG) {
                    LogInFile.getInstance().WriteLog("Prayer::cierges restants: " + resObj.getString("num_available"), true);
                    Log.d("Prayer::cierges restants: ", resObj.getString("num_available"));
                }
                finish();
            }
        }
    }

    private void getCandlesFromApi() {
        List<NameValuePair> args = new ArrayList<NameValuePair>();
        args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        HttpApiCall _apiCaller = new HttpApiCall(this, args, 2);
        _apiCaller.execute(Tools.API + Tools.CDACCOUNTCANDLEGET);
    }
}