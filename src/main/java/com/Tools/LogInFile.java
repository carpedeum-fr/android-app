package com.Tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Guillaume on 30/09/13.
 * Log dans un fichier
 */

public class LogInFile {

    private static final LogInFile _instance = new LogInFile();
    public File _file = null;
    public File _root = null;

    private File _userDataFile = null;

    private String fileRep = "/CarpeDeum";

    private LogInFile() {


        _root = new File(Environment.getExternalStorageDirectory() + fileRep);

        if (!_root.exists()) {

            boolean created = _root.mkdir();

            Log.d("LogInFile", "Dir creation : " + created);

        }


        /*File fileWithinMyDir = new File(mydir, "myfile");
        FileOutputStream out = new FileOutputStream(fileWithinMyDir);
        */

        _file = new File(_root, "logCarpeDeum.txt");
        _userDataFile = new File(_root, "usrData");
        WriteLog("\r\n\r\n", false);
    }

    public static LogInFile getInstance() {
        return _instance;
    }

    @SuppressLint("SimpleDateFormat")
    public void WriteLog(String toWrite, boolean date) {
        try {
            if (_root.canWrite()) {

                long filesize = _file.length();
                filesize /= 1000;
                boolean init = false;

                FileWriter filewriter;

                if (filesize > 5000) {
                    filewriter = new FileWriter(_file, false);
                    init = true;
                }
                else {
                    filewriter = new FileWriter(_file, true);
                }
                BufferedWriter out = new BufferedWriter(filewriter);
                if (date) {
                    Date actuelle = new Date();
                    SimpleDateFormat s = new SimpleDateFormat("ddMMyyyy_HHmmss");
                    String dat = s.format(actuelle);
                    if (init) {
                        out.write("CLEAN FILE\r\n");
                    }
                    out.write(dat + ": " + toWrite + "\r\n");
                }
                else {
                    out.write(toWrite + "\r\n");
                }
                out.close();
            }
        } catch (IOException e) {
            Log.e("CarpeDeum", "Could not write file " + e.getMessage());
        }
    }

    public void saveCredentials(String email, String password) throws Exception {

        if (_root.canWrite()) {

            FileWriter filewriter = new FileWriter(_userDataFile, false);
            BufferedWriter out = new BufferedWriter(filewriter);

            String emailBase64 = Base64.encodeToString(email.getBytes(), Base64.DEFAULT);
            String passwordBase64 = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);

            out.write(email + ":" + passwordBase64);

            out.close();
        }
    }


    public void saveSession(String uid, String sid) throws Exception {
        if (_root.canWrite()) {

            FileWriter filewriter = new FileWriter(_userDataFile, true);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(uid + ":" + sid + "\r\n");

            out.close();
        }
    }

    public void saveJsonObj(String jsonObj) throws Exception {

        if (_root.canWrite()) {

            FileWriter filewriter = new FileWriter(_userDataFile, true);
            BufferedWriter out = new BufferedWriter(filewriter);

            out.write(jsonObj + "\r\n");

            out.close();
        }
    }
}
