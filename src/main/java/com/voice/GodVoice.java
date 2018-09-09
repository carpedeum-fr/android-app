package com.voice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Network.ApiCaller;
import com.Network.DownloadImageTask;
import com.Network.DownloadImages;
import com.Network.HttpApiCall;
import com.Tools.ImageCache;
import com.Tools.Tools;
import com.i2heaven.carpedeum.R;
import com.carpedeum.Today;
import com.comments.AddComment;
import com.messages.DisplayMessages;
import com.user.MyProfile;
import com.user.UserConnected;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created by COCH on 21/07/13.
 * Class pour la Parole de Dieu
 */
public class GodVoice extends Activity implements ApiCaller {

    private int _resId = -1;
    private String _GodVoiceSrc = null;
    private String _audioURL = null;
    private String _picture = null;
    private String _voice = null;
    private String _apiVoice = null;
    private String _credits = null;
    private String _date = null;
    private String _medText = null;
    private String _medTextCredits = null;
    private String _oration = null;
    private String _orationCredits = null;
    private static int _selected = 0;
    private String _readingTitle;
    private String _readingSubTitle;
    private String _reading;
    private String _readingCredits;
    private String _reading2Title;
    private String _reading2SubTitle;
    private String _reading2;
    private String _reading2Credits;
    private String _psalmTitle;
    private String _psalmSubTitle;
    private String _psalm;
    private String _psalmCredits;
    private int _drawableLikeID = -1;
    private List<NameValuePair> _args = new ArrayList<NameValuePair>();
    private HttpApiCall _apiCaller = null;
    private String TAG = "GodVoice";
    private static int COMMENT = 12;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.com_voice_godvoice_evangile);
        Intent myIntent = getIntent();
        Bundle extras = myIntent.getExtras();
        if (extras != null) {
            _resId = extras.getInt("resId");
            _GodVoiceSrc = extras.getString("GodVoiceSrc");
            _audioURL = extras.getString("GodVoiceAudioURL");
            _picture = extras.getString("GodVoicePic");
            _voice = extras.getString("GodVoice");
            _credits = extras.getString("GodVoiceCredits");
            _medText = extras.getString("MedTextGodVoice");
            _medTextCredits = extras.getString("MedTextCreditsGodVoice");
            _oration = extras.getString("Oration");
            _orationCredits = extras.getString("OrationCredits");
            _date = extras.getString("date");

            _readingTitle = extras.getString("readingTitle");
            _readingSubTitle = extras.getString("readingSubTitle");
            _reading = extras.getString("reading");
            _readingCredits = extras.getString("readingCredits");

            _reading2Title = extras.getString("reading2Title");
            _reading2SubTitle = extras.getString("reading2SubTitle");
            _reading2 = extras.getString("reading2");
            _reading2Credits = extras.getString("reading2Credits");

            _psalmTitle = extras.getString("psalmTitle");
            _psalmSubTitle = extras.getString("psalmSubTitle");
            _psalm = extras.getString("psalm");
            _psalmCredits = extras.getString("psalmCredits");

            if (_date == null || _date.equals("")) {
                _date = Today.getFormatedForDBCurrentDate();
            }
            setLayoutTheme();
            ImageView imageLogoIV = (ImageView)findViewById(R.id.imageView_headerLogo);
            imageLogoIV.setVisibility(View.INVISIBLE);
            TextView titleTV = (TextView)findViewById(R.id.textView_title_header);
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(getResources().getString(R.string.LaParoledeDieu));
            if (_selected == 0 ||_selected == 2) {
                displayEvangileContents();
            }
            else if (_selected == 1) {
                setContentView(R.layout.com_voice_godvoice_oraison);
                displayOraisonContents();
            }
            else if (_selected == 3) {
                setContentView(R.layout.com_voice_godvoice_meditation);
                displayMedContents();
            }
            else if (_selected == 4) {
                setContentView(R.layout.com_voice_godvoice_lecture);
                displayReadingContents();
            }
            else if (_selected == 5) {
                setContentView(R.layout.com_voice_godvoice_psaume);
                displayPsaumeContents();
            }
            setOnClickListeners();
        }
    }

    private void setOnClickListeners() {
        final Button oraisonBtn = (Button)findViewById(R.id.button_oraison_com_voice_godvoice);
        final Button evangileBtn = (Button)findViewById(R.id.button_evangile_com_voice_godvoice);
        final Button meditationBtn = (Button)findViewById(R.id.button_meditation_com_voice_godvoice);
        final Button lectureBtn = (Button)findViewById(R.id.button_lecture_com_voice_godvoice);
        final Button psaumeBtn = (Button)findViewById(R.id.button_psaume_com_voice_godvoice);
        oraisonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.com_voice_godvoice_oraison);
                _selected = 1;
                setOnClickListeners();
                displayOraisonContents();
            }
        });
        evangileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.com_voice_godvoice_evangile);
                _selected = 2;
                setOnClickListeners();
                displayEvangileContents();
            }
        });
        meditationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.com_voice_godvoice_meditation);
                _selected = 3;
                setOnClickListeners();
                displayMedContents();
            }
        });
        lectureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.com_voice_godvoice_lecture);
                _selected = 4;
                setOnClickListeners();
                displayReadingContents();
            }
        });
        psaumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.com_voice_godvoice_psaume);
                _selected = 5;
                setOnClickListeners();
                displayPsaumeContents();
            }
        });

        if (_selected == 1) {
            oraisonBtn.setBackgroundResource(R.drawable.segment_on_single);
            lectureBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            psaumeBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            evangileBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            meditationBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);

            oraisonBtn.setTextColor(getResources().getColor(R.color.black));
            lectureBtn.setTextColor(getResources().getColor(R.color.white));
            psaumeBtn.setTextColor(getResources().getColor(R.color.white));
            evangileBtn.setTextColor(getResources().getColor(R.color.white));
            meditationBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 4) {
            oraisonBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            lectureBtn.setBackgroundResource(R.drawable.segment_on_single);
            psaumeBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            evangileBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            meditationBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);

            oraisonBtn.setTextColor(getResources().getColor(R.color.white));
            lectureBtn.setTextColor(getResources().getColor(R.color.black));
            psaumeBtn.setTextColor(getResources().getColor(R.color.white));
            evangileBtn.setTextColor(getResources().getColor(R.color.white));
            meditationBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 5) {
            oraisonBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            lectureBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            psaumeBtn.setBackgroundResource(R.drawable.segment_on_single);
            evangileBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            meditationBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);

            oraisonBtn.setTextColor(getResources().getColor(R.color.white));
            lectureBtn.setTextColor(getResources().getColor(R.color.white));
            psaumeBtn.setTextColor(getResources().getColor(R.color.black));
            evangileBtn.setTextColor(getResources().getColor(R.color.white));
            meditationBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 2) {
            oraisonBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            lectureBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            psaumeBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            evangileBtn.setBackgroundResource(R.drawable.segment_on_single);
            meditationBtn.setBackgroundResource(R.drawable.roundedcornergristransparentright);

            oraisonBtn.setTextColor(getResources().getColor(R.color.white));
            lectureBtn.setTextColor(getResources().getColor(R.color.white));
            psaumeBtn.setTextColor(getResources().getColor(R.color.white));
            evangileBtn.setTextColor(getResources().getColor(R.color.black));
            meditationBtn.setTextColor(getResources().getColor(R.color.white));
        }
        else if (_selected == 3) {
            oraisonBtn.setBackgroundResource(R.drawable.roundedcornergristransparentleft);
            lectureBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            psaumeBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            evangileBtn.setBackgroundColor(getResources().getColor(R.color.grisTransparent));
            meditationBtn.setBackgroundResource(R.drawable.segment_on_single);

            oraisonBtn.setTextColor(getResources().getColor(R.color.white));
            lectureBtn.setTextColor(getResources().getColor(R.color.white));
            psaumeBtn.setTextColor(getResources().getColor(R.color.white));
            evangileBtn.setTextColor(getResources().getColor(R.color.white));
            meditationBtn.setTextColor(getResources().getColor(R.color.black));
        }

    }

    private void displayPsaumeContents() {
        TextView psalmTitleTV = (TextView)findViewById(R.id.textView_title_com_voice_godvoice_psaume);
        psalmTitleTV.setText(Html.fromHtml(_psalmTitle));
        TextView psalmSubTitleTV = (TextView)findViewById(R.id.textView_subtitle_com_voice_godvoice_psaume);
        psalmSubTitleTV.setText(Html.fromHtml(_psalmSubTitle));
        TextView psalmTV = (TextView)findViewById(R.id.textView_psalm_com_voice_godvoice_psaume);
        psalmTV.setText(Html.fromHtml(_psalm));
        TextView psalmCreditsTV = (TextView)findViewById(R.id.textView_psalmCredits_com_voice_godvoice_psaume);
        psalmCreditsTV.setText(Html.fromHtml(_psalmCredits));

        LinearLayout shareLL = (LinearLayout)findViewById(R.id.linearLayout_share_com_godvoice_psaume);
        shareLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharePsalm();
            }
        });
    }

    private void sharePsalm() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDPSALM + _date + "/" +
                " " + getString(R.string.CALENDAR_PSALM_SHARE_EMAIL_SUBJECT);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void displayReadingContents() {
        LinearLayout lect2LL = (LinearLayout)findViewById(R.id.linearLayout_secondlect_com_voice_godvoice_lecture);
        if (_reading2 == null || _reading2.length() == 0)
            lect2LL.setVisibility(View.GONE);
        else {
            lect2LL.setVisibility(View.VISIBLE);
            TextView reading2TitleTV = (TextView)findViewById(R.id.textView_headertitle2_com_voice_godvoice_lecture);
            reading2TitleTV.setText(Html.fromHtml(_reading2Title));
            TextView reading2SubTitleTV = (TextView)findViewById(R.id.textView_voicesrc2_com_voice_godvoice_lecture);
            reading2SubTitleTV.setText(Html.fromHtml(_reading2SubTitle));
            TextView reading2TV = (TextView)findViewById(R.id.textView_godvoice2_com_voice_godvoice_lecture);
            reading2TV.setText(Html.fromHtml(_reading2));
            TextView reading2CreditsTV = (TextView)findViewById(R.id.textView_godvoiceCredits2_com_voice_godvoice_lecture);
            reading2CreditsTV.setText(Html.fromHtml(_reading2Credits));
        }
        TextView readingTitleTV = (TextView)findViewById(R.id.textView_headertitle1_com_voice_godvoice_lecture);
        readingTitleTV.setText(Html.fromHtml(_readingTitle));
        TextView readingSubTitleTV = (TextView)findViewById(R.id.textView_voicesrc_com_voice_godvoice_lecture);
        readingSubTitleTV.setText(Html.fromHtml(_readingSubTitle));
        TextView readingTV = (TextView)findViewById(R.id.textView_godvoice_com_voice_godvoice_lecture);
        readingTV.setText(Html.fromHtml(_reading));
        TextView readingCreditsTV = (TextView)findViewById(R.id.textView_godvoiceCredits_com_voice_godvoice_lecture);
        readingCreditsTV.setText(Html.fromHtml(_readingCredits));

        LinearLayout shareLL = (LinearLayout)findViewById(R.id.linearLayout_share_com_godvoice_lecture);
        shareLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareReading();
            }
        });
    }

    private void displayOraisonContents() {
        TextView orationTV = (TextView)findViewById(R.id.textView_oraison_com_voice_godvoice_oraison);
        orationTV.setText(Html.fromHtml(_oration));

        TextView orationCreditsTV = (TextView)findViewById(R.id.textView_oraisonCredits_com_voice_godvoice_oraison);
        orationCreditsTV.setText(Html.fromHtml(_orationCredits));

        ImageView shareBtn = (ImageView)findViewById(R.id.imageButton_share_com_voice_godvoice_oraison);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOraison();
            }
        });
        TextView shareTV = (TextView)findViewById(R.id.textView_share_com_voice_godvoice_oraison);
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOraison();
            }
        });
    }

    private void shareOraison() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDORATION + _date + "/" +
                " " + getString(R.string.OraisondujoursurCD);
        if (Tools.CDDEBUG) Log.d("GodVoice::shareOraison::text", text);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void shareReading() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDREADING + _date + "/" +
                " " + getString(R.string.CALENDAR_READING_SHARE_EMAIL_SUBJECT);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void displayMedContents() {
        TextView GodVoiceSrcTV = (TextView)findViewById(R.id.textView_godvoicesrc_com_voice_godvoice_meditation);
        if (_GodVoiceSrc != null && _GodVoiceSrc.length() > 0 && GodVoiceSrcTV != null)
            GodVoiceSrcTV.setText(_GodVoiceSrc);
        if (!_medText.equals("")) {
            displayGospelCommentaryWithoutApi();
        }
        if (_args == null)
            _args = new ArrayList<NameValuePair>();
        _args.clear();
        if (UserConnected.getInstance().IsUserConnected()) {
            _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
            _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
        }
        _args.add(new BasicNameValuePair("date", _date));
        _apiCaller = new HttpApiCall(this, _args, 2);
        _apiCaller.execute(Tools.API + Tools.CDCALENDAR + Tools.CDGETGOSPELCOMMENTARY);

        ImageView shareIV = (ImageView)findViewById(R.id.imageView_shareicon_com_voice_godvoice_mediation);
        shareIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCommentary();
            }
        });
        TextView shareTV = (TextView)findViewById(R.id.textView_shareicon_com_voice_godvoice_mediation);
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCommentary();
            }
        });
    }

    private void shareCommentary() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDGOSPELCOMMENTARY + _date + "/" +
                " " + _GodVoiceSrc + " " + getString(R.string.surCarpeDeum);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    public void getGodVoiceCommentaryFromApi(String result) {
        try {
            if (Tools.CDDEBUG)
                Log.d("Voice::getGodVoiceCommentaryFromApi::", result);
            JSONObject gospelCommentaryObj = new JSONObject(result);
            TextView gospelMedTV = (TextView)findViewById(R.id.textView_med_com_voice_godvoice_mediation);
            TextView gospelMedCreditsTV = (TextView)findViewById(R.id.textView_medcredits_com_voice_godvoice_mediation);
            if (gospelCommentaryObj.getString("text").length() == 0) {
                gospelMedTV.setText(R.string.CALENDAR_TAB_COMMENTARY_EMPTY);
                gospelMedTV.setBackgroundResource(R.drawable.roundedcorner_yellow);
                gospelMedTV.setPadding(10, 10, 10, 10);
                gospelMedCreditsTV.setVisibility(View.GONE);
                gospelMedCreditsTV.setTextSize(12);
                RelativeLayout shareRL = (RelativeLayout)findViewById(R.id.relativeLayout_share_com_voice_godvoice_meditation);
                shareRL.setVisibility(View.GONE);
            }
            else {
                if (_medText.equals("")) {
                    gospelMedTV.setText(Html.fromHtml(gospelCommentaryObj.getString("text")));
                }
                if (_medTextCredits.equals("")) {
                    gospelMedCreditsTV.setText(Html.fromHtml(gospelCommentaryObj.getString("credits")));
                }
            }
            TextView numLikesTV = (TextView)findViewById(R.id.textview_numLikes_com_voice_godvoice_mediation);
            numLikesTV.setText(gospelCommentaryObj.getString("num_likes"));
            manageLikes(gospelCommentaryObj);
            setComments(gospelCommentaryObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setComments(JSONObject result) throws Exception {
        LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_voice_manvoice);
        commentsLL.removeAllViews();
        setCommentOnClickListener(result.getString("latest_comments"), result.getString("id"));
        if (result.getInt("num_comments") > 0) {
            if (Tools.CDDEBUG)
                Log.d("Comments", result.getString("num_comments"));
            JSONArray commentsArray = new JSONArray(result.getString("latest_comments"));
            for (int i = 0; i < commentsArray.length() && i < 4; ++i) {
                final JSONObject commentObj = commentsArray.getJSONObject(i);
                if (Tools.CDDEBUG)
                    Log.d("Comment::", commentObj.toString());
                LinearLayout commentLL = new LinearLayout(this);
                commentLL.setOrientation(LinearLayout.HORIZONTAL);
                ImageView profileIV = getProfileImageView(commentObj.getString("profilepic"), 6);
                /*if (profileIV != null) {
                    LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(getScreenWidth() / 6, getScreenWidth() / 6);
                    imageViewLP.setMargins(5, 5, 5, 5);
                    commentLL.addView(profileIV, imageViewLP);
                }*/
                commentLL.addView(profileIV);
                String text = "<small><font color='#888888'>" + commentObj.getString("profilename") + ", " + commentObj.getString("dateinfo") + "</small><br/><br/>";
                text += "<font color='#000000'>" + commentObj.getString("text") + "</font>";
                TextView commentTV = new TextView(this);
                commentTV.setText(Html.fromHtml(text));
                commentTV.setPadding(5, 0, 5, 5);
                commentTV.setTextSize(16);
                commentLL.addView(commentTV, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 80));
                commentsLL.addView(commentLL);
                commentLL.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            setCommentLLOnClickListener(commentObj.getString("profileid"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                LinearLayout.LayoutParams separatorLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
                View separatorView = new View(this);
                separatorView.setBackgroundResource(R.color.gris80);
                commentsLL.addView(separatorView, separatorLP);
            }
            if (Tools.CDDEBUG) {
                Log.d("ManVoice::setComments::num::", result.getString("num_comments") + ", latest : " + result.getString("latest_comments").length());
            }
            if (result.getInt("num_comments") > 4) {
                moreComments(result.getInt("num_comments"), result.getString("latest_comments"), result.getString("id"));
            }
        }
    }

    private ImageView getProfileImageView(String imageURL, int divider) {
        ImageView profileIV = new ImageView(this);
        int size = getScreenWidth() / divider;
        LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(size, size);
        profileIV.setLayoutParams(profileLP);
        if (imageURL != null && imageURL.length() > 0 && internetConnectionOk()) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(imageURL + size);
            if (cachedImage == null) {
                new DownloadImages(profileIV, true, imageURL + size).execute(Tools.MEDIAROOT + imageURL, String.valueOf(size));
            }
            else {
                profileIV.setImageBitmap(cachedImage);
                profileIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            return profileIV;
        }
        profileIV.setImageResource(R.drawable.default_user);
        profileIV.setMaxWidth(size);
        profileIV.setMaxHeight(size);
        return profileIV;
    }

    public boolean internetConnectionOk() {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void setCommentOnClickListener(final String lastestComments, final String id) {
        TextView addCommentTv = (TextView)findViewById(R.id.textView_addComment_com_voice_manvoice);
        addCommentTv.setVisibility(View.VISIBLE);
        addCommentTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent addCommentIntent = new Intent(GodVoice.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "GOSPELCOMMENTARY");
                    addCommentIntent.putExtra("item_id", id);
                    startActivityForResult(addCommentIntent, COMMENT);
                }
                else {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("startActivity", "profile");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COMMENT && resultCode == RESULT_OK) {
            try {
                displayMedContents();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setCommentLLOnClickListener(String profileId) {
        Intent profileIntent = new Intent(GodVoice.this, MyProfile.class);
        profileIntent.putExtra("resId", _resId);
        profileIntent.putExtra("profileId", profileId);
        startActivity(profileIntent);
    }

    private void moreComments(int totalComments, final String lastestComments, final String id) {
        LinearLayout commentsLL = (LinearLayout)findViewById(R.id.linearLayout_comments_com_voice_manvoice);
        int restComments = totalComments - 4;
        if (Tools.CDDEBUG)
            Log.d("ManVoice::moreComments::", "rest : " + restComments);
        LinearLayout moreCommentsLL = new LinearLayout(this);
        moreCommentsLL.setOrientation(LinearLayout.HORIZONTAL);
        ImageView commentIV = new ImageView(this);
        commentIV.setImageResource(R.drawable.footer_comments);
        LinearLayout.LayoutParams commentsIvLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 20);
        commentsIvLP.gravity = Gravity.CENTER;
        moreCommentsLL.addView(commentIV, commentsIvLP);
        TextView moreCommentsTV = new TextView(this);
        moreCommentsTV.setText(getString(R.string.DISPLAY_MORE_COMMENTS, restComments));
        LinearLayout.LayoutParams textLP = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 80);
        textLP.gravity = Gravity.CENTER;
        moreCommentsLL.addView(moreCommentsTV, textLP);
        moreCommentsLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserConnected.getInstance().IsUserConnected()) {
                    Intent addCommentIntent = new Intent(GodVoice.this, AddComment.class);
                    addCommentIntent.putExtra("resId", _resId);
                    //addCommentIntent.putExtra("comments", lastestComments);
                    addCommentIntent.putExtra("item_type", "GOSPELCOMMENTARY");
                    addCommentIntent.putExtra("item_id", id);
                    startActivityForResult(addCommentIntent, COMMENT);
                }
                else {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("startActivity", "profile");
                    setResult(RESULT_OK, myIntent);
                    finish();
                }
            }
        });
        commentsLL.addView(moreCommentsLL, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getScreenWidth() / 6));
    }

    private void manageLikes(final JSONObject classifiedObj) throws Exception {
        ImageView iLikeIV = (ImageView)findViewById(R.id.imageView_likes_com_voice_godvoice_meditation);
        if (classifiedObj.getString("i_like").equals("1"))
            iLikeIV.setImageResource(_drawableLikeID);
        else
            iLikeIV.setImageResource(R.drawable.footer_like);
        iLikeIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UserConnected.getInstance().IsUserConnected()) {
                    Intent myIntent = getIntent();
                    myIntent.putExtra("startActivity", "profile");
                    setResult(RESULT_OK, myIntent);
                    finish();
                } else {
                    try {
                        List<NameValuePair> _args = new ArrayList<NameValuePair>();
                        String url;
                        _args.add(new BasicNameValuePair("item_type", "GOSPELCOMMENTARY"));
                        _args.add(new BasicNameValuePair("item_id", classifiedObj.getString("id")));
                        _args.add(new BasicNameValuePair("uid", UserConnected.getInstance().get_uid()));
                        _args.add(new BasicNameValuePair("sid", UserConnected.getInstance().get_sid()));
                        if (classifiedObj.getString("i_like").equals("1"))
                            url = Tools.API + Tools.CDLIKESREMOVE;
                        else
                            url = Tools.API + Tools.CDLIKESADD;
                        _apiCaller = new HttpApiCall(GodVoice.this, _args, 3);
                        _apiCaller.execute(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void displayGospelCommentaryWithoutApi() {
        TextView gospelMedTV = (TextView)findViewById(R.id.textView_med_com_voice_godvoice_mediation);
        gospelMedTV.setText(Html.fromHtml(_medText));
        TextView gospelMedCreditsTV = (TextView)findViewById(R.id.textView_medcredits_com_voice_godvoice_mediation);
        gospelMedCreditsTV.setText(Html.fromHtml(_medTextCredits));
    }

    private void displayEvangileContents() {
        TextView GodVoiceSrcTV = (TextView)findViewById(R.id.textView_godvoicesrc_com_voice_godvoice_evangile);
        GodVoiceSrcTV.setText(_GodVoiceSrc);
        Button godVoiceBtn = (Button)findViewById(R.id.button_audio_com_voice_godvoice_evangile);
        godVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Tools.CDDEBUG) Log.d("Starting player with URL:", _audioURL);
                Intent listenVoiceIntent = new Intent(GodVoice.this, ListenGodVoice.class);
                listenVoiceIntent.putExtra("audioURL", _audioURL);
                listenVoiceIntent.putExtra("imgURL", _picture);
                startActivity(listenVoiceIntent);
            }
        });
        displayPicture();
        displayVoice();
    }

    private void displayVoice() {
        if (_voice != null && !_voice.equals("")) {
            TextView voiceTV = (TextView)findViewById(R.id.textView_godvoice_com_voice_godvoice_evangile);
            voiceTV.setText(Html.fromHtml(_voice));
            TextView voiceCreditsTV = (TextView)findViewById(R.id.textView_godvoiceCredits_com_voice_godvoice_evangile);
            voiceCreditsTV.setText(Html.fromHtml(_credits));
        }
        else {
            //TODO Call api to get the voice
        }
        ImageButton shareIB = (ImageButton)findViewById(R.id.imageButton_share_com_voice_godvoice_evangile);
        shareIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVoice();
            }
        });
        TextView shareTV = (TextView)findViewById(R.id.textView_share_com_voice_godvoice_evangile);
        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareVoice();
            }
        });
    }

    private void shareVoice() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String text = Tools.CDMOBILEROOT + Tools.CDDEFAULTLANG + Tools.CDCONTENT + Tools.CDGOSPEL + _date + "/" +
                " " + _GodVoiceSrc + " " + getString(R.string.surCarpeDeum);
        if (Tools.CDDEBUG) Log.d("GodVoice::shareVoice::text", text);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.PartagerVia)));
    }

    private void displayPicture() {
        ImageView godVoiceIV = (ImageView)findViewById(R.id.imageView_evangile_com_voice_godvoice_evangile);
        if (_picture != null && !_picture.equals("null") && !_picture.equals("")) {
            Bitmap cachedImage = ImageCache.getInstance().getBitmapFromMemCache(_picture + getScreenWidth() / 2);
            if (cachedImage == null) {
                new DownloadImages(godVoiceIV, true, _picture + getScreenWidth() / 6).execute(Tools.MEDIAROOT + _picture, String.valueOf(getScreenWidth() / 2));
            }
            else {
                godVoiceIV.setImageBitmap(cachedImage);

            }
            int width = getScreenWidth() / 2;
            int height = (3 * width) / 4;
            LinearLayout.LayoutParams GodVoiceLP = new LinearLayout.LayoutParams(width, height);
            GodVoiceLP.gravity = Gravity.CENTER;
            GodVoiceLP.setMargins(0, 10, 0, 10);
            godVoiceIV.setLayoutParams(GodVoiceLP);
        }
    }

    /*public void getGodVoiceFromApi(String result) {
        try {
            JSONObject godVoiceElems = new JSONObject(result);
            if (Tools.CDDEBUG) Log.d("GodVoice::getGodVoiceFromApi::image::", godVoiceElems.getString("image"));
            _picture = godVoiceElems.getString("image");
            _apiVoice = godVoiceElems.getString("text");
            displayPicture();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

    private void setLayoutTheme() {
        if (_resId == 0 || _resId == -1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header);
            _drawableLikeID = R.drawable.footer_like_on;
        }
        else if (_resId == 1) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_blue);
            _drawableLikeID = R.drawable.footer_like_on_blue;
        }
        else if (_resId == 2) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_gold);
            _drawableLikeID = R.drawable.footer_like_on_gold;
        }
        else if (_resId == 3) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_green);
            _drawableLikeID = R.drawable.footer_like_on_green;
        }
        else if (_resId == 4) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_mauve);
            _drawableLikeID = R.drawable.footer_like_on_mauve;
        }
        else if (_resId == 5) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_orange);
            _drawableLikeID = R.drawable.footer_like_on_orange;
        }
        else if (_resId == 6) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_purple);
            _drawableLikeID = R.drawable.footer_like_on_purple;
        }
        else if (_resId == 7) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_red);
            _drawableLikeID = R.drawable.footer_like_on_red;
        }
        else if (_resId == 8) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.header_silver);
            _drawableLikeID = R.drawable.footer_like_on_silver;
        }
    }


    // TOOLS
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
        else {
            width = display.getWidth();
        }
        return width;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onMessageButtonClicked(View view) {
        Intent messagesIntent = new Intent(GodVoice.this, DisplayMessages.class);
        messagesIntent.putExtra("resId", _resId);
        startActivity(messagesIntent);
    }

    @Override
    public void onApiResult(String result, int type) throws Exception {
        if (type == 1) {
            JSONObject godVoiceElems = new JSONObject(result);
            if (Tools.CDDEBUG) Log.d("GodVoice::getGodVoiceFromApi::image::", godVoiceElems.getString("image"));
            _picture = godVoiceElems.getString("image");
            _apiVoice = godVoiceElems.getString("text");
            displayPicture();
        }
        else if (type == 2) {
            getGodVoiceCommentaryFromApi(result);
        }
        else if (type == 3) {
            displayMedContents();
        }
    }
}