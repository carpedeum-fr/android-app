package com.Tools;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Guillaume on 07/01/2015.
 *
 * Lis le fichier de préférences utilisateur pour récupérer sa session
 */


public class ReadUserFile {

    private static final ReadUserFile _instance = new ReadUserFile();
    public File _userDataFile = null;
    public File _root = null;
    private String fileRep = "/CarpeDeum";

    private ReadUserFile() {

        _root = new File(Environment.getExternalStorageDirectory() + fileRep);
        _userDataFile = new File(_root, "usrData");
    }

    public static ReadUserFile getInstance() {
        return _instance;
    }

    public String getUID() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(_userDataFile));
        br.readLine();

        String line = br.readLine();
        String uid = line.split(":")[0];

        return uid;
    }

    public String getSID() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(_userDataFile));
        br.readLine();

        String line = br.readLine();
        String sid = line.split(":")[1];
        System.out.println("sid" + sid);

        return sid;
    }

    public String getJsonObj() throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(_userDataFile));
        br.readLine();
        br.readLine();

        return br.readLine();

    }
}
