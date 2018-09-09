package com.prayers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Display;
import android.view.FocusFinder;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.LogInFile;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.comments.AddComment;
import com.messages.DisplayMessages;
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Prayer extends Activity implements ApiCaller {

    private int _resId = -1;
    private final String TAG = "Prayer";

    private ProgressDialog _ProgressDialog = null;
    private String _prayer = null;
    private ArrayList<NameValuePair> _args = new ArrayList<>();
    private HttpApiCall _apiCaller = null;
    private boolean _pried = false;
    private JSONObject prayerObj = null;
    private boolean changes = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.com_prayers_prayer);
        _ProgressDialog = new ProgressDialog(this);
        _ProgressDialog.setMessage(getString(R.string.ChargementEnCours));
        _ProgressDialog.show();
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
            _prayer = extras.getString("prayer");
            try {
                callApiForPrayer(new JSONObject(_prayer));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void callApiForPrayer(JSONObject prayerObj) {
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        try {
            _args.add(new BasicNameValuePair("id", prayerObj.getString("id")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (NameValuePair n : _args) {
            Log.d(TAG, n.getName() + " - " + n.getValue());
        }
        //_apiCaller = new HttpApiCall(this, _args, 1);
        //_apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSGET);


        PoolRequetes.getInstance().ajouterNouvelleRequete(this, _args, Tools.API + Tools.CDCLASSIFIEDSGET, 1);
    }

    public void displayPrayerInfos(String result, int type) throws Exception {
        if (Tools.CDDEBUG)
            Log.d(TAG, "displayPrayerInfos::type::" + type);
        if (type == 1) {
            JSONObject resObj = new JSONObject(result);
            if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                displayPrayerInfos(resObj);
            }
        }
        else if (type == 3 || type == 4) {
            if (Tools.CDDEBUG)
                Log.d(TAG, "displayPrayerInfos::" + _prayer);
            if (type == 3) _pried = true;
            callApiForPrayer(new JSONObject(_prayer));
        }
    }

    private void displayPrayerInfos(final JSONObject prayerObj) {
        TextView authorTV = (TextView)findViewById(R.id.textView_profilename_com_prayers_prayer);
        ImageView authorIV = (ImageView)findViewById(R.id.imageView_profilepic_com_prayers_prayer);
        TextView dateTimeTV = (TextView)findViewById(R.id.textView_datetime_com_prayers_prayer);
        TextView prayerTitleTV = (TextView)findViewById(R.id.textView_prayer_title_com_prayers_prayer);
        TextView prayerTextTV = (TextView)findViewById(R.id.textView_prayer_text_com_prayers_prayer);

        Button commentBtn = (Button)findViewById(R.id.button_add_comment_com_prayers_prayer);
        try {
            if (prayerObj.has("image") && prayerObj.getString("image").length() > 0)
                displayPrayerImg(prayerObj.getString("image"));
            setNumPriedAndCierges(prayerObj);
            setPriedAndCandleOnClickListener(prayerObj);
            authorTV.setText(prayerObj.getString("accountname"));


            // On récupère l'image de l'auteur de la prière
            setAuthorImage(authorIV, prayerObj.getString("accountimage"));
            authorIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profileIntent = new Intent(Prayer.this, MyProfile.class);
                    profileIntent.putExtra("resId", _resId);
                    try {
                        profileIntent.putExtra("profileId", prayerObj.getString("accountid"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(profileIntent);
                }
            });
            dateTimeTV.setText(prayerObj.getString("dateinfo"));
            prayerTitleTV.setText(prayerObj.getString("title"));
            if (!prayerObj.getString("text").equals("null"))
                prayerTextTV.setText(prayerObj.getString("text"));
            if (prayerObj.getString("allow_comments").equals("1")) {
                commentBtn.setVisibility(View.VISIBLE);
                commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            commentPrayer(prayerObj.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            else {
                commentBtn.setVisibility(View.GONE);
            }
            if (prayerObj.getString("can_delete").equals("1")) {
                Button deletePrayerBtn = (Button)findViewById(R.id.button_delete_prayer_com_prayers_prayer);
                deletePrayerBtn.setVisibility(View.VISIBLE);
                deletePrayerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            deletePrayer(prayerObj.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (_ProgressDialog != null)
            _ProgressDialog.cancel();
    }

    /**
     * Affichage de l'image de la prière
     *
     * @param imgUrl
     */
    private void displayPrayerImg(String imgUrl) {
        ImageView prayerIV = (ImageView)findViewById(R.id.imageView_default_photo_com_prayers_prayer);
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + Tools.STD_W / 3);
        if (cachedImage == null) {
            new DownloadImages(prayerIV, true, imgUrl + Tools.STD_W / 3).execute(Tools.MEDIAROOT + imgUrl, String.valueOf(Tools.STD_W / 3));
        }
        else {
            prayerIV.setImageBitmap(cachedImage);
            prayerIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void displayError(String err) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(err);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.cancel();
            }
        });
        alertDialog.show();
    }

    private void setPriedAndCandleOnClickListener(final JSONObject prayerObj) throws JSONException {
        Button candleBtn = (Button)findViewById(R.id.button_candleon_com_prayers_prayer);
        candleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().get_num_candles() <= 0) {
                    displayError(getString(R.string.PRAYER_MESSAGE_CANDLE_0));
                }
                else {
                    new AlertDialog.Builder(Prayer.this)
                            .setTitle(R.string.PRAYER_MESSAGE_CANDLE)
                            .setMessage(getString(R.string.PRAYER_MESSAGE_CANDLE_S, UserConnected.getInstance().get_num_candles()))
                            .setNegativeButton(R.string.CANCEL,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.cancel();
                                        }
                                    })
                            .setPositiveButton(R.string.PRAYER_MESSAGE_CANDLE_OK,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            try {
                                                callApiAndBuyCandle(prayerObj);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }).create().show();
                }
            }
        });
        Button priedBtn = (Button)findViewById(R.id.button_pray_com_prayers_prayer);
        if (_pried) {
            priedBtn.setText(getString(R.string.PRAYER_BUTTON_PRAYED));
            float alpha = 0.45f;
            AlphaAnimation alphaUp = new AlphaAnimation(alpha, alpha);
            alphaUp.setFillAfter(true);
            priedBtn.startAnimation(alphaUp);
        }
        else {
            priedBtn.setText(getString(R.string.PRAYER_BUTTON_PRAY));
        }
        priedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _args.clear();
                _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                try {
                    _args.add(new BasicNameValuePair("id", prayerObj.getString("id")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                _apiCaller = new HttpApiCall(Prayer.this, _args, 3);
                _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSPRAYED);
            }
        });
    }

    private void callApiAndBuyCandle(JSONObject prayerObj) throws JSONException {

        this.prayerObj = prayerObj;

        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", prayerObj.getString("id")));

        Log.d("callApiAndBuyCandle", "uid::" + UserConnected.getInstance().get_uid() + " - sid:: " + UserConnected.getInstance().get_sid() + " - id::" + prayerObj.getString("id"));

        _apiCaller = new HttpApiCall(Prayer.this, _args, 4);
        _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSCANDLE);
    }

    private void setNumPriedAndCierges(JSONObject prayerObj) throws JSONException {
        ImageView candleIV = (ImageView)findViewById(R.id.imageView_candle_com_prayers_prayers);
        if ((prayerObj.getInt("now") >= prayerObj.getInt("candlebegin")) && (prayerObj.getInt("now") <= prayerObj.getInt("candleend"))) {
            candleIV.setImageResource(R.drawable.candle_on2);
        }
        else {
            candleIV.setImageResource(R.drawable.candle_off);
        }
        TextView numPriedTV = (TextView)findViewById(R.id.textView_numpried_com_prayers_prayer);
        if (prayerObj.getInt("num_prayed") > 0) {
            numPriedTV.setVisibility(View.VISIBLE);
            numPriedTV.setText(prayerObj.getString("num_prayed") + " " + getString(R.string.PRAYER_PRAYED));
        }
        else {
            numPriedTV.setVisibility(View.GONE);
        }
        TextView numCandlesTV = (TextView)findViewById(R.id.textView_numcierges_com_prayers_prayer);
        if (prayerObj.getInt("num_candles") > 0) {
            numCandlesTV.setVisibility(View.VISIBLE);
            numCandlesTV.setText(prayerObj.getString("num_candles") + " " + getString(R.string.PRAYER_CANDLE));
        }
        else {
            numCandlesTV.setVisibility(View.GONE);
        }
        Button commentBtn = (Button)findViewById(R.id.button_add_comment_com_prayers_prayer);
        if (prayerObj.getInt("num_comments") > 0) {
            commentBtn.setText(getString(R.string.PRAYER_0_COMMENT) + " " + prayerObj.getString("num_comments"));
        }
    }

    private void deletePrayer(final String id) {
        AlertDialog.Builder deleteAD = new AlertDialog.Builder(Prayer.this);
        deleteAD.setTitle(getString(R.string.PRAYER_DELETE));
        deleteAD.setMessage(getString(R.string.PRAYER_DELETE_CONFIRM));
        deleteAD.setCancelable(false);
        deleteAD.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        deleteAD.setPositiveButton(getString(R.string.DELETE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendDeleteAction(id);
            }
        });
        deleteAD.create().show();
    }

    private void sendDeleteAction(String id) {
        if (Tools.CDDEBUG)
            Log.d("Prayer::sendDeleteAction::", "deleting prayer with id::" + id);
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", id));
        _args.add(new BasicNameValuePair("prayer", "1"));
        _apiCaller = new HttpApiCall(this, _args, 2);
        _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSDELETE);
    }


    /**
     * Fonction appelée après la suppression d'une prière
     *
     * @param result
     */
    public void deletedPrayer(String result) {
        if (result != null) {
            try {
                JSONObject resObj = new JSONObject(result);
                if (!resObj.has("ok") || !resObj.getString("ok").equals("1")) {
                    if (Tools.CDDEBUG)
                        Log.d("Prayer::deletedPrayer::", "Error while deleting prayer");
                    //TODO message erreur
                }
                else {
                    Log.d("Prayer::deletedPrayer::", "prayer deleted !");
                    Intent myIntent = getIntent();
                    myIntent.putExtra("changes", changes);
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void commentPrayer(String id) {
        Intent addComment = new Intent(Prayer.this, AddComment.class);
        addComment.putExtra("resId", _resId);
        addComment.putExtra("item_id", id);
        addComment.putExtra("item_type", "CLASSIFIED");
        addComment.putExtra("type", "priere");
        startActivityForResult(addComment, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            try {
                callApiForPrayer(new JSONObject(_prayer));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAuthorImage(ImageView authorIV, String accountimageURL) {
        if (accountimageURL != null && !accountimageURL.equals("")) {
            int size = Tools.STD_W / 7;
            LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
            authorIV.setLayoutParams(profileLP);
            //new DownloadImageTask(authorIV, true).execute(Tools.MEDIAROOT + accountimageURL);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(accountimageURL + size);
            if (cachedImage == null && internetConnectionOk()) {
                //new DownloadImages(authorIV, true, accountimageURL + size).execute(Tools.MEDIAROOT + accountimageURL, String.valueOf(size));

                PoolRequetes.getInstance().ajouterNouvelleRequeteImage(authorIV, true, accountimageURL, size);

            }
            else {
                authorIV.setImageBitmap(cachedImage);
                authorIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenWidth() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = getWindowManager().getDefaultDisplay();
        int width;
        if (apiLevel >= 13) {
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }
        else
            width = display.getWidth();
        return width;
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Prayer.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {

        changes = true;
        if (type == 1 || type == 3 || type == 4) {
            displayPrayerInfos(result, type);
            if (type == 4) {
                //TODO resoudre erreur
                Toast.makeText(this, "Votre cierge a bien été allumé...", Toast.LENGTH_SHORT).show();
                getCandlesFromApi();
                downloadPrayer();
            }
        }
        else if (type == 2) {
            deletedPrayer(result);
        }
        else if (type == 5) {
            JSONObject resObj = new JSONObject(result);
            if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                UserConnected.getInstance().set_num_candles(resObj.getInt("num_available"));
                if (Tools.CDDEBUG) {
                    LogInFile.getInstance().WriteLog("Prayer::cierges restants: " + resObj.getString("num_available"), true);
                    Log.d("Prayer::cierges restants: ", resObj.getString("num_available"));
                }
            }
        }
    }

    /**
     * Retélécharge la prière
     */
    private void downloadPrayer() {
        Log.d("downloadPrayer", "calling API...");
        callApiForPrayer(prayerObj);
    }

    private void getCandlesFromApi() {
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _apiCaller = new HttpApiCall(this, _args, 5);
        _apiCaller.execute(Tools.API + Tools.CDACCOUNTCANDLEGET);
    }


    @Override
    public void onBackPressed() {


        Intent myIntent = getIntent();
        myIntent.putExtra("changes", changes);
        setResult(RESULT_OK, myIntent);
        finish();
    }
}
