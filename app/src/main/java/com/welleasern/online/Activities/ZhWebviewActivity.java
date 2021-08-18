package com.welleasern.online.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.welleasern.online.Functionality.MyRecognitionListener;
import com.welleasern.online.Functionality.MyWebAppInterface;
import com.welleasern.online.Functionality.MyWebviewClient;
import com.welleasern.online.Functionality.VoiceRecorder;
import com.welleasern.online.Model.SpeakingState;
import com.welleasern.online.Network.DownloadCallback;
import com.welleasern.online.Network.NetworkFragment;
import com.welleasern.online.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Support Jelly Bean, which means API version greater than or equal to 16
 * Class that mainly have a WebView Component and works as a hybrid mobile application
 * For more detail, please reference https://developer.android.com/guide/webapps/webview
 *
 * Author: Junbong Jang
 * Date: 5/6/2019
 */

public class ZhWebviewActivity extends FragmentActivity implements KeyEvent.Callback, DownloadCallback<String> {

    public static WebView webview;
    private static int prev_system_volume;
    VoiceRecorder voiceRecorder;
    SpeechRecognizer sr;

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment networkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean downloading = false;

    CountDownTimer transcription_request_timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        networkFragment = NetworkFragment.getInstance(getFragmentManager(),
                this.getString(R.string.transcription_request_url));
        initWebview(savedInstanceState);
        initRecognition();
        String androidId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String fileName = getExternalCacheDir().getAbsolutePath() + "/" + androidId + "_android_voice.mp3";
        voiceRecorder = new VoiceRecorder(fileName);

