package com.prayers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
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
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessages;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 28/08/13.
 *
 * Affichage des prières - Mes prières et les prières
 */

public class MyPrayers extends Activity implements ApiCaller {

    private int _resId = -1;
    private int _drawableArrowID = -1;
    private ProgressDialog _progressDialog = null;
    private boolean _changes = false;
    private String _caller = null;
    private String _userId = null;
    private int _selected = 0;
    private final String TAG = "MyPrayers";
    private int _first = 0;

    private ArrayList<NameValuePair> _args = null;
    private HttpApiCall _apiCaller = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_my_prayers);

        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        if (extras != null) {

            // Extraction des paramètres de l'intent
            _resId = extras.getInt("resId");
            _caller = extras.getString("caller");
            if (extras.containsKey("userId"))
                _userId = extras.getString("userId");

            // Définition de la couleur du thème
            setLayoutTheme();

            // On cache le logo carpedeum pour le remplacer par le titre de l'activité
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);

            resizePrayerMenu();

            // Définition du titre de l'activité
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            if ((_caller != null && _caller.equals("together")) || (_userId != null))
                titleTV.setText(getResources().getString(R.string.VIEW_TITLE_PRAYERS));
            else {
                LinearLayout menuLL = (LinearLayout)findViewById(R.id.linearLayout_com_prayers_menu);
                if (menuLL != null) menuLL.setVisibility(View.GONE);
                titleTV.setText(getResources().getString(R.string.VIEW_TITLE_MY_PRAYERS));
            }



        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (Tools.CDDEBUG)
            Log.d("MyPrayers::", "onResume");
        //LinearLayout prayersLL = (LinearLayout)findViewById(R.id.linearLayout_prayers_com_my_prayers);
        //prayersLL.removeAllViews();

        /*if (_first > 0) {
            int save = _first;
            _first = 0;

            Log.d("onResume::", "first : " + _first);
            loadPrayers();

            _first = save;
            Log.d("onResume::", "first : " + _first);
            loadPrayers();
        }
        else {
            loadPrayers();
        }*/
        loadPrayers();
    }


    private void loadPrayers() {
        setPrayersOnClickListeners();
        Button createPrayerBtn = (Button)findViewById(R.id.button_create_prayer_com_my_prayers);
        LinearLayout menuLL = (LinearLayout)findViewById(R.id.linearLayout_com_prayers_menu);
        if (_caller != null && _caller.equals("together")){
            createPrayerBtn.setVisibility(View.GONE);
            menuLL.setVisibility(View.VISIBLE);
            setMenuOnClickListeners();
            getAllPrayers();
        }
        else {
            menuLL.setVisibility(View.GONE);
            if (_userId != null) {
                createPrayerBtn.setVisibility(View.GONE);
            }
            else {
                createPrayerBtn.setVisibility(View.VISIBLE);
            }
            getMyPrayers();
        }
    }


    /**
     * Redimensionne le menu haut des prières en fonction de largeur de l'écran, la taille de l'écran, etc.
     */
    private void resizePrayerMenu() {

        LinearLayout prayersMenuLL = (LinearLayout)findViewById(R.id.linearLayout_menu_prayers_com_prayers_menu);
        if (Tools.getScreenInches(this) > Tools.STD_INCHES) {

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Tools.STD_W, 45);
            prayersMenuLL.setLayoutParams(lp);

        }

    }

    private void setMenuOnClickListeners() {
        final Button dateBtn = (Button)findViewById(R.id.button_date_com_prayers_menu);
        final Button distanceBtn = (Button)findViewById(R.id.button_distance_com_prayers_menu);
        final Button activeBtn = (Button)findViewById(R.id.button_active_com_prayers_menu);

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _selected = 1;
                _first = 0;
                setButtonBackgroundResources();
                getAllPrayers();
            }
        });
        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _selected = 2;
                _first = 0;
                setButtonBackgroundResources();
                getAllPrayers();
            }
        });
        activeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _selected = 3;
                _first = 0;
                setButtonBackgroundResources();
                getAllPrayers();
            }
        });
        TextView loadMoreTV = (TextView)findViewById(R.id.textView_loadmore_com_my_prayers);
        loadMoreTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _first += 25;
                getAllPrayers();
            }
        });
    }

    private void setButtonBackgroundResources() {
        final Button dateBtn = (Button)findViewById(R.id.button_date_com_prayers_menu);
        final Button distanceBtn = (Button)findViewById(R.id.button_distance_com_prayers_menu);
        final Button activeBtn = (Button)findViewById(R.id.button_active_com_prayers_menu);

        if (_selected == 1) {
            dateBtn.setBackgroundResource(R.drawable.segment_on_single);
            distanceBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            activeBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
            dateBtn.setTextColor(getResources().getColor(R.color.black));
            distanceBtn.setTextColor(getResources().getColor(R.color.white));
            activeBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 2) {
            dateBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            distanceBtn.setBackgroundResource(R.drawable.segment_on_single);
            activeBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);
            dateBtn.setTextColor(getResources().getColor(R.color.white));
            distanceBtn.setTextColor(getResources().getColor(R.color.black));
            activeBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 3) {
            dateBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            distanceBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            activeBtn.setBackgroundResource(R.drawable.segment_on_single);
            dateBtn.setTextColor(getResources().getColor(R.color.white));
            distanceBtn.setTextColor(getResources().getColor(R.color.white));
            activeBtn.setTextColor(getResources().getColor(R.color.black));
        }
    }


    /**
     * Appelle l'API pour télécharger la liste des prières
     */
    private void getAllPrayers() {
        _progressDialog.show();
        if (_args == null)
            _args = new ArrayList<>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("type", "pray"));
        if (_selected == 1)
            _args.add(new BasicNameValuePair("mode", "recent"));
        else if (_selected == 2)
            _args.add(new BasicNameValuePair("mode", "local"));
        else if (_selected == 3)
            _args.add(new BasicNameValuePair("mode", "popular"));
        _args.add(new BasicNameValuePair("limit", "25"));
        int type = 1;
        if (_first > 1) {
            _args.add(new BasicNameValuePair("first", String.valueOf(_first)));
            type = 2;
        }


        /*_apiCaller = new HttpApiCall(this, _args, type);
        _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSLIST);
        _apiCaller = null;
        */
        Log.d("getAllPrayers::type", "" + type);
        PoolRequetes.getInstance().ajouterNouvelleRequeteEnFin(this, _args, Tools.API + Tools.CDCLASSIFIEDSLIST, type);
    }



    /**
     * Affichage de la liste des prières
     *
     * @param prayersArray
     * @param type
     */
    private void displayPrayers(JSONArray prayersArray, int type) throws Exception {
        if (Tools.CDDEBUG)
            Log.d("MyPrayers::displayPrayers::lenght::", String.valueOf(prayersArray.length()) + " - type " + type);
        LinearLayout prayersLL = (LinearLayout)findViewById(R.id.linearLayout_prayers_com_my_prayers);
        if (type != 2) {
            prayersLL.removeAllViews();
        }
        for (int i = 0; i < prayersArray.length(); ++i) {


            final JSONObject prayerObj = prayersArray.getJSONObject(i);

            View prayerLayout = getLayoutInflater().inflate(R.layout.com_prayers_prayer_list, null);
            assert prayerLayout != null;


            // Image de la prière
            ImageView prayerIV = (ImageView)prayerLayout.findViewById(R.id.imageView_prayerimage_com_prayers_prayer_list);
            setPrayerProfileImage(prayerIV, prayerObj.getString("profilepic"), prayerObj.getString("image"));


            // Premier texte
            TextView fromDateTV = (TextView)prayerLayout.findViewById(R.id.textView_from_and_date_com_prayers_prayer_list);
            fromDateTV.setText(prayerObj.getString("dateinfo"));


            TextView nameTV = (TextView)prayerLayout.findViewById(R.id.textView_prayer_name_com_prayers_prayer_list);
            nameTV.setText(prayerObj.getString("title"));


            TextView statsTV = (TextView)prayerLayout.findViewById(R.id.textView_distance_stats_com_prayers_prayer_list);

            String text = "";

            if (prayerObj.getInt("num_prayed") > 0) {
                text = prayerObj.getString("num_prayed") + " ";
                if (prayerObj.getInt("num_prayed") > 1)
                    text += getString(R.string.PRAYED_S);
                else
                    text += getString(R.string.PRAYED);
            }

            if (prayerObj.getInt("num_candles") > 0) {
                text += " - " + prayerObj.getString("num_candles") + " ";
                if (prayerObj.getInt("num_candles") > 1)
                    text += getString(R.string.CANDLE_S);
                else
                    text += getString(R.string.CANDLE);
            }


            if (prayerObj.getInt("num_comments") > 0) {
                text += " - " + prayerObj.getString("num_comments") + " ";
                if (prayerObj.getInt("num_comments") > 1)
                    text += getString(R.string.COMMENTS_PRAYER);
                else
                    text += getString(R.string.COMMENT_PRAYER);


            }

            statsTV.setText(text);


            ImageView candleIV = (ImageView)prayerLayout.findViewById(R.id.imageView_candle_com_prayers_prayer_list);
            setCandleImage(candleIV, prayerObj.getString("candle_burning"));


            prayerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent prayerIntent = new Intent(MyPrayers.this, Prayer.class);
                    prayerIntent.putExtra("resId", _resId);
                    prayerIntent.putExtra("prayer", prayerObj.toString());
                    startActivityForResult(prayerIntent, 1);
                }
            });

            prayersLL.addView(prayerLayout);

            /*

            LinearLayout prayerLL = new LinearLayout(this);
            prayerLL.setBackgroundColor(getResources().getColor(R.color.white));


            LinearLayout.LayoutParams profileImageLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20);
            prayerLL.setOrientation(LinearLayout.HORIZONTAL);
            //Log.d(TAG, "CLASSIFIED : " + prayerObj.toString());


            // Obtention de l'image associée à la prière
            ImageView prayerIV = getPrayerProfileImage(prayerObj.getString("profilepic"), prayerObj.getString("image"));
            if (prayerIV != null) {
                prayerLL.addView(prayerIV, profileImageLP);
            }


            TextView prayerInfosTV = new TextView(this);
            String text = getName(prayerObj) + "<small><font color='#737373'>" + prayerObj.getString("dateinfo") + "</font></small><br/>";
            text += prayerObj.getString("title") + "<br/><br/>";
            text += "<small><font color='#737373'>" + prayerObj.getString("num_prayed") + " ";
            if (prayerObj.getInt("num_prayed") > 1)
                text += getString(R.string.PRAYED_S);
            else
                text += getString(R.string.PRAYED);
            text += " - " + prayerObj.getString("num_candles") + " ";
            if (prayerObj.getInt("num_candles") > 1)
                text += getString(R.string.CANDLE_S);
            else
                text += getString(R.string.CANDLE);
            text += " - " + prayerObj.getString("num_comments") + " ";
            if (prayerObj.getInt("num_comments") > 1)
                text += getString(R.string.COMMENTS_PRAYER);
            else
                text += getString(R.string.COMMENT_PRAYER);
            text += "</font></small>";
            prayerInfosTV.setTextSize(15);
            prayerInfosTV.setText(Html.fromHtml(text));
            LinearLayout.LayoutParams prayerInfosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
            prayerInfosTV.setTextColor(getResources().getColor(R.color.black));
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
                    Intent prayerIntent = new Intent(MyPrayers.this, Prayer.class);
                    prayerIntent.putExtra("resId", _resId);
                    prayerIntent.putExtra("prayer", prayerObj.toString());
                    startActivityForResult(prayerIntent, 1);
                }
            });
            prayersLL.addView(prayerLL);


            */

            LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
            layoutparamsSeparator.setMargins(0, 5, 0, 5);
            View separatorView = new View(this);
            separatorView.setBackgroundResource(R.color.gris73);
            prayersLL.addView(separatorView, layoutparamsSeparator);
        }
    }

    private String getName(JSONObject prayerObj) {
        if (_caller != null && _caller.equals("together")) {
            try {
                return "<small><font color='#000000'>" + prayerObj.getString("profilename") + ", </font></small>";
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private ImageView setCandleImage(ImageView candleIV, String candle_burning) {
        if (candle_burning.equals("0"))
            candleIV.setImageResource(R.drawable.list_candle_off);
        else
            candleIV.setImageResource(R.drawable.list_candle_on);
        return candleIV;
    }


    /**
     * Renvoie l'image associée à la prière
     * A défaut renvoie l'image de profil de l'utilisateur
     * A défaut renvoie une image vide
     *
     * @param profilepicURL
     * @param imageURL
     * @return
     */
    private ImageView setPrayerProfileImage(ImageView imageIV, String profilepicURL, String imageURL) {

        if (imageURL != null && imageURL.length() > 0) {
            profilepicURL = imageURL;
        }


        int size = Tools.STD_W / 5;

        if (profilepicURL != null && profilepicURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(profilepicURL + Tools.STD_W / 5);
            if (cachedImage == null) {
                //new DownloadImages(imageIV, true, profilepicURL + Tools.STD_W / 5).execute(Tools.MEDIAROOT + profilepicURL, String.valueOf(Tools.STD_W / 5));
                PoolRequetes.getInstance().ajouterNouvelleRequeteImage(imageIV, true, profilepicURL, size);
            }
            else {
                imageIV.setImageBitmap(cachedImage);
            }
            imageIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        RelativeLayout.LayoutParams imageLP = new RelativeLayout.LayoutParams(size, size);
        imageIV.setLayoutParams(imageLP);
        return imageIV;
    }

    private void getMyPrayers() {
        _progressDialog.show();
        if (_args == null)
            _args = new ArrayList<>();
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        if (_userId == null)
            _args.add(new BasicNameValuePair("id", UserConnected.getInstance().get_uid()));
        else
            _args.add(new BasicNameValuePair("id", _userId));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("type", "pray"));
        _args.add(new BasicNameValuePair("mode", "profile"));
        _args.add(new BasicNameValuePair("limit", "20"));
        _apiCaller = new HttpApiCall(this, _args, 1);
        _apiCaller.execute(Tools.API + Tools.CDCLASSIFIEDSLIST);
        _apiCaller = null;
    }

    private void setPrayersOnClickListeners() {
        Button createPrayerBtn = (Button)findViewById(R.id.button_create_prayer_com_my_prayers);
        //TODO couleur de la fleche
        //createPrayerBtn.setCompoundDrawables(null, null, getResources().getDrawable(R.drawable.disclosure_blue), null);
        createPrayerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createPrayerIntent = new Intent(MyPrayers.this, CreatePrayer.class);
                createPrayerIntent.putExtra("resId", _resId);
                startActivityForResult(createPrayerIntent, 1);
            }
        });
        if (_userId != null) {
            createPrayerBtn.setVisibility(View.GONE);
        }
        else {
            createPrayerBtn.setVisibility(View.VISIBLE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (Tools.CDDEBUG) {
            Log.d("onActivityResult", "" + requestCode);
        }

        /*if (requestCode == 1 && resultCode == RESULT_OK && data.hasExtra("changes") && data.getExtras().getBoolean("changes")) {

            if (_caller != null && _caller.equals("together")) {


                setPrayersOnClickListeners();
                getAllPrayers();
            }
            else {
                _changes = true;
                setPrayersOnClickListeners();
                getMyPrayers();
            }




        }*/
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableArrowID = R.drawable.disclosure;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableArrowID = R.drawable.disclosure_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableArrowID = R.drawable.disclosure_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableArrowID = R.drawable.disclosure_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableArrowID = R.drawable.disclosure_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableArrowID = R.drawable.disclosure_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableArrowID = R.drawable.disclosure_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableArrowID = R.drawable.disclosure_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableArrowID = R.drawable.disclosure_silver;
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

    @Override
    public void onBackPressed() {
        Intent myIntent = getIntent();
        if (_changes)
            setResult(RESULT_OK, myIntent);
        else
            setResult(RESULT_CANCELED, myIntent);
        Log.d("MyPrayers::onBackPressed::", String.valueOf(_changes));
        finish();
        super.onBackPressed();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(MyPrayers.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
            if (!resObj.getString("results").equals("null")) {
                TextView moreTV = (TextView)findViewById(R.id.textView_loadmore_com_my_prayers);
                if (resObj.has("more") && resObj.getString("more").equals("1"))
                    moreTV.setVisibility(View.VISIBLE);
                else
                    moreTV.setVisibility(View.GONE);
                JSONArray prayersArray = resObj.getJSONArray("results");
                if (prayersArray.length() > 0) {
                    TextView noResultTV = (TextView)findViewById(R.id.textView_no_results_com_my_prayers);
                    noResultTV.setVisibility(View.GONE);
                    displayPrayers(prayersArray, type);
                }
            }
        }
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}