package com.together;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.carpedeum.MainActivity;
import com.i2heaven.carpedeum.R;
import com.classifieds.Classified;
import com.classifieds.Classifieds;
import com.classifieds.CreateClassified;
import com.prayers.CreatePrayer;
import com.prayers.MyPrayers;
import com.prayers.Prayer;
import com.status.DisplayStatus;
import com.user.Parvis;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 27/08/13.
 * Onglet ensemble
 */

public class Together implements ApiCaller {

    MainActivity _mainAcInstance = null;
    private int _resId = -1;
    public ProgressDialog _progressDialog = null;
    private int _drawableArrowID = -1;
    private static int CREATEPRAYERFROMTOGETHER = 31;
    private static int CREATECLASSIFIEDFROMTOGETHER = 32;

    public Together(MainActivity instance, int resId) {
        _mainAcInstance = instance;
        _resId = resId;
        getLayoutTheme();
        _progressDialog = new ProgressDialog(_mainAcInstance);
        _progressDialog.setMessage(_mainAcInstance.getString(R.string.ChargementEnCours));
        setButtonsOnClickListeners();
    }

    /**
     * Listeners des boutons
     */
    private void setButtonsOnClickListeners() {
        Button createPrayerBtn = (Button)_mainAcInstance.findViewById(R.id.button_create_prayer_com_together);
        createPrayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPrayerIntent = new Intent(_mainAcInstance, CreatePrayer.class);
                createPrayerIntent.putExtra("resId", _resId);
                _mainAcInstance.startActivityForResult(createPrayerIntent, CREATEPRAYERFROMTOGETHER);
            }
        });
        Button createClassifiedBtn = (Button)_mainAcInstance.findViewById(R.id.button_display_create_classified_com_together);
        createClassifiedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createPrayerIntent = new Intent(_mainAcInstance, CreateClassified.class);
                createPrayerIntent.putExtra("resId", _resId);
                _mainAcInstance.startActivityForResult(createPrayerIntent, CREATECLASSIFIEDFROMTOGETHER);
            }
        });
        Button displayAllPrayerBtn = (Button)_mainAcInstance.findViewById(R.id.button_display_all_prayers_com_together);
        displayAllPrayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allPrayersIntent = new Intent(_mainAcInstance, MyPrayers.class);
                allPrayersIntent.putExtra("resId", _resId);
                allPrayersIntent.putExtra("caller", "together");
                _mainAcInstance.startActivity(allPrayersIntent);
            }
        });
        TextView connectedUsersTV = (TextView)_mainAcInstance.findViewById(R.id.textView_connected_com_together);
        connectedUsersTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent parvisIntent = new Intent(_mainAcInstance, Parvis.class);
                parvisIntent.putExtra("resId", _resId);
                _mainAcInstance.startActivity(parvisIntent);
            }
        });
        TextView classifiedsTV = (TextView)_mainAcInstance.findViewById(R.id.textView_classifieds_com_together);
        classifiedsTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent classifiedsIntent = new Intent(_mainAcInstance, Classifieds.class);
                classifiedsIntent.putExtra("resId", _resId);
                _mainAcInstance.startActivity(classifiedsIntent);
            }
        });
        Button allClassifiedsBtn = (Button)_mainAcInstance.findViewById(R.id.button_display_all_classifieds_com_together);
        allClassifiedsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent classifiedsIntent = new Intent(_mainAcInstance, Classifieds.class);
                classifiedsIntent.putExtra("resId", _resId);
                _mainAcInstance.startActivity(classifiedsIntent);
            }
        });
        RelativeLayout prayersRL = (RelativeLayout)_mainAcInstance.findViewById(R.id.relativeLayout_prayers_com_together);
        prayersRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent allPrayersIntent = new Intent(_mainAcInstance, MyPrayers.class);
                allPrayersIntent.putExtra("resId", _resId);
                allPrayersIntent.putExtra("caller", "together");
                _mainAcInstance.startActivity(allPrayersIntent);
            }
        });
    }

    /**
     * Télécharger les informations de l'onglet ensemble
     */
    public void displayTogetherInfos() {
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));

        HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
        String url = Tools.API + Tools.HOME;
        apiCaller.execute(url);
    }

    private void displayPrayers(JSONArray prayersArray) throws Exception {
        LinearLayout allPrayersLL = (LinearLayout)_mainAcInstance.findViewById(R.id.linearLayout_allprayers_com_togethers);
        allPrayersLL.removeAllViews();

        for (int i = 0; i < prayersArray.length(); ++i) {
            final JSONObject prayerObj = prayersArray.getJSONObject(i);
            LinearLayout prayerLL = new LinearLayout(_mainAcInstance);
            prayerLL.setBackgroundColor(_mainAcInstance.getResources().getColor(R.color.white));
            prayerLL.setOrientation(LinearLayout.HORIZONTAL);
            String imgURL = prayerObj.getString("image");
            if (imgURL.length() == 0)
                imgURL = prayerObj.getString("profilepic");
            ImageView prayerIV = getImage(imgURL, 5);
            if (prayerIV != null) {
                prayerLL.addView(prayerIV);
            }
            TextView prayerInfosTV = new TextView(_mainAcInstance);
            String text = "<small><font color='#737373'>" + prayerObj.getString("dateinfo") + "</font></small><br/>";
            text += prayerObj.getString("title") + "<br/><br/>";
            text += "<small><font color='#737373'>" + prayerObj.getString("num_prayed") + " ";
            if (prayerObj.getInt("num_prayed") > 1)
                text += _mainAcInstance.getString(R.string.PRAYED_S);
            else
                text += _mainAcInstance.getString(R.string.PRAYED);
            text += " - " + prayerObj.getString("num_candles") + " ";
            if (prayerObj.getInt("num_candles") > 1)
                text += _mainAcInstance.getString(R.string.CANDLE_S);
            else
                text += _mainAcInstance.getString(R.string.CANDLE);
            text += "</font></small>";
            prayerInfosTV.setTextSize(15);
            prayerInfosTV.setText(Html.fromHtml(text));
            LinearLayout.LayoutParams prayerInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
            prayerInfosTV.setTextColor(_mainAcInstance.getResources().getColor(R.color.black));
            prayerInfosLP.setMargins(5, 0, 0, 0);
            prayerLL.addView(prayerInfosTV, prayerInfosLP);
            ImageView candleIV = getCandleImage(prayerObj.getString("candle_burning"));

            LinearLayout.LayoutParams candleInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
            candleInfosLP.gravity = Gravity.CENTER;

            prayerLL.addView(candleIV, candleInfosLP);
            prayerLL.setPadding(5, 5, 5, 5);
            prayerLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent prayerIntent = new Intent(_mainAcInstance, Prayer.class);
                    prayerIntent.putExtra("resId", _resId);
                    prayerIntent.putExtra("prayer", prayerObj.toString());
                    //TODO recupération du result lors de la suppression ?
                    _mainAcInstance.startActivity(prayerIntent);
                }
            });
            allPrayersLL.addView(prayerLL);

            LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            View separatorView = new View(_mainAcInstance);
            separatorView.setBackgroundResource(R.color.gris73);

            allPrayersLL.addView(separatorView, layoutparamsSeparator);
        }

    }

    private ImageView getCandleImage(String candle_burning) {
        ImageView candleIV = new ImageView(_mainAcInstance);
        if (candle_burning.equals("0"))
            candleIV.setImageResource(R.drawable.list_candle_off);
        else
            candleIV.setImageResource(R.drawable.list_candle_on);
        return candleIV;
    }

    private void displayContactsActivities(JSONArray resultArray) throws Exception {
        LinearLayout allActivitiesLL = (LinearLayout)_mainAcInstance.findViewById(R.id.linearLayout_contacts_activities_com_togethers);
        allActivitiesLL.removeAllViews();
        for (int i = 0; i < resultArray.length(); ++i) {
            final JSONObject activityObj = resultArray.getJSONObject(i);
            LinearLayout activityLL = new LinearLayout(_mainAcInstance);
            activityLL.setBackgroundColor(_mainAcInstance.getResources().getColor(R.color.white));
            activityLL.setOrientation(LinearLayout.HORIZONTAL);
            ImageView profileIV = getImage(activityObj.getString("profilepic"), 6);
            if (profileIV != null) {
                //profileIV.setPadding(0, 0, 5, 0);
                activityLL.addView(profileIV);
            }
            TextView infosTV = new TextView(_mainAcInstance);
            String moreInfos = "";
            if (activityObj.getString("type").equals("posted-classified")) {
                moreInfos = "Annonce de ";
            }
            else if (activityObj.getString("type").equals("posted-prayer")) {
                moreInfos = _mainAcInstance.getString(R.string.PRAYER_BY);
            }
            String text = "<small><font color='#737373'>" + moreInfos + "</font>" + activityObj.getString("profilename") + "<font color='#737373'>, " + activityObj.getString("dateinfo") + "</font></small><br/>";
            text += activityObj.getString("text");
            infosTV.setTextSize(15);
            infosTV.setText(Html.fromHtml(text));
            infosTV.setTextColor(_mainAcInstance.getResources().getColor(R.color.black));
            LinearLayout.LayoutParams infosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
            infosLP.setMargins(5, 0, 0, 0);
            activityLL.addView(infosTV, infosLP);
            activityLL.setPadding(5, 5, 5, 5);
            activityLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent activity;
                    try {
                        if (activityObj.getString("type").equals("posted-status")) {
                            activity = new Intent(_mainAcInstance, DisplayStatus.class);
                            activity.putExtra("resId", _resId);
                            activity.putExtra("statusId", activityObj.getString("id"));
                            _mainAcInstance.startActivity(activity);
                        }
                        else if (activityObj.getString("type").equals("posted-prayer")) {
                            activity = new Intent(_mainAcInstance, Prayer.class);
                            activity.putExtra("resId", _resId);
                            activity.putExtra("prayer", activityObj.toString());
                            _mainAcInstance.startActivity(activity);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            allActivitiesLL.addView(activityLL);
            LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            View separatorView = new View(_mainAcInstance);
            separatorView.setBackgroundResource(R.color.gris73);
            allActivitiesLL.addView(separatorView, layoutparamsSeparator);
        }
    }

    private ImageView getImage(String imageURL, int divider) {
        ImageView profileIV = new ImageView(_mainAcInstance);
        int size = Tools.STD_W / divider;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (imageURL != null && imageURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imageURL + size);
            if (cachedImage == null) {
                new DownloadImages(profileIV, false, imageURL + size).execute(Tools.MEDIAROOT + imageURL, String.valueOf(size));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
            }
            profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        //profileIV.setMaxWidth(size);
        //profileIV.setMaxHeight(size);
        return profileIV;
    }



    private void getLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            _drawableArrowID = R.drawable.disclosure;
        }
        else if (_resId == 1) {
            _drawableArrowID = R.drawable.disclosure_blue;
        }
        else if (_resId == 2) {
            _drawableArrowID = R.drawable.disclosure_gold;
        }
        else if (_resId == 3) {
            _drawableArrowID = R.drawable.disclosure_green;
        }
        else if (_resId == 4) {
            _drawableArrowID = R.drawable.disclosure_mauve;
        }
        else if (_resId == 5) {
            _drawableArrowID = R.drawable.disclosure_orange;
        }
        else if (_resId == 6) {
            _drawableArrowID = R.drawable.disclosure_purple;
        }
        else if (_resId == 7) {
            _drawableArrowID = R.drawable.disclosure_red;
        }
        else if (_resId == 8) {
            _drawableArrowID = R.drawable.disclosure_silver;
        }
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)_mainAcInstance.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public int getScreenWidth() {
        int apiLevel = android.os.Build.VERSION.SDK_INT;
        Display display = _mainAcInstance.getWindowManager().getDefaultDisplay();
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

    /**
     * Résultat de l'appel à l'API de Carpe Deum
     *
     * @param result
     * @param type
     * @throws Exception
     */
    @Override
    public void onApiResult(String result, int type) throws Exception {

       if (type == 1) {

           if (_progressDialog != null)
               _progressDialog.cancel();
           if (result != null && !result.equals("")) {
               JSONObject resultObj = new JSONObject(result);
               if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                   displayStats(resultObj);
                   if (!resultObj.getString("list_contactupdates").equals("null")) {
                       TextView noUpdatesTV = (TextView)_mainAcInstance.findViewById(R.id.textView_no_updates_com_together);
                       noUpdatesTV.setVisibility(View.GONE);
                       displayContactsActivities(resultObj.getJSONArray("list_contactupdates"));
                   }
                   else {
                       TextView noUpdatesTV = (TextView)_mainAcInstance.findViewById(R.id.textView_no_updates_com_together);
                       noUpdatesTV.setVisibility(View.VISIBLE);
                   }
                   if (!resultObj.getString("list_prayers").equals("null")) {
                       TextView noPrayersTV = (TextView)_mainAcInstance.findViewById(R.id.textView_noprayers_com_together);
                       noPrayersTV.setVisibility(View.GONE);
                       displayPrayers(resultObj.getJSONArray("list_prayers"));
                   }
               }
               else {
                   //TODO message erreur
               }
           }
           else {
               //TODO message erreur
           }

       }

    }

    /**
     * Affichage du nombre de connectés, intentions, etc sur les pictos en haut de la page
     *
     * @param resultObj
     * @throws Exception
     */
    private void displayStats(JSONObject resultObj) throws Exception {
        TextView connectedTV = (TextView)_mainAcInstance.findViewById(R.id.textView_connected_com_together);
        TextView prayersTV = (TextView)_mainAcInstance.findViewById(R.id.textView_prayers_com_together);
        TextView classifiedsTV = (TextView)_mainAcInstance.findViewById(R.id.textView_classifieds_com_together);

        connectedTV.setText(resultObj.getString("count_online_users"));
        prayersTV.setText(resultObj.getString("count_total_prayers"));
        classifiedsTV.setText(resultObj.getString("count_total_classifieds"));
    }
}




























