package com.voice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.Network.DownloadImages;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;

import java.io.IOException;

/**
 * Created by Guillaume on 27/08/13.
 * Listen God Voice
 */

public class ListenGodVoice extends Activity {

    private Boolean _isPLAYING = false;
    private MediaPlayer _mp = null;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.com_voice_godvoice_evangile_listen);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("audioURL")) {
            try {
                listenGodVoice(extras.getString("audioURL"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            setBackgroundImage(extras.getString("imgURL"));
        }
    }

    /**
     * Afficher image de fond
     *
     * @param imgUrl
     */
    private void setBackgroundImage(String imgUrl) {
        ImageView GodVoiceIV = (ImageView)findViewById(R.id.imageView_godVoicePic_com_voice_godvoice_evangile_listen);
        Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imgUrl + getScreenWidth());
        if (cachedImage == null) {
            new DownloadImages(GodVoiceIV, true, imgUrl + getScreenWidth()).execute(Tools.MEDIAROOT + imgUrl, String.valueOf(getScreenWidth()));
        }
        else {
            GodVoiceIV.setImageBitmap(cachedImage);
            GodVoiceIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    /**
     * Lancer la lecture de l'évangile
     *
     * @param url
     */
    private void listenGodVoice(final String url) throws Exception {
        Log.d("URL", url);

        ImageView controlsIV = (ImageView)findViewById(R.id.imageView_controls_com_voice_godvoice_evangile_listen);
        controlsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });
        if (!_isPLAYING) {
            _isPLAYING = true;
            _mp = new MediaPlayer();
            controlsIV.setImageResource(R.drawable.audio_player_pause);
            _mp.setDataSource(url);
            _mp.prepare();
            _mp.start();

            final SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar_listen_com_voice_godvoice_evangile_listen);
            int duration = _mp.getDuration();
            seekBar.setMax(duration);

            mRunnable = new Runnable() {

                @Override
                public void run() {
                    if (_mp != null){
                        int mCurrentPosition = _mp.getCurrentPosition();
                        seekBar.setProgress(mCurrentPosition);
                    }
                    mHandler.postDelayed(this, 1000);
                }
            };


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (_mp != null && fromUser){
                        _mp.seekTo(progress);
                    }
                }
            });

            mRunnable.run();

        } else {
            controlsIV.setImageResource(R.drawable.audio_player_play);
            _isPLAYING = false;
            _mp.pause();
        }
    }

    private void playPause() {
        ImageView controlsIV = (ImageView)findViewById(R.id.imageView_controls_com_voice_godvoice_evangile_listen);
        if (_mp != null) {
            if (_isPLAYING) {
                _mp.pause();
                _isPLAYING = false;
                controlsIV.setImageResource(R.drawable.audio_player_play);
            }
            else {
                _mp.start();
                _isPLAYING = true;
                controlsIV.setImageResource(R.drawable.audio_player_pause);
            }
        }
    }

    private void stopPlaying() {
        _mp.release();
        _mp = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopPlaying();
        finish();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        playPause();
    }
}