package com.user;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TextView;

import com.Network.ApiCaller;
import com.Tools.LogInFile;
import com.carpedeum.MainActivity;
import com.i2heaven.carpedeum.R;

/**
 * Created by Guillaume on 23/08/13.
 * All infos of connected user
 */

public class UserConnected {

    public static int GENDER_M = 0;
    public static int GENDER_F = 1;

    private static final UserConnected _instance = new UserConnected();
    private boolean _isConnected = false;
    private String _sid = null;
    private String _uid = null;
    private String name = null;
    private String pic = null;
    private Bitmap _profilePic = null;
    private String _firstName = null;
    private String _lastName = null;
    private String _age = null;
    private int _gender = GENDER_M;
    private String _birthDate = null;
    private String _selfportrait = null;
    private int _num_prayers = 0;
    private int _num_classifieds = 0;
    private int _num_candles = -1;
    private int _num_visited = -1;
    private boolean _gps = false;
    private boolean _online = false;
    private String _geolat = null;
    private String _geolng = null;
    private Integer _numUnreadMessages = 0;
    private String _jsonObj = null;
    private boolean _premium = false;
    private String _password = "";
    private String _mail;

    public static UserConnected getInstance() {
        return _instance;
    }

    public void setUserConnected(boolean connected) {
        _isConnected = connected;
    }

    public boolean IsUserConnected() {
        return _isConnected;
    }

    public String get_sid() {
        return _sid;
    }

    public void set_sid(String _sid) {
        this._sid = _sid;
    }

    public String get_uid() {
        return _uid;
    }

    public void set_uid(String _uid) {
        this._uid = _uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Bitmap get_profilePic() {
        return _profilePic;
    }

    public void set_profilePic(Bitmap _profilePic) {
        this._profilePic = _profilePic;
    }

    public String get_firstName() {
        return _firstName;
    }

    public void set_firstName(String _firstName) {
        this._firstName = _firstName;
    }

    public String get_lastName() {
        return _lastName;
    }

    public void set_lastName(String _lastName) {
        this._lastName = _lastName;
    }

    public String get_age() {
        return _age;
    }

    public void set_age(String _age) {
        this._age = _age;
    }

    public int get_gender() {
        return _gender;
    }

    public void set_gender(int _gender) {
        this._gender = _gender;
    }

    public String get_birthDate() {
        return _birthDate;
    }

    public void set_birthDate(String _birthDate) {
        this._birthDate = _birthDate;
    }

    public String get_selfportrait() {
        return _selfportrait;
    }

    public void set_selfportrait(String _selfportrait) {
        this._selfportrait = _selfportrait;
    }

    public int get_num_prayers() {
        return _num_prayers;
    }

    public void set_num_prayers(int _num_prayers) {
        this._num_prayers = _num_prayers;
    }

    public int get_num_classifieds() {
        return _num_classifieds;
    }

    public void set_num_classifieds(int _num_classifieds) {
        this._num_classifieds = _num_classifieds;
    }

    public boolean is_online() {
        return _online;
    }

    public void set_online(boolean _online) {
        this._online = _online;
    }

    public String get_geolat() {
        return _geolat;
    }

    public void set_geolat(String _geolat) {
        this._geolat = _geolat;
    }

    public String get_geolng() {
        return _geolng;
    }

    public void set_geolng(String _geolng) {
        this._geolng = _geolng;
    }

    public Integer get_numUnreadMessages() {
        return _numUnreadMessages;
    }

    public void set_numUnreadMessages(Integer _numUnreadMessages) {
        this._numUnreadMessages = _numUnreadMessages;
    }

    public void disconnectUser() {
        _isConnected = false;
        _sid = null;
        _uid = null;
        name = null;
        pic = null;
        _profilePic = null;
        _firstName = null;
        _lastName = null;
        _age = null;
        _gender = GENDER_M;
        _birthDate = null;
        _selfportrait = null;
        _num_prayers = 0;
        _num_classifieds = 0;
        _gps = false;
        _online = false;
        _geolat = null;
        _geolng = null;
        _numUnreadMessages = 0;
        _num_candles = 0;
        _premium = false;
        _num_visited = -1;
    }

    public int get_num_candles() {
        return _num_candles;
    }

    public void set_num_candles(int _num_candles) {
        this._num_candles = _num_candles;
    }

    public String get_jsonObj() {
        return _jsonObj;
    }

    public void set_jsonObj(String _jsonObj) {
        this._jsonObj = _jsonObj;
    }

    public boolean is_premium() {
        return _premium;
    }

    public void set_premium(boolean _premium) {
        this._premium = _premium;
    }

    public int get_num_visited() {
        return _num_visited;
    }

    public void set_num_visited(int num_visited) {
        this._num_visited = num_visited;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public String get_password() {
        return _password;
    }

    public String get_mail() {
        return _mail;
    }

    public void set_mail(String _mail) {
        this._mail = _mail;
    }

    public void saveSession(String uid, String sid) throws Exception {
        LogInFile.getInstance().saveSession(uid, sid);
    }

    public void saveCredentials(String email, String password) throws Exception {
        LogInFile.getInstance().saveCredentials(email, password);
    }

    public void saveJsonObj(String jsonObj) throws Exception {
        LogInFile.getInstance().saveJsonObj(jsonObj);
    }
}
