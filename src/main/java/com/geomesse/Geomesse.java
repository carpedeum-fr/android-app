package com.geomesse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.GPSTracker;
import com.Network.HttpApiCall;
import com.Network.PoolRequetes;
import com.Tools.ImageCache;
import com.Tools.LogInFile;
import com.Tools.Tools;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.i2heaven.carpedeum.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.messages.DisplayMessages;
import com.places.DisplayPlace;
import com.user.UserConnected;
import com.google.android.gms.location.LocationListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Guillaume on 27/08/13.
 * Geomesse
 */

public class Geomesse extends FragmentActivity implements ApiCaller, OnMapReadyCallback {
    private int _resId = -1;
    private int _drawableArrowID = -1;
    private LinearLayout _allPlaces = null;
//    private LocationClient mLocationClient;
    private GoogleMap mMap;
    private Marker _myMarker = null;
    private JSONArray _placesArray = null;
    private String TAG = "Geomesse";
    private ProgressDialog _progressDialog = null;
    private double _myLat = 0.f;
    private double _myLng = 0.f;
    private Location _myLocation = null;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;
    private boolean _mapDisplayed = false;
    private boolean _abcSearch = false;
    private boolean _geoSearch = false;

    private final Object lock = new Object();
    private boolean located = false;

    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(15000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private static final int REQUEST_LOCATION = 112;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogInFile.getInstance().WriteLog("Geomesse::onCreate", true);

        if (!checkGoogleServices()) {
            Log.d(TAG, "Geomesse::onCreate::checkGoogleServices failed");
            LogInFile.getInstance().WriteLog("Geomesse::onCreate::checkGoogleServices failed", true);
            //TODO message erreur Services Gplay non presents
            finish();
        }
        else {
            LogInFile.getInstance().WriteLog("Geomesse::onCreate::checkGoogleServices ok", true);

            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
            setContentView(R.layout.com_geomesse);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                _resId = extras.getInt("resId");
                setLayoutTheme();
            }
            // Logo Géomesse
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.GONE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.Geomesse));

            try {

                if (!canAccessLocation()) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION);
                }
                else {
                    setUpMapIfNeeded();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION:
                if (canAccessLocation()) {
                    setUpMapIfNeeded();
                }
                break;
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    private void initGeomesse() throws Exception {
        //Toast.makeText(this, "Init Géomesse", Toast.LENGTH_LONG).show();

        _progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.ChargementEnCours));
        //_progressDialog.show();
        setButtonsOnClickListeners();

        // On souhaite afficher la carte en arrivant
        _mapDisplayed = true;

        startLocationListener();
    }


    @Override
    protected void onResume() {
        super.onResume();

        try {
            initGeomesse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GPSTracker gps;

    private void startLocationListener() throws Exception {
        gps = new GPSTracker(Geomesse.this);

        // check if GPS enabled
        if (gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            LogInFile.getInstance().WriteLog("Located : " + located, true);
            //Toast.makeText(this, "GPS : " + latitude + " - " + longitude, Toast.LENGTH_SHORT).show();

            goToMyLocation(gps.getLocation());
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void setButtonsOnClickListeners() {
        final ScrollView placeSV = (ScrollView)findViewById(R.id.scrollView_places_com_geomesse);
        final LinearLayout mapView = (LinearLayout)findViewById(R.id.linearLayout_map_com_geomesse);
        final TextView createPlaceTV = (TextView)findViewById(R.id.textView_create_place_com_geomesse);

        final TextView mapBtn = (TextView)findViewById(R.id.button_display_map_com_geomesse_menu);
        final TextView listBtn = (TextView)findViewById(R.id.button_display_list_com_geomesse_menu);

        createPlaceTV.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(_drawableArrowID), null);
        createPlaceTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent createPlaceIntent = new Intent(Geomesse.this, CreatePlace.class);
                    createPlaceIntent.putExtra("resId", _resId);
                    startActivity(createPlaceIntent);
                }
                else {
                    //TODO message erreur
                }
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_placesArray != null) {
                    _mapDisplayed = true;
                    try {
                        mapBtn.setTextColor(getResources().getColor(R.color.black));
                        mapBtn.setBackgroundResource(R.drawable.segment_on_single);
                        listBtn.setTextColor(getResources().getColor(R.color.white));
                        listBtn.setBackgroundColor(getResources().getColor(R.color.blackTransparentMessages));
                        mapView.setVisibility(View.VISIBLE);
                        placeSV.setVisibility(View.GONE);
                        displayMap();
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogInFile.getInstance().WriteLog(TAG + " -> " + e.getMessage(), true);
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), getString(R.string.LIST_LOADING), Toast.LENGTH_SHORT).show();
                }
            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _mapDisplayed = false;
                listBtn.setTextColor(getResources().getColor(R.color.black));
                listBtn.setBackgroundResource(R.drawable.segment_on_single);
                mapBtn.setTextColor(getResources().getColor(R.color.white));
                mapBtn.setBackgroundColor(getResources().getColor(R.color.blackTransparentMessages));
                mapView.setVisibility(View.GONE);
                placeSV.setVisibility(View.VISIBLE);
                createPlaceTV.setVisibility(View.VISIBLE);

                if (_placesArray != null) {

                    _allPlaces = (LinearLayout)findViewById(R.id.linearLayout_places_com_geomesse);
                    _allPlaces.removeAllViews();

                    try {
                        for (int i = 0; i < _placesArray.length(); ++i) {
                            addPlaceToListing(_placesArray.getJSONObject(i));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        final ImageView cancelAbcSIV = (ImageView)findViewById(R.id.imageView_removeabc_com_geomesse);
        ImageButton searchAbcIB = (ImageButton)findViewById(R.id.imageButton_search_abc_com_geomesse);
        searchAbcIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_abcSearch) {
                    _abcSearch = true;
                    cancelAbcSIV.setVisibility(View.VISIBLE);
                    showSearchDialog();
                }
                else {
                    _abcSearch = false;
                    downloadPlaces(null);
                    cancelAbcSIV.setVisibility(View.GONE);
                }
            }
        });

        final ImageView cancelGeoIV = (ImageView)findViewById(R.id.imageView_removeloc_com_geomesse);
        ImageButton searchGeoIB = (ImageButton)findViewById(R.id.imageButton_search_loc_com_geomesse);
        searchGeoIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!_geoSearch) {
                    _geoSearch = true;
                    cancelGeoIV.setVisibility(View.VISIBLE);
                    showLocDialog();
                }
                else {
                    _geoSearch = false;
                    downloadPlaces(null);
                    cancelGeoIV.setVisibility(View.GONE);
                }
            }
        });
    }

    private void displayMapOnScreen() {

        final ScrollView placeSV = (ScrollView)findViewById(R.id.scrollView_places_com_geomesse);
        final LinearLayout mapView = (LinearLayout)findViewById(R.id.linearLayout_map_com_geomesse);
        final TextView createPlaceTV = (TextView)findViewById(R.id.textView_create_place_com_geomesse);

        final TextView mapBtn = (TextView)findViewById(R.id.button_display_map_com_geomesse_menu);
        final TextView listBtn = (TextView)findViewById(R.id.button_display_list_com_geomesse_menu);

        mapBtn.setTextColor(getResources().getColor(R.color.black));
        mapBtn.setBackgroundResource(R.drawable.segment_on_single);
        listBtn.setTextColor(getResources().getColor(R.color.white));
        listBtn.setBackgroundColor(getResources().getColor(R.color.blackTransparentMessages));
        mapView.setVisibility(View.VISIBLE);
        placeSV.setVisibility(View.GONE);
        createPlaceTV.setVisibility(View.GONE);
    }

    private void geocodingAddr(String addr) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(addr, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            double lat = address.getLatitude();
            double lng = address.getLongitude();
            Log.d(TAG, "ADDR: " + lat + ", lng: " + lng);
            downloadPlaces(lat, lng);
        }
    }

    private void showLocDialog() {
        AlertDialog.Builder _locAlert = new AlertDialog.Builder(this);

        final EditText searchED = new EditText(this);
        searchED.setHint("Rechercher une adresse");
        _locAlert.setView(searchED);
        _locAlert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        _locAlert.setPositiveButton("Rechercher ici", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (searchED.getText().toString() != null && searchED.getText().toString().length() > 0) {
                    geocodingAddr(searchED.getText().toString());
                }
                dialog.cancel();
            }
        });
        _locAlert.show();
    }

    private void showSearchDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Geomesse");
        final EditText input = new EditText(this);
        input.setHint("Recherche par nom");
        alert.setView(input);
        alert.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.setPositiveButton("Rechercher", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString() != null && input.getText().toString().length() > 0) {
                    downloadPlaces(input.getText().toString());
                }
                dialog.cancel();
            }
        });
        alert.show();
    }

    private void displayMap() {
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog(TAG + "::displayMap", true);
        }
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog(TAG + "::displayMap::map setted", true);
        }
        if (_mapDisplayed) {
            if (Tools.CDDEBUG) {
                LogInFile.getInstance().WriteLog(TAG + "::displayMap::_mapDisplayed", true);
                LogInFile.getInstance().WriteLog(TAG + "_myLat::" + _myLat + " - _myLng:: " + _myLng, true);
            }
            LatLng latLng = new LatLng(_myLat, _myLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            _myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ma position"));
            if (Tools.CDDEBUG) {
                LogInFile.getInstance().WriteLog(TAG + "::displayMap::_myMarker setted", true);
            }
        }
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog(TAG + "::displayPlacesMarkers -> go", true);
        }
        try {
            displayPlacesMarkers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog(TAG + "::displayPlacesMarkers ok", true);
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override public boolean onMarkerClick(Marker marker) {
                Log.d("Marker click: ", marker.getTitle() + marker.getSnippet());
                if (!marker.isInfoWindowShown())
                    marker.showInfoWindow();
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent placeIntent = new Intent(Geomesse.this, DisplayPlace.class);
                placeIntent.putExtra("resId", _resId);
                placeIntent.putExtra("id", marker.getSnippet());
                startActivity(placeIntent);
            }
        });
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog(TAG + "::all ok !", true);
        }
    }

    private void displayPlacesMarkers() throws Exception {
        LatLng placeLatLng;
        for (int i = 0; i < _placesArray.length(); ++i) {
            JSONObject placeObj = _placesArray.getJSONObject(i);
            placeLatLng = new LatLng(placeObj.getDouble("lat"), placeObj.getDouble("lng"));
            mMap.addMarker(new MarkerOptions().position(placeLatLng).title(placeObj.getString("name")).snippet(placeObj.getString("id")).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_view_button_church)));
        }
    }

    private void goToMyLocation(Location myLocation) {
        if (_myMarker == null && _myLocation == null && myLocation != null) {
            _myLocation = myLocation;
            _myLat = myLocation.getLatitude();
            _myLng = myLocation.getLongitude();
            if (Tools.CDDEBUG) {
                Log.d(TAG, "my lat: " + _myLat);
                Log.d(TAG, "my lng: " + _myLng);
            }
            downloadPlaces(null);
        }
    }

    private void downloadPlaces(double lat, double lng) {
        if (internetConnectionOk()) {
            LogInFile.getInstance().WriteLog("Geomesse::downloadPlaces::internetConnectionOk", true);
            if (_args == null)
                _args = new ArrayList<>();
            _args.clear();
            _args.add(new BasicNameValuePair("lat", String.valueOf(lat)));
            _args.add(new BasicNameValuePair("lng", String.valueOf(lng)));
            _args.add(new BasicNameValuePair("limit", Tools.CDPLACELIMIT));
            _apiCaller = new HttpApiCall(this, _args, 1);
            if (Tools.CDDEBUG) LogInFile.getInstance().WriteLog("Geomesse::downloadPlaces::url::" + Tools.API + Tools.CDPLACESLIST + "::uid::" + UserConnected.getInstance().get_uid(), true);
            _apiCaller.execute(Tools.API + Tools.CDPLACESLIST);
            _apiCaller = null;
        }
    }

    private void downloadPlaces(String place) {
        if (internetConnectionOk()) {
            LogInFile.getInstance().WriteLog("Geomesse::downloadPlaces::internetConnectionOk", true);
            if (_args == null)
                _args = new ArrayList<>();
            _args.clear();
            if (place != null)
                _args.add(new BasicNameValuePair("q", place));
            _args.add(new BasicNameValuePair("lat", String.valueOf(_myLat)));
            _args.add(new BasicNameValuePair("lng", String.valueOf(_myLng)));
            _args.add(new BasicNameValuePair("limit", Tools.CDPLACELIMIT));
            _apiCaller = new HttpApiCall(this, _args, 1);
            if (Tools.CDDEBUG) LogInFile.getInstance().WriteLog("Geomesse::downloadPlaces::url::" + Tools.API + Tools.CDPLACESLIST + "::uid::" + UserConnected.getInstance().get_uid(), true);
            _apiCaller.execute(Tools.API + Tools.CDPLACESLIST);
            _apiCaller = null;
        }
    }

    private void addPlaceToListing(final JSONObject placeObj) throws Exception {


        View placeLayout = getLayoutInflater().inflate(R.layout.com_geomesse_place_list, null);
        assert placeLayout != null;

        ImageView placeIV = (ImageView)placeLayout.findViewById(R.id.imageView_placeimage_com_geomesse_place_list);
        setPlaceImage(placeIV, placeObj.getString("image"));

        TextView placeNameTV = (TextView)placeLayout.findViewById(R.id.textView_place_name_com_geomesse_place_list);
        placeNameTV.setText(placeObj.getString("name"));

        TextView placeInfoTV = (TextView)placeLayout.findViewById(R.id.textView_info_com_geomesse_place_list);
        placeInfoTV.setText(placeObj.getString("infos1"));

        // Intent pour montrer la fiche d'une église
        placeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getPlaceIntent = new Intent(Geomesse.this, DisplayPlace.class);
                getPlaceIntent.putExtra("resId", _resId);
                try {
                    getPlaceIntent.putExtra("id", placeObj.getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(getPlaceIntent);
            }
        });


        int size = Tools.STD_W / 6;

        RelativeLayout.LayoutParams layoutLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
        placeLayout.setLayoutParams(layoutLP);

        _allPlaces.addView(placeLayout);

        LinearLayout.LayoutParams layoutparamsSeparator = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        layoutparamsSeparator.setMargins(0, 10, 0, 10);
        View separatorView = new View(this);
        separatorView.setBackgroundResource(R.color.gris73);
        _allPlaces.addView(separatorView, layoutparamsSeparator);
    }

    private void setPlaceImage(ImageView placeIV, String picURL) {

        if (picURL != null && picURL.length() > 0) {
            int size = Tools.STD_W / 6;
            RelativeLayout.LayoutParams profileLP = new RelativeLayout.LayoutParams(size, size);
            placeIV.setLayoutParams(profileLP);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(picURL + size);
            if (cachedImage == null) {
                //new DownloadImages(profileIV, true, picURL + size).execute(Tools.MEDIAROOT + picURL, String.valueOf(size));
                PoolRequetes.getInstance().ajouterNouvelleRequeteImage(placeIV, true, picURL, size);
            } else {
                placeIV.setImageBitmap(cachedImage);
                placeIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
        }
    }

    private ImageView getPlaceImage(String picURL) {
        if (picURL != null && picURL.length() > 0) {
            ImageView profileIV = new ImageView(this);
            int size = Tools.STD_W / 7;
            LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
            profileIV.setLayoutParams(profileLP);
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(picURL + size);
            if (cachedImage == null) {
                //new DownloadImages(profileIV, true, picURL + size).execute(Tools.MEDIAROOT + picURL, String.valueOf(size));
                PoolRequetes.getInstance().ajouterNouvelleRequeteImage(profileIV, true, picURL, size);
            }
            else {
                profileIV.setImageBitmap(cachedImage);
                profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            return profileIV;
        }
        return null;
    }

    private boolean checkGoogleServices() {
        int resCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        LogInFile.getInstance().WriteLog("Geomesse::checkGoogleServices::versionCode::" + GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE, true);
        Log.d("Geomesse", "::checkGoogleServices::versionCode::" + GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE);

        if (resCode == ConnectionResult.SUCCESS){
            if (Tools.CDDEBUG) {
                Log.d("Geomesse::checkGoogle::", "res ok !");
                LogInFile.getInstance().WriteLog("Geomesse::checkGoogleServices::ok !", true);
            }
            return true;
        }
        if (Tools.CDDEBUG) {
            LogInFile.getInstance().WriteLog("Geomesse::checkGoogleServices:: KO !", true);
            Log.d("Geomesse::checkGoogle::", "res KO !");
        }
        return false;
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void setLayoutTheme() {
        if (_resId == 0 ||_resId == -1) {
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
    public void onApiResult(String result, int type) throws Exception {
        //mLocationClient.disconnect();
        if (_progressDialog != null && _progressDialog.isShowing())
            _progressDialog.cancel();
        //if (Tools.CDDEBUG)
            //LogInFile.getInstance().WriteLog("Geomesse::onApiResult", true);
        if (result != null && type == 1) {
            if (Tools.CDDEBUG) {
                Log.d(TAG, "result: " + result);
                //LogInFile.getInstance().WriteLog("Geomesse::onApiResult::result::" + result, true);
            }
            JSONObject resObj = new JSONObject(result);
            if (resObj.has("ok")) {
                if (!_mapDisplayed) {
                    _allPlaces = (LinearLayout) findViewById(R.id.linearLayout_places_com_geomesse);
                    _allPlaces.removeAllViews();
                }
                TextView messageTV = (TextView)findViewById(R.id.textView_noresult_com_geomesse);
                if (resObj.has("message") && !_mapDisplayed) {
                    messageTV.setText(resObj.getString("message"));
                    messageTV.setVisibility(View.VISIBLE);
                }
                else {
                    if (!_mapDisplayed) {
                        messageTV.setVisibility(View.GONE);
                    }
                    if (Tools.CDDEBUG) {
                        Log.d("Geomesse::places::", resObj.getString("results"));
                        //LogInFile.getInstance().WriteLog("Geomesse::places::" + result, true);
                    }
                    _placesArray = resObj.getJSONArray("results");

                    if (_mapDisplayed) {
                        /**
                         * Affiche les marqueurs sur la carte
                         */
                        displayMapOnScreen();
                        displayMap();
                        displayPlacesMarkers();
                    }
                    else {
                        /**
                         * Ajoute chaque église à la liste des eglises
                         */
                        for (int i = 0; i < _placesArray.length(); ++i) {
                            addPlaceToListing(_placesArray.getJSONObject(i));
                        }
                    }
                }
            }
        }
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(Geomesse.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            initGeomesse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapfragment_com_geomesse);
            mapFrag.getMapAsync(this);
        }
    }
}