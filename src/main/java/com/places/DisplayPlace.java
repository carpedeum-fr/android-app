package com.places;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.geomesse.CreatePlace;
import com.messages.DisplayMessages;
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Guillaume on 20/10/13.
 * Affiche la carte de visite d'un lieu
 */
public class DisplayPlace extends Activity implements ApiCaller {

    private int _resId = -1;
    private ProgressDialog _progressDialog = null;
    private int _drawableArrowID = -1;
    private int _checkInID = -1;
    private HashMap<String, String> _places = null;
    private LinkedHashMap<String, Integer> _daysOfWeek = null;
    private String _placeId = null;
    private ArrayList<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;
    private String TAG = "DisplayPlace";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_places_display_place);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            setLayoutTheme();
        }
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
        imageLogoIV.setVisibility(View.GONE);
        TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
        titleTV.setVisibility(View.VISIBLE);
        titleTV.setText(getResources().getString(R.string.Geomesse));
        assert extras != null;
        fillPlaceTypesArray();
        if (extras.containsKey("id")) {
            _placeId = extras.getString("id");
            getPlaceInfosFromAPI(extras.getString("id"));
        }
        try {
            ManageAds.displayAdds(this, getScreenWidth(), true, R.id.headershadow, getScreenWidth());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*

    private void getAdsFromAdsManager() throws Exception {
        final JSONObject ads = ManageAds.getInstance().getAdd();
        if (Tools.CDDEBUG)
            Log.d("Calendar:ads:", ads.toString());
        ImageView addIV = (ImageView)findViewById(R.id.adds);
        if (getScreenWidth() > Tools.REF_SCREEN_WIDTH)
            new DownloadImageTask(addIV, false).execute(ads.getString("image"));
        else
            new DownloadImageTask(addIV, true).execute(ads.getString("image"));
        addIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!ads.getString("title").equals("Carpe Deum")) {
                        Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                        openPageIntent.setData(Uri.parse(ads.getString("url")));
                        startActivity(openPageIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    */

    private void fillPlaceTypesArray() {
        _places = Tools.CDPLACESCORRES;

        _daysOfWeek = new LinkedHashMap<String, Integer>();
        _daysOfWeek.put("MONDAY", R.string.PLACE_SCHEDULE_MONDAY);
        _daysOfWeek.put("TUESDAY", R.string.PLACE_SCHEDULE_TUESDAY);
        _daysOfWeek.put("WEDNESDAY", R.string.PLACE_SCHEDULE_WEDNESDAY);
        _daysOfWeek.put("THURSDAY", R.string.PLACE_SCHEDULE_THURSDAY);
        _daysOfWeek.put("FRIDAY", R.string.PLACE_SCHEDULE_FRIDAY);
        _daysOfWeek.put("SATURDAY", R.string.PLACE_SCHEDULE_SATURDAY);
        _daysOfWeek.put("SUNDAY", R.string.PLACE_SCHEDULE_SUNDAY);
    }

    private void getPlaceInfosFromAPI(String id) {
        if (internetConnectionOk()) {
            _progressDialog.show();
            if (_args == null)
                _args = new ArrayList<NameValuePair>();
            _args.clear();
            if (UserConnected.getInstance().IsUserConnected()) {
                _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
            }
            _args.add(new BasicNameValuePair("id", id));
            /*_apiCaller = new HttpApiCall(this, _args, 1);
            _apiCaller.execute(Tools.API + Tools.CDPLACESGET);
            _apiCaller = null;
            */

            PoolRequetes.getInstance().ajouterNouvelleRequete(this, _args, Tools.API + Tools.CDPLACESGET, 1);

        }
    }

    private void displayPlace(final JSONObject resObj) throws Exception {
        boolean checked = false;
        displayPlaceInfos(resObj);
        displayEditButton(resObj);
        if (resObj.getString("image").length() > 0)
            displayPlacePicture(resObj.getString("image"));
        if (resObj.getString("schedule").length() > 0)
            displayHours(resObj.getString("schedule"), resObj.getString("schedule_notes"));
        Button urlBtn = (Button)findViewById(R.id.button_web_com_places_display_place);
        if (resObj.getString("url").length() == 0)
            urlBtn.setVisibility(View.GONE);
        else
            displayURLBtn(resObj.getString("url").substring(resObj.getString("url").indexOf("\"") + 1, resObj.getString("url").lastIndexOf("\"")));
        Button telBtn = (Button)findViewById(R.id.button_tel_com_places_display_place);
        if (resObj.getString("tel").length() == 0)
            telBtn.setVisibility(View.GONE);
        else
            displayTelBtn(resObj.getString("tel").substring(resObj.getString("tel").indexOf("\"") + 1, resObj.getString("tel").lastIndexOf("\"")));
        displayOpenMapsBtn(resObj.getString("geolng"), resObj.getString("geolat"));
        if (resObj.has("checkins") && !resObj.getString("checkins").equals("null"))
            checked = checkIn(new JSONArray(resObj.getString("checkins")));
        Button checkInBtn = (Button)findViewById(R.id.button_checkin_com_places_display_place);
        if (UserConnected.getInstance().IsUserConnected()) {
            checkInBtn.setVisibility(View.VISIBLE);
            if (checked) {
                checkInBtn.setText(getString(R.string.PLACE_BUTTON_CHECKEDIN));
            }
            else {
                checkInBtn.setText(getString(R.string.PLACE_BUTTON_CHECKIN));
                checkInBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _args.clear();
                        if (UserConnected.getInstance().IsUserConnected()) {
                            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                        }
                        try {
                            _args.add(new BasicNameValuePair("id", resObj.getString("id")));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        _apiCaller = new HttpApiCall(DisplayPlace.this, _args, 2);
                        _apiCaller.execute(Tools.API + Tools.CDPLACESCHECKIN);
                        _apiCaller = null;
                    }
                });
            }
        }
        else
            checkInBtn.setVisibility(View.GONE);
    }

    private void displayEditButton(final JSONObject place) {
        final Button editPlaceBtn = (Button)findViewById(R.id.button_edit_com_places_display_place);
        editPlaceBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        editPlaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editPlaceIntent = new Intent(DisplayPlace.this, CreatePlace.class);
                editPlaceIntent.putExtra("resId", _resId);
                editPlaceIntent.putExtra("place", place.toString());
                startActivity(editPlaceIntent);
            }
        });
    }

    private boolean checkIn(JSONArray checkinsArray) throws Exception {
        Button checkInBtn = (Button)findViewById(R.id.button_checkin_com_places_display_place);
        boolean checkedIn = false;
        LinearLayout checkinsLL = (LinearLayout)findViewById(R.id.linearLayout_checkins_com_places_display_place);
        checkinsLL.removeAllViews();
        for (int i = 0; i < checkinsArray.length(); ++i) {
            JSONObject checkInObj = checkinsArray.getJSONObject(i);
            if (checkInObj.getString("id").equals(UserConnected.getInstance().get_uid()))
                checkedIn = true;
            addCheckedPerson(checkInObj, checkinsLL);
        }
        if (checkedIn) {
            checkInBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_checkInID), null);
        }
        return checkedIn;
    }

    private void addCheckedPerson(final JSONObject checkInObj, LinearLayout checkinsLL) throws Exception {
        LinearLayout checkedPersonLL = new LinearLayout(this);
        checkedPersonLL.setBackgroundColor(getResources().getColor(R.color.white));
        final LinearLayout.LayoutParams personImageLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 20);
        checkedPersonLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView profileIV = getPersonImage(checkInObj.getString("profilepic"));
        if (profileIV != null) {
            profileIV.setPadding(0, 0, 5, 0);
            checkedPersonLL.addView(profileIV, personImageLP);
        }
        TextView infosTV = new TextView(this);
        String text = checkInObj.getString("profilename") + "<br/>";
        text += "<small><font color='#737373'>" + checkInObj.getString("dateinfo") + "</font></small><br/>";
        infosTV.setTextSize(15);
        infosTV.setText(Html.fromHtml(text));
        LinearLayout.LayoutParams infosLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 60);
        infosTV.setTextColor(getResources().getColor(R.color.black));
        infosLP.setMargins(5, 0, 0, 0);
        checkedPersonLL.addView(infosTV, infosLP);
        checkedPersonLL.setPadding(5, 5, 5, 5);
        checkedPersonLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(DisplayPlace.this, MyProfile.class);
                profileIntent.putExtra("resId", _resId);
                try {
                    profileIntent.putExtra("profileId", checkInObj.getString("profileid"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(profileIntent);
            }
        });
        ImageView arrowIV = new ImageView(this);
        arrowIV.setImageResource(_drawableArrowID);
        LinearLayout.LayoutParams arrowLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 10);
        arrowLP.gravity = Gravity.CENTER;
        checkedPersonLL.addView(arrowIV, arrowLP);
        checkinsLL.addView(checkedPersonLL);
        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris73);
        checkinsLL.addView(separatorView, layoutparamsSeparator);
    }

    private ImageView getPersonImage(String picURL) {
        ImageView profileIV = new ImageView(this);
        int size = getScreenWidth() / 7;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        if (picURL != null && picURL.length() > 0) {
            profileIV.setLayoutParams(profileLP);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(picURL + size);
            if (cachedImage == null) {
                new DownloadImages(profileIV, true, picURL + size).execute(Tools.MEDIAROOT + picURL, String.valueOf(size));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
                profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        profileIV.setLayoutParams(profileLP);
        return profileIV;
    }

    private void displayOpenMapsBtn(final String geolng, final String geolat) {
        Button openMapBtn = (Button)findViewById(R.id.button_display_map_com_places_display_place);
        if (geolng.length() > 0 && geolat.length() > 0) {
            openMapBtn.setVisibility(View.VISIBLE);
            openMapBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
            openMapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?daddr=" + geolat + "," + geolng));
                    Log.d("url: ", "http://maps.google.com/?daddr=" + geolat + "," + geolng);
                    startActivity(intent);
                }
            });
        }
        else {
            openMapBtn.setVisibility(View.GONE);
        }
    }

    private void displayURLBtn(final String url) {
        final Button urlBtn = (Button)findViewById(R.id.button_web_com_places_display_place);
        urlBtn.setVisibility(View.VISIBLE);
        urlBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        final String newUrl = url.replace("\\", "");
        urlBtn.setText(getString(R.string.PLACE_BUTTON_URL) + ": " + newUrl);
        urlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                Log.d(TAG, "URL: " + newUrl);
                openPageIntent.setData(Uri.parse(newUrl));
                startActivity(openPageIntent);
            }
        });
    }

    private void displayTelBtn(final String tel) {
        final Button telBtn = (Button)findViewById(R.id.button_tel_com_places_display_place);
        if (tel == null || tel.length() == 0) {
            telBtn.setVisibility(View.GONE);
        }
        else {
            telBtn.setVisibility(View.VISIBLE);
            telBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
            telBtn.setText(getString(R.string.PLACE_BUTTON_TEL) + ": " + tel);
            telBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + tel));
                    startActivity(callIntent);
                }
            });
        }
    }

    /**
     * Affiche les horaire de l'eglise
     *
     * @param schedule
     * @param notes
     */
    private void displayHours(final String schedule, final String notes) {
        final Button displayHoursBtn = (Button)findViewById(R.id.button_hours_com_places_display_place);
        displayHoursBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        displayHoursBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayHoursBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                String text = "<small>" + getString(R.string.PLACE_SCHEDULE_WEEK) + ":</small><br/><br/>";
                try {
                    if (!schedule.equals("null")) {
                        JSONArray scheduleArray = new JSONArray(schedule);
                        for (String day : _daysOfWeek.keySet() ) {
                            JSONArray hoursArray = getHoursForTheDay(day, scheduleArray);
                            // N'affiche le jour que s'il y a des horaires à afficher
                            if (hoursArray.length() > 0) {
                                text += "<font color='#666666'>" + getString(_daysOfWeek.get(day)).toUpperCase() + "</font>&nbsp;";
                            }
                            for (int i = 0; i < hoursArray.length(); ++i) {
                                JSONObject hourObj = hoursArray.getJSONObject(i);
                                text += hourObj.getString("time") + "<br/>";
                                if (hourObj.getString("notes").length() > 0)
                                    text += "<font color='#666666'><small>" + hourObj.getString("notes") + "</small></font><br/>";
                            }
                        }
                        text += "<br/>";
                        if (notes.length() > 0)
                            text += notes;
                        displayHoursBtn.setGravity(Gravity.CENTER);
                        displayHoursBtn.setText(Html.fromHtml(text));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            private JSONArray getHoursForTheDay(String day, JSONArray scheduleArray) throws JSONException {
                JSONArray hoursArray = new JSONArray();
                for (int i = 0; i < scheduleArray.length(); ++i) {
                    JSONObject dayObj = scheduleArray.getJSONObject(i);
                    if (dayObj.getString("dayofweek").equals(day))
                        hoursArray.put(dayObj);
                }
                return hoursArray;
            }
        });
    }

    private void displayPlacePicture(String url) {
        if (url != null && url.length() > 0) {
            ImageView picIV = (ImageView)findViewById(R.id.imageView_place_pic_com_places_display_place);
            int size = getScreenWidth() / 2;
            Log.d("SIZE : ", "" + size);
            LinearLayout.LayoutParams pictureLP = new LinearLayout.LayoutParams(size, size);
            picIV.setLayoutParams(pictureLP);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(url + size);
            if (cachedImage == null) {
                new DownloadImages(picIV, true, url + size).execute(Tools.MEDIAROOT + url, String.valueOf(size));
            }
            else {
                picIV.setImageBitmap(cachedImage);
                picIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private void displayPlaceInfos(JSONObject resObj) throws Exception {
        TextView typeTV = (TextView)findViewById(R.id.textView_place_type_com_places_display_place);
        typeTV.setText(resObj.getString("typename"));
        TextView nameTV = (TextView)findViewById(R.id.textView_place_name_com_places_display_place);
        nameTV.setText(resObj.getString("name"));
        TextView locTV = (TextView)findViewById(R.id.textView_place_location_com_places_display_place);
        locTV.setText(resObj.getString("address"));
    }

    private void setLayoutTheme() {
        if (_resId == 0 ||_resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableArrowID = R.drawable.disclosure;
            _checkInID = R.drawable.list_checkin;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableArrowID = R.drawable.disclosure_blue;
            _checkInID = R.drawable.list_checkin_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableArrowID = R.drawable.disclosure_gold;
            _checkInID = R.drawable.list_checkin_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableArrowID = R.drawable.disclosure_green;
            _checkInID = R.drawable.list_checkin_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableArrowID = R.drawable.disclosure_mauve;
            _checkInID = R.drawable.list_checkin_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableArrowID = R.drawable.disclosure_orange;
            _checkInID = R.drawable.list_checkin_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableArrowID = R.drawable.disclosure_purple;
            _checkInID = R.drawable.list_checkin_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableArrowID = R.drawable.disclosure_red;
            _checkInID = R.drawable.list_checkin_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableArrowID = R.drawable.disclosure_silver;
            _checkInID = R.drawable.list_checkin_silver;
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

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(DisplayPlace.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject resObj = new JSONObject(result);
        if (resObj.has("ok") && !resObj.has("message")) {
            if (type == 1)
                displayPlace(resObj);
            else if (type == 2)
                getPlaceInfosFromAPI(_placeId);
        }
    }
}