package com.ads;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.account.Premium;
import com.i2heaven.carpedeum.R;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 01/11/13.
 * Recupere les pubs et les gère
 */

public class ManageAds {

    private static final ManageAds _instance = new ManageAds();
    private JSONArray _ads = null;
    private static String TAG = "ManageAds";

    public static ManageAds getInstance() {
        return _instance;
    }

    public void init() {
        getAdsFromApi();
    }

    private void getAdsFromApi() {
        /*if (Tools.CDDEBUG)
            Log.d("ManageAds::", "getAdsFromApi");*/
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        if (UserConnected.getInstance().IsUserConnected()) {
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        }
        _args.add(new BasicNameValuePair("viewed", "1"));
        _args.add(new BasicNameValuePair("clicked", ""));
        HttpApiCall apiCaller = new HttpApiCall(this, _args);
        String url = Tools.API + Tools.CDADSGET;
        apiCaller.execute(url);
    }

    public void onApiResult(String result) throws Exception {
        if (result != null) {
            /*if (Tools.CDDEBUG)
                Log.d("ManageAds::onApiResult::", result);*/
            _ads = new JSONArray(new JSONObject(result).getString("results"));
        }
    }

    public JSONObject getAdd() {
        if (_ads != null) {
            try {
                return _ads.getJSONObject((int)(Math.random() * (_ads.length())));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void displayAdds(final Activity instance, int size, boolean shadow, int id, int screenSize) throws Exception {
        final JSONObject ads = ManageAds.getInstance().getAdd();
        ImageView addIV = (ImageView)instance.findViewById(R.id.adds);
        if (UserConnected.getInstance().IsUserConnected() && UserConnected.getInstance().is_premium()) {
            addIV.setVisibility(View.GONE);
            ImageView shadowView = (ImageView)instance.findViewById(R.id.bottomshadow);
            shadowView.setVisibility(View.GONE);
            ScrollView scroolV = (ScrollView)instance.findViewById(R.id.scrollView);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)scroolV.getLayoutParams();
            assert params != null;
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            if (shadow) params.addRule(RelativeLayout.BELOW, id);
            scroolV.setLayoutParams(params);
        }
        else {
            try {
                Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(ads.getString("image") + size);
                if (cachedImage == null) {
                    if (screenSize > Tools.REF_SCREEN_WIDTH)
                        new DownloadImages(addIV, false, ads.getString("image") + size).execute(ads.getString("image"), String.valueOf(size), "50");
                    else
                        new DownloadImages(addIV, true, ads.getString("image") + size).execute(ads.getString("image"), String.valueOf(size), "50");
                } else {
                    addIV.setImageBitmap(cachedImage);
                    addIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
                addIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (!ads.getString("title").equals("Carpe Deum")) {
                                Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                                openPageIntent.setData(Uri.parse(ads.getString("url")));
                                instance.startActivity(openPageIntent);
                            }
                            else {
                                if (UserConnected.getInstance().IsUserConnected()) {
                                    Intent premiumIntent = new Intent(instance, Premium.class);
                                    premiumIntent.putExtra("resId", Tools.RESID);
                                    instance.startActivity(premiumIntent);
                                }
                                else {
                                    //TODO page login
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {

            }
        }
    }
}
