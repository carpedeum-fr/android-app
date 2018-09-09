package com.user;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.Network.ApiCaller;
import com.Network.HttpApiCall;
import com.Tools.Tools;
import com.carpedeum.MainActivity;
import com.i2heaven.carpedeum.R;
import com.messages.DisplayMessage;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Guillaume on 07/12/13.
 * Service pour ping et vérifier l'arrivée de nouveaux messages
 */

public class PingService extends IntentService implements ApiCaller {

    private static final String TAG = "PingService";
    private Handler _countHandler = new Handler();

    public PingService() {
        super("PingService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        runnable.run();
    }

    private Runnable runnable = new Runnable()
    {
        public void run()
        {
        if (UserConnected.getInstance().IsUserConnected()) {
            pingApi();
            countApi();
        }
        _countHandler.postDelayed(this, 50000);
        }
    };

    private void countApi() {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        String url = Tools.API + Tools.CDMESSAGESCOUNT;
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        HttpApiCall apiCaller = new HttpApiCall(PingService.this, _args, 1);
        apiCaller.execute(url);
    }

    private void pingApi() {
        List<NameValuePair> _args = new ArrayList<NameValuePair>();
        String url = Tools.API + Tools.CDACCOUNTPING;
        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        HttpApiCall apiCaller = new HttpApiCall(PingService.this, _args, 2);
        apiCaller.execute(url);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        JSONObject resObj = new JSONObject(result);
        if (type == 1 && resObj.has("num_unread")) {
            if (UserConnected.getInstance().get_numUnreadMessages() < resObj.getInt("num_unread")) {
                sendNotificationOfNewMessage();
            }
            UserConnected.getInstance().set_numUnreadMessages(resObj.getInt("num_unread"));
        }
    }

    private void sendNotificationOfNewMessage() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_little)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.notification_little))
                        .setContentTitle(getString(R.string.RECEIVED_NEWMESSAGE))
                        .setContentText(getString(R.string.RECEIVED_MESSAGE));
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
