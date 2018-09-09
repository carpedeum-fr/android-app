package com.user;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
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
import com.Tools.LogInFile;
import com.Tools.Tools;
import com.account.Candles;
import com.account.Premium;
import com.carpedeum.MainActivity;
import com.i2heaven.carpedeum.R;
import com.classifieds.MyClassifieds;
import com.messages.DisplayMessages;
import com.places.DisplayPlace;
import com.prayers.MyPrayers;
import com.status.CreateStatus;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Guillaume on 25/08/13.
 * Profile Activity when logged
 */

public class Profile implements LocationListener, ApiCaller {

    private static final String TAG = "Profile";
    MainActivity _mainAcInstance = null;
    private int _resId = -1;
    public static int PRAYER = 3;
    private static int MYCLASSIFIEDS = 4;
    public ProgressDialog _progressDialog = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;

    public Profile(MainActivity instance, int resID) {
        _mainAcInstance = instance;
        _resId = resID;

        initProgressDialog();
        setButtonsOnClickListeners();
        getMyProfileFromAPI();
    }

    private void initProgressDialog() {
        _progressDialog = new ProgressDialog(_mainAcInstance);
        _progressDialog.setMessage(_mainAcInstance.getString(R.string.ChargementEnCours));
        /*_progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (_mainAcInstance.getApplicationContext() != null)
                    Toast.makeText(_mainAcInstance.getApplicationContext(), _mainAcInstance.getString(R.string.LOADING_BACKGROUND), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    public void setButtonsOnClickListeners() {
        ImageView messageIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_new_messages_com_profile);
        if (messageIV != null) {
            messageIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent messagesIntent = new Intent(_mainAcInstance, DisplayMessages.class);
                    messagesIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(messagesIntent);
                }
            });
        }
        ImageView connectedUsersIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_connected_around_com_profile);
        if (connectedUsersIV != null) {
            connectedUsersIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent parvisIntent = new Intent(_mainAcInstance, Parvis.class);
                    parvisIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(parvisIntent);
                }
            });
        }
        ImageView connectedContactsIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_connected_contacts_com_profile);
        if (connectedContactsIV != null) {
            connectedContactsIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent parvisIntent = new Intent(_mainAcInstance, Parvis.class);
                    parvisIntent.putExtra("resId", _resId);
                    parvisIntent.putExtra("type", "connected_contacts");
                    _mainAcInstance.startActivity(parvisIntent);
                }
            });
        }
        Button publishBtn = (Button)_mainAcInstance.findViewById(R.id.button_publish_com_profile);
        if (publishBtn != null) {
            publishBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    publishNewStatus();
                }
            });
        }
        Button displayMessagesBtn = (Button)_mainAcInstance.findViewById(R.id.button_display_conversation_com_profile_options);
        if (displayMessagesBtn != null) {
            displayMessagesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent messagesIntent = new Intent(_mainAcInstance, DisplayMessages.class);
                    messagesIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(messagesIntent);
                }
            });
        }
        Button changeEmailBtn = (Button)_mainAcInstance.findViewById(R.id.button_change_email_com_profile_options);
        if (changeEmailBtn != null) {
            changeEmailBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(_mainAcInstance, ChangeEmail.class);
                    emailIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(emailIntent);
                }
            });
        }
        Button changePasswordBtn = (Button)_mainAcInstance.findViewById(R.id.button_change_password_com_profile_options);
        if (changePasswordBtn != null) {
            changePasswordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent passwdIntent = new Intent(_mainAcInstance, ChangePassword.class);
                    passwdIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(passwdIntent);
                }
            });
        }
        Button deleteProfileBtn = (Button)_mainAcInstance.findViewById(R.id.button_delete_profile_com_profile_options);
        if (deleteProfileBtn != null) {
            deleteProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent deleteProfileIntent = new Intent(_mainAcInstance, DeleteProfile.class);
                    deleteProfileIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivityForResult(deleteProfileIntent, Tools.DELETEPROFILE);
                }
            });
        }
        Button candlesBtn = (Button)_mainAcInstance.findViewById(R.id.button_account_candles_com_profile_options);
        if (candlesBtn != null) {
            if (UserConnected.getInstance().get_num_candles() != -1) {
                candlesBtn.setText(_mainAcInstance.getString(R.string.HOME_SHORTCUT_ACCOUNT_CANDLES) + " (" + UserConnected.getInstance().get_num_candles() + ")");
            }
            candlesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent candleIntent = new Intent(_mainAcInstance, Candles.class);
                    candleIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(candleIntent);
                }
            });
        }
        Button premiumBtn = (Button)_mainAcInstance.findViewById(R.id.button_premium_account_com_profile_options);
        if (premiumBtn != null) {
            premiumBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent premiumIntent = new Intent(_mainAcInstance, Premium.class);
                    premiumIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(premiumIntent);
                }
            });
        }
        Button inviteFriendsBtn = (Button)_mainAcInstance.findViewById(R.id.button_invite_friends_com_profile_options);
        if (inviteFriendsBtn != null) {
            inviteFriendsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent inviteFriendsIntent = new Intent(_mainAcInstance, InviteFriends.class);
                    inviteFriendsIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(inviteFriendsIntent);
                }
            });
        }
    }

    private void publishNewStatus() {
        Intent publishIntent = new Intent(_mainAcInstance, CreateStatus.class);
        publishIntent.putExtra("resId", _resId);
        _mainAcInstance.startActivityForResult(publishIntent, PRAYER);
    }

    public void getMyProfileFromAPI() {
        if (Tools.CDDEBUG)
            Log.d(TAG, "getMyProfileFromAPI::uid::" + UserConnected.getInstance().get_sid() + " - " + UserConnected.getInstance().get_uid());
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("id", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
        apiCaller.execute(Tools.API + Tools.CDPROFILESGET);
    }

    @Override
    public void onApiResult(String result, int type) {
        if (_progressDialog != null) {
            _progressDialog.cancel();
        }
        if (result != null) {
            try {
                JSONObject resultObj = new JSONObject(result);
                if (resultObj.has("ok") && resultObj.getString("ok").equals("1")) {
                    if (type == 1) {
                        setUserProfileInfos(resultObj);
                    }
                    else if (type == 2) {
                        changeStatus();
                    }
                    else if (type == 3) {
                        if (Tools.CDDEBUG) {
                            Log.d("Profile::onApiResult::", "GPS mis a jour::" + result);
                            LogInFile.getInstance().WriteLog("Profile::onApiResult::GPS mis à jour::" + result, true);
                        }
                    }
                    else if (type == 4) {
                        if (Tools.CDDEBUG) {
                            Log.d("Profile::onApiResult::lieux::", result);
                            // Afficher les églises autour
                            displayPlacesAround(new JSONArray(resultObj.getString("results")));
                        }
                    }
                    else if (type == 5) {
                        JSONObject resObj = new JSONObject(result);
                        if (resObj.has("ok") && resObj.getString("ok").equals("1")) {
                            UserConnected.getInstance().set_num_candles(resObj.getInt("num_available"));
                            if (Tools.CDDEBUG) {
                                LogInFile.getInstance().WriteLog("Prayer::cierges restants: " + resObj.getString("num_available"), true);
                                Log.d(TAG, "cierges restants: " + resObj.getString("num_available"));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        assert _progressDialog != null;
        _progressDialog.cancel();
    }

    private void changeStatus() {
        TextView onlineTV = (TextView)_mainAcInstance.findViewById(R.id.textView_online_com_profile);
        ImageView onlineIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_online_com_profile);
        if (UserConnected.getInstance().is_online()) {
            UserConnected.getInstance().set_online(false);
            onlineIV.setImageResource(R.drawable.switch_off);
            onlineTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_APPEARS_OFFLINE));
            if (Tools.CDDEBUG) Log.d("Profile::changeStatus::", "j'etais en ligne, je passe hors ligne !");
        }
        else {
            UserConnected.getInstance().set_online(true);
            onlineIV.setImageResource(R.drawable.switch_on);
            onlineTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_APPEARS_ONLINE));
            if (Tools.CDDEBUG) Log.d("Profile::changeStatus::", "j'étais hors ligne, je passe en ligne !");
        }
    }

    /*
        Cette fonction remplie la classe UserConnected avec les données de l'utilisateur.
     */
    private void setUserProfileInfos(final JSONObject profileObj) throws JSONException {

        UserConnected.getInstance().set_jsonObj(profileObj.toString());
        UserConnected.getInstance().set_firstName(profileObj.getString("first_name"));
        UserConnected.getInstance().set_lastName(profileObj.getString("last_name"));
        if (profileObj.getInt("premium") == 1) {
            UserConnected.getInstance().set_premium(true);
            displayProfileViews();
        }
        else {
            TextView numViews = (TextView)_mainAcInstance.findViewById(R.id.textView_num_visited_com_profile);
            numViews.setVisibility(View.GONE);
        }
        if (profileObj.has("age") && !profileObj.getString("age").equals(""))
            UserConnected.getInstance().set_age(profileObj.getString("age"));
        if (profileObj.getString("gender").equals("M"))
            UserConnected.getInstance().set_gender(UserConnected.GENDER_M);
        else
            UserConnected.getInstance().set_gender(UserConnected.GENDER_F);
        UserConnected.getInstance().set_birthDate(profileObj.getString("date_birth"));
        UserConnected.getInstance().set_selfportrait(profileObj.getString("selfportrait"));
        UserConnected.getInstance().set_num_prayers(Integer.parseInt(profileObj.getString("num_prayers")));
        UserConnected.getInstance().set_num_classifieds(Integer.parseInt(profileObj.getString("num_classifieds")));

        UserConnected.getInstance().setPic(profileObj.getString("image"));
        setProfilePic();

        TextView onlineTV = (TextView)_mainAcInstance.findViewById(R.id.textView_online_com_profile);
        ImageView onlineIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_online_com_profile);
        if (profileObj.getString("online").equals("1")) {
            UserConnected.getInstance().set_online(true);
            if (onlineIV != null) {
                onlineIV.setImageResource(R.drawable.switch_on);
                onlineTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_APPEARS_ONLINE));
            }
        }
        else {
            UserConnected.getInstance().set_online(false);
            if (onlineIV != null) {
                onlineIV.setImageResource(R.drawable.switch_off);
                onlineTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_APPEARS_OFFLINE));
            }
        }
        setLocalisationInfos();
        /*if (profileObj.has("list_updates") && !profileObj.getString("list_updates").equals("")) {
            displayMyActivities(new JSONArray(profileObj.getString("list_updates")));
        }*/
        LinearLayout myProfileLL = (LinearLayout)_mainAcInstance.findViewById(R.id.linearLayout_my_profile_com_profile);
        if (myProfileLL != null) {
            myProfileLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myprofileIntent = new Intent(_mainAcInstance, MyProfile.class);
                    myprofileIntent.putExtra("resId", _resId);
                    try {
                        if (profileObj.has("list_updates") && !profileObj.getString("list_updates").equals("")) {
                            myprofileIntent.putExtra("updates", profileObj.getString("list_updates"));
                        }
                        else {
                            myprofileIntent.putExtra("updates", "null");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    _mainAcInstance.startActivity(myprofileIntent);
                }
            });
        }
        getCandlesFromApi();
        setProfileButtonsOnClickListeners();
    }

    private void displayProfileViews() {
        if (UserConnected.getInstance().get_num_visited() != -1) {
            TextView numViews = (TextView)_mainAcInstance.findViewById(R.id.textView_num_visited_com_profile);
            numViews.setVisibility(View.VISIBLE);
            if (UserConnected.getInstance().get_num_visited() > 1)
                numViews.setText(Html.fromHtml(_mainAcInstance.getString(R.string.HOME_VISIT_N, UserConnected.getInstance().get_num_visited())));
            else
                numViews.setText(Html.fromHtml(_mainAcInstance.getString(R.string.HOME_VISIT_0_1, UserConnected.getInstance().get_num_visited())));
        }
    }

    private void getCandlesFromApi() {
        _args.clear();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _apiCaller = new HttpApiCall(this, _args, 5);
        _apiCaller.execute(Tools.API + Tools.CDACCOUNTCANDLEGET);
    }

    public static void setUserInfos(JSONObject profileObj, String sid) throws Exception {
        UserConnected.getInstance().setUserConnected(true);
        try {
            UserConnected.getInstance().set_uid(profileObj.getString("id"));
        } catch (Exception e) {
            UserConnected.getInstance().set_uid(profileObj.getString("uid"));
        }
        UserConnected.getInstance().set_sid(sid);
        UserConnected.getInstance().set_jsonObj(profileObj.toString());
        UserConnected.getInstance().set_firstName(profileObj.getString("first_name"));
        UserConnected.getInstance().set_lastName(profileObj.getString("last_name"));
        if (profileObj.getInt("premium") == 1)
            UserConnected.getInstance().set_premium(true);
        if (profileObj.has("age") && !profileObj.getString("age").equals(""))
            UserConnected.getInstance().set_age(profileObj.getString("age"));
        if (profileObj.getString("gender").equals("M"))
            UserConnected.getInstance().set_gender(UserConnected.GENDER_M);
        else
            UserConnected.getInstance().set_gender(UserConnected.GENDER_F);
        UserConnected.getInstance().set_birthDate(profileObj.getString("date_birth"));
        UserConnected.getInstance().set_selfportrait(profileObj.getString("selfportrait"));
        UserConnected.getInstance().set_num_prayers(Integer.parseInt(profileObj.getString("num_prayers")));
        UserConnected.getInstance().set_num_classifieds(Integer.parseInt(profileObj.getString("num_classifieds")));
        if (profileObj.getString("online").equals("1"))
            UserConnected.getInstance().set_online(true);
        else
            UserConnected.getInstance().set_online(false);
        if (profileObj.getString("image").length() > 0)
            UserConnected.getInstance().setPic(profileObj.getString("image"));
    }

    private void setLocalisationInfos() {
        final Location location = checkIfGPSEnabledAndGetLocation();
        ImageView gpsIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_gps_com_profile);
        TextView gpsTV = (TextView)_mainAcInstance.findViewById(R.id.textView_gps_com_profile);
        Button setLocationManualyBtn = (Button)_mainAcInstance.findViewById(R.id.button_manual_location_com_profile_options);
        if (gpsIV != null && gpsTV != null) {
            if (location != null) {
                gpsIV.setImageResource(R.drawable.switch_on);
                gpsTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_GPS_IS_ON));
                setLocationManualyBtn.setVisibility(View.GONE);
                updateLocation(location);
            }
            else {
                gpsIV.setImageResource(R.drawable.switch_off);
                gpsTV.setText(_mainAcInstance.getString(R.string.HOME_SWITCH_GPS_IS_OFF));
                setLocationManualyBtn.setVisibility(View.VISIBLE);
            }
            gpsIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showGPSSeetings();
                }
            });
        }
    }

    private void updateLocation(Location location) {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("geolat", String.valueOf(location.getLatitude())));
        _args.add(new BasicNameValuePair("geolng", String.valueOf(location.getLongitude())));
        if (Tools.CDDEBUG) {
            Log.d("Profile::updateLocation::", "updating location of uid " +
                    UserConnected.getInstance().get_uid() + ", lat : " + String.valueOf(location.getLatitude())
                    + ", lng : " + String.valueOf(location.getLongitude()));
            LogInFile.getInstance().WriteLog("Profile::updateLocation::updating location of uid " +
                    UserConnected.getInstance().get_uid() + ", lat : " + String.valueOf(location.getLatitude())
                    + ", lng : " + String.valueOf(location.getLongitude()), true);
        }
        UserConnected.getInstance().set_geolat(String.valueOf(location.getLatitude()));
        UserConnected.getInstance().set_geolng(String.valueOf(location.getLongitude()));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 3);
        String url = Tools.API + Tools.CDPROFILESET;
        apiCaller.execute(url);
        requestPlacesFromApi(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
    }

    //Call api to get the places located around me
    private void requestPlacesFromApi(String lat, String lng) {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("geolat", lat));
        _args.add(new BasicNameValuePair("geolng", lng));
        _args.add(new BasicNameValuePair("limit", "75"));
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 4);
        String url = Tools.API + Tools.CDPLACESLIST;
        apiCaller.execute(url);
    }


    /**
     * Afficher les églises autour
     *
     * @param placesArray
     */
    private void displayPlacesAround(JSONArray placesArray) {
        LinearLayout allPlacesLL = (LinearLayout)_mainAcInstance.findViewById(R.id.linearLayout_places_com_profile);
        if (allPlacesLL != null) {
            allPlacesLL.removeAllViews();
            for (int i = 0; i < 3; ++i) {
                try {
                    final JSONObject placeObj = placesArray.getJSONObject(i);
                    //LinearLayout placeLL = new LinearLayout(_mainAcInstance);
                    //placeLL.setBackgroundColor(_mainAcInstance.getResources().getColor(R.color.white));


                    View placeView = _mainAcInstance.getLayoutInflater().inflate(R.layout.com_profile_place_list, null);
                    assert placeView != null;

                    /**
                     * Image du lieu
                     */
                    /*
                    LinearLayout.LayoutParams placeImageLP = new LinearLayout.LayoutParams(Tools.STD_W / 7, Tools.STD_W / 7);
                    placeLL.setOrientation(LinearLayout.HORIZONTAL);
                    ImageView profileIV = getPlaceImage(placeObj.getString("image"));
                    if (profileIV != null) {
                        profileIV.setPadding(0, 0, 5, 0);
                        placeLL.addView(profileIV, placeImageLP);
                    }
                    */


                    ImageView placeIV = (ImageView)placeView.findViewById(R.id.imageView_churchimage_com_profile_place_list);
                    setPlaceImage(placeIV, placeObj.getString("image"));



                    TextView placesInfosTV = (TextView)placeView.findViewById(R.id.textView_name_com_profile_place_list);
                    placesInfosTV.setText(placeObj.getString("name"));


                    TextView placeDistTV = (TextView)placeView.findViewById(R.id.textView_distance_com_profile_place_list);
                    placeDistTV.setText(placeObj.getString("distance") + " - " + Tools.CDPLACESCORRES.get(placeObj.getString("type")));

                    placeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent displayPlaceIntent = new Intent(_mainAcInstance, DisplayPlace.class);
                            displayPlaceIntent.putExtra("resId", _resId);
                            try {
                                displayPlaceIntent.putExtra("id", placeObj.getString("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            _mainAcInstance.startActivity(displayPlaceIntent);
                        }
                    });

                    allPlacesLL.addView(placeView);

                    // Séparateur
                    LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                    View separatorView = new View(_mainAcInstance);
                    separatorView.setBackgroundResource(R.color.gris73);
                    allPlacesLL.addView(separatorView, layoutparamsSeparator);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Renvoie l'image du lieu religieux
     *
     *
     * @param placeIV
     * @param picURL
     * @return
     */
    private void setPlaceImage(ImageView placeIV, String picURL) {

        if (picURL != null && picURL.length() > 0) {
            int size = Tools.STD_W / 7;
            RelativeLayout.LayoutParams profileLP = new RelativeLayout.LayoutParams(size, size);
            placeIV.setLayoutParams(profileLP);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(picURL + size);
            if (cachedImage == null) {
                new DownloadImages(placeIV, true, picURL + size).execute(Tools.MEDIAROOT + picURL, String.valueOf(size));
            }
            else {
                placeIV.setImageBitmap(cachedImage);
                placeIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private void showGPSSeetings() {
        _mainAcInstance.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
    }

    private void setProfileButtonsOnClickListeners() {
        Button myPrayersBtn = (Button)_mainAcInstance.findViewById(R.id.button_myprayers_com_profile);
        if (myPrayersBtn != null) {
            myPrayersBtn.setText(_mainAcInstance.getString(R.string.HOME_MYLIST_PRAYERS) + " (" + UserConnected.getInstance().get_num_prayers() + ")");
            myPrayersBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent prayersIntent = new Intent(_mainAcInstance, MyPrayers.class);
                    prayersIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivity(prayersIntent);
                }
            });
        }

        Button myClassifiedsBtn = (Button)_mainAcInstance.findViewById(R.id.button_myclassifieds_com_profile);
        String myClassifiedsString =  _mainAcInstance.getString(R.string.HOME_MYLIST_CLASSIFIEDS);
        myClassifiedsString += " (" + String.valueOf(UserConnected.getInstance().get_num_classifieds()) + ")";
        if (myClassifiedsBtn != null) {
            myClassifiedsBtn.setText(myClassifiedsString);
            myClassifiedsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent classifiedsIntent = new Intent(_mainAcInstance, MyClassifieds.class);
                    classifiedsIntent.putExtra("resId", _resId);
                    _mainAcInstance.startActivityForResult(classifiedsIntent, MYCLASSIFIEDS);
                }
            });
        }
        ImageView onlineIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_online_com_profile);
        if (onlineIV != null) {
            onlineIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeOnlineStatus();
                }

                private void changeOnlineStatus() {
                    List<NameValuePair> _args = new ArrayList<NameValuePair>();
                    _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                    _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                    if (UserConnected.getInstance().is_online()) {
                        _args.add(new BasicNameValuePair("invisible", "1"));
                    }
                    else {
                        _args.add(new BasicNameValuePair("invisible", "0"));
                    }
                    HttpApiCall apiCaller = new HttpApiCall(Profile.this, _args, 2);
                    String url = Tools.API + Tools.CDACCOUNTINVISIBLE;
                    apiCaller.execute(url);
                }
            });
        }
    }

    public void setProfileLayout() {
        setProfilePic();
    }


    /**
     * Afficher mon image de profil
     */
    private void setProfilePic() {
        ImageView profileIV = (ImageView)_mainAcInstance.findViewById(R.id.imageView_profilepic_com_profile);
        if (profileIV != null) {
            int size = Tools.STD_W / 7;
            LinearLayout.LayoutParams profilePicLP = new LinearLayout.LayoutParams(size, size);
            profileIV.setLayoutParams(profilePicLP);
            if (_mainAcInstance.internetConnectionOk()) {
                if (UserConnected.getInstance().getPic() != null && UserConnected.getInstance().getPic().length() > 0)
                    new DownloadImageTask(profileIV, true, true).execute(Tools.MEDIAROOT + UserConnected.getInstance().getPic());
            }
        }
    }

    private Location checkIfGPSEnabledAndGetLocation() {
        LocationManager lm = (LocationManager)_mainAcInstance.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        boolean located;
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {

            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, this);
                located = true;
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            else
            {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
                located = true;
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
        else
        {
            located = false;
        }
        if (located && location != null) {
            if (Tools.CDDEBUG) {
                Log.d("Profile::", "located");
                LogInFile.getInstance().WriteLog("Profile::checkIfGPSEnabled::GPS enabled", true);
            }
            return location;
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
}