package com.user;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.account.Premium;
import com.ads.ManageAds;
import com.i2heaven.carpedeum.R;
import com.classifieds.MyClassifieds;
import com.messages.DisplayMessage;
import com.messages.DisplayMessages;
import com.prayers.MyPrayers;
import com.prayers.Prayer;
import com.status.DisplayStatus;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 26/08/13.
 * Display my profile
 */

public class MyProfile extends Activity implements ApiCaller {

    private int _resId = -1;
    private static int EDIT_PROFILE = 1;
    private static int MYPRAYERS = 2;
    private static int STATUS = 3;
    private static int MYCLASSIFIEDS = 4;
    private JSONArray _updates = null;
    private boolean _myProfile = false;
    private JSONObject _profileInfosObj = null;
    private int _listContactImdId = -1;
    private ProgressDialog _progressDialog = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_profile_my_profile);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        if (extras != null) {
            _resId = extras.getInt("resId");
            try {
                if (extras.containsKey("updates") && !extras.getString("updates").equals("null"))
                    _updates = new JSONArray(extras.getString("updates"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.MonProfil));
        }
        assert extras != null;
        if (UserConnected.getInstance().IsUserConnected()) {
            if (extras.containsKey("profileId")) {
                _myProfile = false;
                downloadProfileOfProfileId(extras.getString("profileId"));
            }
            else {
                _myProfile = true;
                try {
                    displayProfile(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            //TODO remplacer par un vrai appel a l'api ?
            setContentView(R.layout.error);
        }
        displayPub();
    }

    private void displayPub() {
        /*try {
            final JSONObject addObj = ManageAds.getInstance().getAdd();
            ImageView addIV = (ImageView)findViewById(R.id.adds_com_profile_my_profile);

            if (addIV != null) {
                new DownloadImageTask(addIV, true).execute(addObj.getString("image"));
                addIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (addObj.getString("url").contains("accountpremium")) {
                                Intent premiumIntent = new Intent(MyProfile.this, Premium.class);
                                premiumIntent.putExtra("resId", _resId);
                                startActivity(premiumIntent);
                            }
                            else {
                                Intent openPageIntent = new Intent(Intent.ACTION_VIEW);
                                openPageIntent.setData(Uri.parse(addObj.getString("url")));
                                startActivity(openPageIntent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            ManageAds.displayAdds(this, getScreenWidth(), true, R.id.headershadow, Tools.STD_W);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadProfileOfProfileId(String profileId) {
        _progressDialog.show();
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        String url;
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        _args.add(new BasicNameValuePair("id", profileId));
        url = Tools.API + Tools.CDPROFILESGET;
        HttpApiCall apiCaller = new HttpApiCall(MyProfile.this, _args, 1);
        apiCaller.execute(url);
    }

    private void displayProfile(JSONObject profileObj) throws Exception {
        if (_myProfile)
            displayProfilePicture(null);
        else {
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setText(profileObj.getString("name"));
            _profileInfosObj = profileObj;
            displayProfilePicture(profileObj.getString("image"));
        }
        if (_updates != null)
            displayMyActivities();
        setProfileOnClickListeners(profileObj);
        displayNameAndAge();
        setProfileOnClickListernersWithApiInfos(profileObj);
    }

    private void setProfileOnClickListeners(JSONObject profileObj) throws Exception {
        LinearLayout contactsLL = (LinearLayout)findViewById(R.id.linearLayout_contacts_com_profile_my_profile);
        Button editPhotoBtn = (Button)findViewById(R.id.button_edit_photo_com_profile_my_profile);
        Button editProfileBtn = (Button)findViewById(R.id.button_edit_infos_com_profile_my_profile);
        Button blockProfileBtn = (Button)findViewById(R.id.button_block_com_profile_my_profile);
        if (_myProfile) {
            contactsLL.setVisibility(View.VISIBLE);
            blockProfileBtn.setVisibility(View.GONE);
            editProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent editProfileIntent = new Intent(MyProfile.this, EditProfileInfos.class);
                    editProfileIntent.putExtra("resId", _resId);
                    startActivityForResult(editProfileIntent, EDIT_PROFILE);
                }
            });
            editPhotoBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editProfileIntent = new Intent(MyProfile.this, EditPhotos.class);
                    editProfileIntent.putExtra("resId", _resId);
                    startActivityForResult(editProfileIntent, EDIT_PROFILE);
                }
            });
        }
        else {
            editProfileBtn.setVisibility(View.GONE);
            editPhotoBtn.setVisibility(View.GONE);
            Button sendMessageBtn = (Button)findViewById(R.id.button_send_message_com_profile_my_profile);
            sendMessageBtn.setVisibility(View.VISIBLE);
            sendMessageBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent sendMessageIntent = new Intent(MyProfile.this, DisplayMessage.class);
                        sendMessageIntent.putExtra("resId", _resId);
                        sendMessageIntent.putExtra("messageID", _profileInfosObj.getString("id"));
                        sendMessageIntent.putExtra("headerName", _profileInfosObj.getString("name"));
                        startActivity(sendMessageIntent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            addFriendBtn(profileObj);
            addBlockBtn(profileObj);
            contactsLL.setVisibility(View.GONE);

        }
    }

    /*
        Bouton pour blocker ou deblocker un contact
     */
    private void addBlockBtn(final JSONObject profileObj) throws Exception {
        Button blockProfileBtn = (Button)findViewById(R.id.button_block_com_profile_my_profile);
        blockProfileBtn.setVisibility(View.VISIBLE);
        if (profileObj.getString("i_blocked").equals("1")) {
            blockProfileBtn.setText(getString(R.string.PROFILE_BUTTON_UNBLOCK));
            blockProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.list_block_on), null);
        }
        else {
            blockProfileBtn.setText(getString(R.string.PROFILE_BUTTON_BLOCK));
            blockProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.list_block_off), null);
        }
        blockProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    _progressDialog.show();
                    List<NameValuePair> _args = new ArrayList<NameValuePair>();
                    _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                    _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                    _args.add(new BasicNameValuePair("id", profileObj.getString("id")));
                    if (profileObj.getString("i_blocked").equals("1"))
                        _args.add(new BasicNameValuePair("removeblock", "1"));
                    else
                        _args.add(new BasicNameValuePair("addblock", "1"));
                    HttpApiCall apiCaller = new HttpApiCall(MyProfile.this, _args, 2);
                    String url = Tools.API + Tools.CDCONTACTSEDIT;
                    apiCaller.execute(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
        Bouton pour ajouter ou supprimer la personne de mes contacts
     */
    private void addFriendBtn(final JSONObject profileObj) throws Exception {
        Button addFriendBtn = (Button)findViewById(R.id.button_addfriend_com_profile_my_profile);
        addFriendBtn.setVisibility(View.VISIBLE);
        if (profileObj.getString("i_friended").equals("1")) {
            addFriendBtn.setText(getString(R.string.PROFILE_BUTTON_UNFRIEND));
            addFriendBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_listContactImdId), null);
        }
        else {
            addFriendBtn.setText(getString(R.string.PROFILE_BUTTON_FRIEND));
            addFriendBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.list_contact_off), null);
        }
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    _progressDialog.show();
                    List<NameValuePair> _args = new ArrayList<NameValuePair>();
                    _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                    _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                    _args.add(new BasicNameValuePair("id", profileObj.getString("id")));
                    if (profileObj.getString("i_friended").equals("1"))
                        _args.add(new BasicNameValuePair("removefriend", "1"));
                    else
                        _args.add(new BasicNameValuePair("addfriend", "1"));
                    HttpApiCall apiCaller = new HttpApiCall(MyProfile.this, _args, 2);
                    String url = Tools.API + Tools.CDCONTACTSEDIT;
                    apiCaller.execute(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void setProfileOnClickListernersWithApiInfos(final JSONObject profileObj) throws Exception {
        Button myPrayersBtn = (Button)findViewById(R.id.button_my_prayers_com_profile_my_profile);
        String myPrayersString;
        if (_myProfile) {
            myPrayersString = getString(R.string.PROFILE_BUTTON_ALL_MY_PRAYERS);
            myPrayersString += " (" + UserConnected.getInstance().get_num_prayers() + ")";
        }
        else {
            myPrayersString = getString(R.string.PROFILE_BUTTON_ALL_HIS_PRAYERS);
            myPrayersString += " (" + String.valueOf(profileObj.getString("num_prayers")) + ")";
        }
        myPrayersBtn.setText(myPrayersString);
        myPrayersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent prayersIntent = new Intent(MyProfile.this, MyPrayers.class);
                prayersIntent.putExtra("resId", _resId);
                try {
                    if (!_myProfile)
                        prayersIntent.putExtra("userId", profileObj.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(prayersIntent, MYPRAYERS);
            }
        });
        Button myClassifiedsBtn = (Button)findViewById(R.id.button_my_classified_com_profile_my_profile);
        String myClassifiedsString;
        if (_myProfile) {
            myClassifiedsString = getString(R.string.PROFILE_BUTTON_ALL_MY_CLASSIFIEDS);
            myClassifiedsString += " (" + UserConnected.getInstance().get_num_classifieds() + ")";
        }
        else {
            myClassifiedsString = getString(R.string.PROFILE_BUTTON_ALL_HIS_CLASSIFIEDS);
            myClassifiedsString += " (" + String.valueOf(profileObj.getString("num_classifieds")) + ")";
        }
        myClassifiedsBtn.setText(myClassifiedsString);
        myClassifiedsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent prayersIntent = new Intent(MyProfile.this, MyClassifieds.class);
                prayersIntent.putExtra("resId", _resId);
                try {
                    if (!_myProfile)
                        prayersIntent.putExtra("userId", profileObj.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(prayersIntent, MYCLASSIFIEDS);
            }
        });
    }

    private void displayMyActivities() {
        TextView myActivitiesTV = (TextView)findViewById(R.id.textView_myactivities_com_profile);
        myActivitiesTV.setVisibility(View.VISIBLE);
        LinearLayout myActivitiesLL = (LinearLayout)findViewById(R.id.linearLayout_myactivities_com_profile);
        if (myActivitiesLL != null) {
            myActivitiesLL.removeAllViews();
            myActivitiesLL.setVisibility(View.VISIBLE);
            try {
                for (int i = 0; i < _updates.length(); ++i) {
                    JSONObject updateJO = _updates.getJSONObject(i);
                    addPosted(updateJO, myActivitiesLL);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPosted(final JSONObject updateJO, LinearLayout myActivitiesLL) {
        LinearLayout prayerLL = new LinearLayout(this);
        prayerLL.setOrientation(LinearLayout.HORIZONTAL);
        TextView infosTV = new TextView(this);
        try {
            String text = "<font color='#989898' size=12>";
            if (updateJO.getString("type").equals("posted-prayer")) {
                text += getString(R.string.VIEW_TITLE_PRAYER) + ", ";
            }
            else if (updateJO.getString("type").equals("posted-classified")) {
                text += getString(R.string.UPDATE_POSTED_CLASSIFIED) + ", ";
            }
            text += updateJO.getString("dateinfo") + "</font><br/>";
            text += updateJO.getString("text");
            infosTV.setText(Html.fromHtml(text));
            infosTV.setTextSize(15);
            infosTV.setTextColor(getResources().getColor(R.color.black));
            prayerLL.addView(infosTV);
            prayerLL.setBackgroundColor(getResources().getColor(R.color.white));
            prayerLL.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams prayerLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            prayerLP.setMargins(0, 1, 0, 1);
            //View separatorView = new View(this);
            //separatorView.setBackgroundColor(getResources().getColor(R.color.grisSeparator));
            if (updateJO.getString("type").equals("posted-prayer")) {
                //Log.d("MyProfile::prayer::id::", updateJO.getString("id"));
                prayerLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent prayerIntent = new Intent(MyProfile.this, Prayer.class);
                        prayerIntent.putExtra("resId", _resId);
                        prayerIntent.putExtra("prayer", updateJO.toString());
                        startActivity(prayerIntent);
                    }
                });
            }
            else if (updateJO.getString("type").equals("posted-status")) {
                prayerLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent statusIntent = new Intent(MyProfile.this, DisplayStatus.class);
                        statusIntent.putExtra("resId", _resId);
                        try {
                            statusIntent.putExtra("statusId", updateJO.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivityForResult(statusIntent, STATUS);
                    }
                });
            }
            else if (updateJO.getString("type").equals("posted-classified")) {

            }
            myActivitiesLL.addView(prayerLL, prayerLP);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_PROFILE) {
            if (resultCode == RESULT_OK) {
                String results = data.getStringExtra("changes");
                if (results != null && results.equals("yes")) {
                    if (Tools.CDDEBUG)
                        Log.d("MyProfile::onActivityResult::result::", results);
                    UserConnected.getInstance().set_profilePic(null);
                    getProfileFromApi();
                }
            }
        }
        else if (requestCode == MYPRAYERS) {
            if (resultCode == RESULT_OK) {
                if (Tools.CDDEBUG)
                    Log.d("MyProfile::onActivityResult::MyPrayers::", "changes...");
                //TODO RELOAD TEMPLATE
            }
            else {
                if (Tools.CDDEBUG)
                    Log.d("MyProfile::onActivityResult::MyPrayers::", "no changes...");
            }
        }
        else if (requestCode == STATUS) {
            if (resultCode == RESULT_OK) {
                if (data.hasExtra("reload") && data.getStringExtra("reload").equals("yes")) {
                    if (Tools.CDDEBUG)
                        Log.d("MyProfile::onActivityResult::STATUS::", "reload");
                    //TODO RELOAD INFOS FROM API
                    displayMyActivities();
                }
            }
        }
    }


    /**
     * Télécharge le profil depuis l'API
     */
    private void getProfileFromApi() {

        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("id", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));

        /*HttpApiCall apiCaller = new HttpApiCall(this, UserConnected.getInstance().get_uid(), UserConnected.getInstance().get_sid());
        String url = Tools.API + Tools.CDPROFILESGET;
        if (Tools.CDDEBUG)
            Log.d("MyProfile::getProfileFromApi::Url::", url);
        apiCaller.execute(url);
        */

        String url = Tools.API + Tools.CDPROFILESGET;
        HttpApiCall apiCaller = new HttpApiCall(this, _args, 1);
        apiCaller.execute(url);

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        _progressDialog.show();
    }

    public void getProfileResult(String result, int type) throws Exception {
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        JSONObject profileObj = new JSONObject(result);
        if (type == 1) {
            if (profileObj.has("ok") && profileObj.getString("ok").equals("1") &&
                    (_myProfile || profileObj.getString("is_me").equals("1"))) {
                if (Tools.CDDEBUG)
                    Log.d("MyProfile::getProfileResult::", "My profile !!");
                _myProfile = true;
                setUserProfileInfos(profileObj);
            }
            displayProfile(profileObj);
        }
        else if (type == 2) {
            if (_myProfile)
                downloadProfileOfProfileId(UserConnected.getInstance().get_uid());
            else
                downloadProfileOfProfileId(_profileInfosObj.getString("id"));
        }
    }

    private void setUserProfileInfos(JSONObject profileObj) throws Exception {
        UserConnected.getInstance().set_firstName(profileObj.getString("first_name"));
        UserConnected.getInstance().set_lastName(profileObj.getString("last_name"));
        if (profileObj.has("age"))
            UserConnected.getInstance().set_age(profileObj.getString("age"));
        if (profileObj.getString("gender").equals("M"))
            UserConnected.getInstance().set_gender(UserConnected.GENDER_M);
        else
            UserConnected.getInstance().set_gender(UserConnected.GENDER_F);
        UserConnected.getInstance().set_birthDate(profileObj.getString("date_birth"));
        UserConnected.getInstance().set_selfportrait(profileObj.getString("selfportrait"));
        if (Tools.CDDEBUG)
            Log.d("MyProfile::setUserProfileInfos::", profileObj.getString("num_prayers"));
        UserConnected.getInstance().set_num_prayers(Integer.parseInt(profileObj.getString("num_prayers")));
        UserConnected.getInstance().set_num_classifieds(Integer.parseInt(profileObj.getString("num_classifieds")));
        UserConnected.getInstance().set_numUnreadMessages(0);
    }

    private void displayNameAndAge() throws JSONException {
        TextView nameAndAgeTV = (TextView)findViewById(R.id.textView_profile_infos_com_profile_my_profile);
        if (nameAndAgeTV != null) {
            String text;
            String age = "";
            if (_myProfile && UserConnected.getInstance().get_age() != null)
                age = getString(R.string.PROFILE_INFO_AGE, Integer.parseInt(UserConnected.getInstance().get_age()));
            else if (!_myProfile && !_profileInfosObj.getString("age").equals("null"))
                age = getString(R.string.PROFILE_INFO_AGE, _profileInfosObj.getInt("age"));
            if (!_myProfile) {
                text = "<strong><p>" + _profileInfosObj.getString("first_name") + "<br/>" + _profileInfosObj.getString("last_name") + "</p></strong><p>" + age + "</p><p>" + _profileInfosObj.getString("selfportrait") + "</p>";
            }
            else
                text = "<strong><p>" + UserConnected.getInstance().get_firstName() + "<br/>" + UserConnected.getInstance().get_lastName() + "</p></strong><p>" + age + "</p><p>" + UserConnected.getInstance().get_selfportrait() + "</p>";
            if (Tools.CDDEBUG)
                Log.d("MyProfile::displayNameAndAge::text::", text);
            nameAndAgeTV.setText(Html.fromHtml(text));
            nameAndAgeTV.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void displayProfilePicture(String profileUrl) {

        ImageView profilePicIV = (ImageView)findViewById(R.id.imageView_profile_pic_com_profile_my_profile);
        Bitmap profilePicBitmap = UserConnected.getInstance().get_profilePic();
        int size = Tools.STD_W / 2;
        LinearLayout.LayoutParams profilePicLP = new LinearLayout.LayoutParams(size, size);
        profilePicIV.setLayoutParams(profilePicLP);
        if (profilePicBitmap == null || !_myProfile) {
            String url;
            if (_myProfile) {
                url = Tools.MEDIAROOT + UserConnected.getInstance().getPic();
                Log.e("displayProfilePicture::profileURL", url);
            }
            else {
                url = Tools.MEDIAROOT + profileUrl;
            }
            new DownloadImageTask(profilePicIV, true).execute(url);
        }
        else {
            if (Tools.CDDEBUG)
                Log.d("MyProfile::displayProfilePicture::", "Bitmap saved");
            profilePicIV.setImageBitmap(profilePicBitmap);
            profilePicIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _listContactImdId = R.drawable.list_contact_on;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _listContactImdId = R.drawable.list_contact_on_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _listContactImdId = R.drawable.list_contact_on_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _listContactImdId = R.drawable.list_contact_on_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _listContactImdId = R.drawable.list_contact_on_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _listContactImdId = R.drawable.list_contact_on_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _listContactImdId = R.drawable.list_contact_on_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _listContactImdId = R.drawable.list_contact_on_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _listContactImdId = R.drawable.list_contact_on_silver;
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
        Intent messagesIntent = new Intent(MyProfile.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }



    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (_progressDialog != null)
            _progressDialog.cancel();
        JSONObject profileObj = new JSONObject(result);
        if (type == 1) {
            if (profileObj.has("ok") && profileObj.getString("ok").equals("1") && (_myProfile || profileObj.getString("is_me").equals("1"))) {
                _myProfile = true;
                setUserProfileInfos(profileObj);
            }
            displayProfile(profileObj);
        }
    }
}