        startTimer();
//        startCheckTranscriptionRequestTimer(10, "12973796");
    }

    private void startTimer() {
        // setup the timer that checks if the SpeakingRecognition started
        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (!appInForegroundMode) {
                    voiceRecorder.stopRecording();
                    voiceRecorder.stopPlaying();
                    handler.removeCallbacks(this);
                } else {
                    if (SpeakingState.respond_to_javascript) {
                        Log.i("respond_to_javascript", "");
                        SpeakingState.respond_to_javascript = false;
                        webview.loadUrl("javascript:respondToJavascript()");
                    }
                    if (SpeakingState.stop_record_myvoice_trigger) {
                        Log.i("stop_record_myvoice", "trigger");
                        SpeakingState.stop_record_myvoice_trigger = false;
                        voiceRecorder.stopRecording();
                    }
                    else if (SpeakingState.record_myvoice_trigger) {
                        Log.i("record_myvoice", "trigger");
                        SpeakingState.record_myvoice_trigger = false;
                        voiceRecorder.startRecording();
                    }
                    //-----------------Voice Recognition for Speaking 3---------------------
                    if (SpeakingState.recognition_stop_trigger) {
                        Log.i("stopRecognition", "triggered");
                        SpeakingState.recognition_stop_trigger = false;
                        SpeakingState.recognition_start_trigger = false;
                        SpeakingState.recognition_on = false;
                        sr.stopListening();
                    }
                    else if (SpeakingState.recognition_start_trigger) {
                        Log.i("startRecognition", "triggered");
                        SpeakingState.recognition_start_trigger = false;
                        SpeakingState.recognition_on = true;
                        voiceRecorder.stopRecording();
                        startRecognition();
                    }
                    //--------------------------------------
                    if (SpeakingState.stop_play_myvoice_trigger) {
                        Log.i("stop_play_myvoice", "triggered");
                        SpeakingState.stop_play_myvoice_trigger = false;
                        voiceRecorder.stopPlaying();
                    }
                    else if (SpeakingState.play_myvoice_trigger) {
                        Log.i("play_myvoice", "triggered");
                        SpeakingState.play_myvoice_trigger = false;
                        SpeakingState.recognition_stop_trigger = true;
                        SpeakingState.stop_record_myvoice_trigger = true;
                        voiceRecorder.startPlaying();
                    }

                    if (SpeakingState.send_myvoice_trigger && SpeakingState.save_filename.equals("")) {
                        Log.i("send_myvoice filename", "triggered");
                        // Speech Recognition using SpeechMatics
                        SpeakingState.send_myvoice_trigger = false;
                        startDownload(voiceRecorder.getAudioFileName(), null);
                    }
                    else if (SpeakingState.send_myvoice_trigger) {
                        Log.i("send_myvoice", "triggered");
                        // Save my voice into Welleastern Server for Speaking FInal
                        // The path is /IMENTOR/recorded-voice-upload.php
                        SpeakingState.send_myvoice_trigger = false;
                        startDownload(voiceRecorder.getAudioFileName(), "https://www.welleastern.co.kr/IMENTOR/recorded-voice-upload-android.php?filename="+ SpeakingState.save_filename);
                    }
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(run);
    }

    // ------------------------ HTTP Connection Code Starts -----------------------------
    // Not necessary now since we are using Google speech recognition without voice recorder

    private void startDownload(String audio_file_name, String url) {
        if (!downloading && networkFragment != null) {
            // Execute the async download.
            networkFragment.startDownload(audio_file_name, url);
            downloading = true;
        }
    }

    private void startCheckTranscriptionRequestTimer(int wait_seconds, String job_id) {
        final String job_id_final = job_id;
        final long TIME_LIMIT = 130000;
        final long wait_millis = wait_seconds * 1000;
        final String request_url = this.getString(R.string.transcription_result_url_front) + job_id_final
                + "/transcript?format=txt&auth_token=" + this.getString(R.string.transcription_auth_token);

        transcription_request_timer = new CountDownTimer(TIME_LIMIT, 5000) {

            public void onTick(long millisUntilFinished) {
                long elasped_time = TIME_LIMIT - millisUntilFinished;
                if (wait_millis <= elasped_time) {
                    startDownload(null, request_url);
                }
            }
            public void onFinish() {
                startDownload(null, request_url);
            }

        }.start();
    }

    @Override
    public void updateFromDownload(String result) {
        // Update your UI here based on result of download.
        Log.i("updateFromDownload", result);
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(result);
            if (mainObject != null && mainObject.get("cost") != null ) { // transcription is requested
                startCheckTranscriptionRequestTimer(mainObject.getInt("check_wait"), mainObject.getString("id"));
            } else if (mainObject != null && mainObject.get("error") != null) {
                Toast.makeText(this, this.getString(R.string.recognition_wait_msg), Toast.LENGTH_LONG);
            }
        } catch (JSONException e) {  // transcript result is here. it's not a json object, but text
            if (!result.contains("404 error") && SpeakingState.save_filename.equals("")) {
                String escaped_result = result.replace("\'", "\\'").replace("SPEAKER: M1", "").replace("SPEAKER: F1", "");
                webview.loadUrl("javascript:gradeRecognizedText(\"" + escaped_result + "\")");
                transcription_request_timer.cancel();
            }
        }

    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void finishDownloading() {
        downloading = false;
        if (networkFragment != null) {
            networkFragment.cancelDownload();
        }
    }

    // ------------------------ HTTP Connection Code Ends -----------------------------


    private void initWebview(Bundle savedInstanceState) {
        webview = (WebView) findViewById(R.id.webview_elem);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new MyWebAppInterface(this), "AndroidJJ");
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webview, true);
        } else {
            CookieManager.getInstance().setAcceptCookie(true);
        }

        webview.getSettings().setSupportZoom(true);
        //webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebChromeClient(new WebChromeClient()); // to make alert popup in HTML
        webview.setWebViewClient(new MyWebviewClient(this));

        webview.getSettings().setUseWideViewPort(true);
        webview.setInitialScale(1);

        if (savedInstanceState == null) {
            webview.loadUrl(this.getString(R.string.webview_url));
        }

    }


    private void initRecognition() {
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new MyRecognitionListener());

        //https://stackoverflow.com/questions/21859079/speechrecognizer-insufficient-permissions-error-with-glass
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        else {
            Toast.makeText(this,  this.getString(R.string.error_permission), Toast.LENGTH_LONG).show();
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 9);
            }
        }
    }

//    https://stackoverflow.com/questions/10538791/how-to-set-the-language-in-speech-recognition-on-android
    public void startRecognition() {
        Log.i("Info", "Recognition Started Zh");

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,4);
        // secret parameters that when added provide audio url in the result
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO", true);

        this.sr.startListening(recognizerIntent);
    }


//--------------------------------- Auxiliary Functionality-----------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        webview.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webview.restoreState(savedInstanceState);
    }


    /**
     * These are for checking whether the app is in foreground or background.
     */
    private boolean appInForegroundMode;

    @Override
    protected void onPause() {
        Log.d("onPause","pause");

        this.mute(this);
        this.appInForegroundMode = false;
        super.onPause();

    }

    @Override
    protected void onResume() {
        Log.d("onResume","onResume");

        this.unmute(this);
        this.appInForegroundMode = true;
        startTimer();
        super.onResume();
    }


    @Override
    protected void onUserLeaveHint()
    {
        Log.d("onUserLeaveHint","Home button pressed");
        super.onUserLeaveHint();
    }

    public static void mute(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        prev_system_volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int mute_volume = 0;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mute_volume, 0);
    }

    public static void unmute(Context context) {

        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        int unmute_volume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, prev_system_volume, 0);
    }

    /**
     * Implement back button to go to previous HTML page
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webview.canGoBack()) {
            webview.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

}
