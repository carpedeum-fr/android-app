package com.Network;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.Tools.Tools;
import com.prayers.MyPrayers;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Guillaume on 21/11/2014.
 *
 * Pool chargé d'ordonnancer les requetes HTTP à l'API (gestion des priorités)
 */

public class PoolRequetes extends Timer {

    public final static int REQUETE_IMAGE = 1;
    public final static int REQUETE_API = 2;

    public final static String INIT = "INIT";
    public final static String PENDING = "PENDING";
    public final static String FINISHED = "FINISHED";

    private boolean started = false;

    private final String TAG = "PoolRequetes";

    /**
     * Id auto incrémenté à charque nouvelle requête
     */
    private int id;

    /**
     * Object à locker pour les accès concurentiels
     */
    private final Object lock = new Object();



    /**
     * Définition d'une requete
     */
    private class Requete {

        private ImageView imageIV = null;
        private boolean crop = false;
        private int type = 0;
        private String url = null;
        private int size = 0;

        private ApiCaller callerInstance = null;
        private ArrayList<NameValuePair> args = null;

        // Id unique de requête
        private int idR = 0;

        public int getId() {
            return this.idR;
        }

        private int typeRequete = 0;

        private DownloadImages dlImage = null;
        private HttpApiCall apiCaller = null;

        public Requete(ApiCaller instance, ArrayList<NameValuePair> args, String url, int type) {

            this.callerInstance = instance;
            this.args = args;
            this.type = type;
            this.idR = id;
            this.url = url;
            this.typeRequete = REQUETE_API;
            apiCaller = new HttpApiCall(this.callerInstance, this.args, this.type);
        }

        public Requete(ImageView profileIV, boolean crop, String url, int size) {

            this.imageIV = profileIV;
            this.crop = crop;
            this.url = url;
            this.size = size;
            this.idR = id;
            this.typeRequete = REQUETE_IMAGE;
            dlImage = new DownloadImages(this.imageIV, crop, url + size);
        }

        public void executer() {

            if (typeRequete == REQUETE_IMAGE) {

                dlImage.setStatut(DownloadImages.PENDING);
                dlImage.execute(Tools.MEDIAROOT + url, String.valueOf(size));

            }
            else {

                apiCaller.setStatut(DownloadImages.PENDING);
                apiCaller.execute(this.url);
            }

        }

        public String getStatut() {

            if (typeRequete == REQUETE_IMAGE) {
                return dlImage.getStatut();
            }
            return apiCaller.getStatut();
        }
    }

    private ArrayList<Requete> listeRequetes = null;


    /**
     * Timer exécuter à intervalle régulier
     * Chargé d'éxécuter les requêtes
     */
    private class MyTimerTask extends TimerTask {
        public void run() {

            synchronized (lock) {

                if (listeRequetes.size() > 0) {
                    Requete firstReq = listeRequetes.get(0);
                    //Log.d(TAG, "1: Id de la requête : " + firstReq.getId() + " - statut : " + firstReq.getStatut());

                    if (firstReq.getStatut().equals(INIT)) {
                        firstReq.executer();
                    }
                    else if (firstReq.getStatut().equals(FINISHED)) {
                        listeRequetes.remove(0);
                    }


                }


                //


                    //firstReq.executer();

                    //Log.d(TAG, "2: Id de la requête : " + firstReq.getId() + " - statut : " + firstReq.getStatut());

                    //listeRequetes.remove(0);
                //}
            }

        }
    }

    private MyTimerTask myTask = null;
    private Timer myTimer = null;

    private PoolRequetes() {

        listeRequetes = new ArrayList<>();
        myTask = new MyTimerTask();
        myTimer = new Timer();
        id = 0;
    }

    private static PoolRequetes INSTANCE = null;

    public static synchronized PoolRequetes getInstance()
    {
        if (INSTANCE == null) {
            INSTANCE = new PoolRequetes();
        }
        return INSTANCE;
    }

    public void ajouterNouvelleRequete(ApiCaller instance,  ArrayList<NameValuePair> args, String url, int type) {

        synchronized (lock) {

            listeRequetes.add(0, new Requete(instance, args, url, type));
            ++id;
        }

    }

    public void ajouterNouvelleRequeteEnFin(ApiCaller instance, ArrayList<NameValuePair> args, String url, int type) {

        synchronized (lock) {

            listeRequetes.add(new Requete(instance, args, url, type));
            ++id;
        }
    }

    public void ajouterNouvelleRequeteImage(ImageView profileIV, boolean crop, String url, int size) {

        synchronized (lock) {

            listeRequetes.add(new Requete(profileIV, crop, url, size));
            ++id;

        }

    }

    public void start() {
        if (!started) {
            myTimer.schedule(myTask, 1000, 100);
            started = true;
        }
    }
}
