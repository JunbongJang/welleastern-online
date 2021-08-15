package com.welleasern.online.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
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

/**
 * This file is not used .................... Just use ZhWebviewActivity for both China and Korea
 * Class that mainly have a WebView Component and works as a hybrid mobile application
 * For more detail, please reference https://developer.android.com/guide/webapps/webview
 *
 * Author: Junbong Jang
 * Date: 5/6/2019
 */

public class WebviewActivity extends FragmentActivity implements KeyEvent.Callback, DownloadCallback<String> {

    SpeechRecognizer sr;
    public static WebView webview;
    VoiceRecorder voiceRecorder;

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment networkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean downloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);


//        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager(),
//                "https://api.speechmatics.com/v1.0/user/67350/jobs/" + "12891839" + "/transcript?format=txt&auth_token=YTAxNmJjNjUtMDY3MS00ODA0LWFlOGItYWJiZTA2MWU2Yjk2");
        networkFragment = NetworkFragment.getInstance(getFragmentManager(), "https://api.speechmatics.com/v1.0/user/67350/jobs/?auth_token=YTAxNmJjNjUtMDY3MS00ODA0LWFlOGItYWJiZTA2MWU2Yjk2");
        initWebview();
        initRecognition();
        voiceRecorder = new VoiceRecorder(getExternalCacheDir().getAbsolutePath());

        // setup the timer that checks if the SpeakingRecognition started
        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            int counter;
            @Override
            public void run() {
                if (SpeakingState.stop_record_myvoice_trigger) {
                    SpeakingState.stop_record_myvoice_trigger = false;
                    voiceRecorder.stopRecording();
                    startDownload(null);
//                    startDownload(voiceRecorder.getAudioFileName());
                }
                else if (SpeakingState.record_myvoice_trigger && appInForegroundMode) {
                    SpeakingState.record_myvoice_trigger = false;
                    voiceRecorder.startRecording();
                }

//                if (SpeakingState.recognition_stop_state) {
//                    SpeakingState.recognition_start_trigger = false;
//                }
//                else if (SpeakingState.recognition_start_trigger && appInForegroundMode) {
//                    SpeakingState.recognition_start_trigger = false;
//                    startRecognition();
//                }

                if (SpeakingState.stop_play_myvoice_trigger) {
                    SpeakingState.stop_play_myvoice_trigger = false;
                    voiceRecorder.stopPlaying();
                }
                else if (SpeakingState.play_myvoice_trigger && appInForegroundMode) {
                    SpeakingState.play_myvoice_trigger = false;
                    voiceRecorder.startPlaying();
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(run);
    }

    // ------------------------ HTTP Connection Code Starts -----------------------------
//    curl -F data_file=@my_audio_file.mp3 -F model=en-US "https://api.speechmatics.com/v1.0/user/67350/jobs/?auth_token=YTAxNmJjNjUtMDY3MS00ODA0LWFlOGItYWJiZTA2MWU2Yjk2"
//    curl "https://api.speechmatics.com/v1.0/user/67350/jobs/$MY_JOB_ID/transcript?format=txt&auth_token=YTAxNmJjNjUtMDY3MS00ODA0LWFlOGItYWJiZTA2MWU2Yjk2"

    private void startDownload(String audio_name) {
        if (!downloading && networkFragment != null) {
            // Execute the async download.
            if (audio_name == null) {
                networkFragment.startDownload(null, null);
            } else {
                networkFragment.startDownload(audio_name, null);
            }
            downloading = true;
        }
    }

    @Override
    public void updateFromDownload(String result) {
        // Update your UI here based on result of download.
        Log.i("updateFromDownload", result);
        webview.loadUrl("javascript:gradeRecognizedText(\"" + "this is a recognized text for testing purpose. No rain No water. He is eating fruit water! Tiger we are thirsty." + "\")");

//        JSONObject mainObject = null;
//        try {
//            mainObject = new JSONObject(result);
//            if ((Integer) mainObject.get("id") != null ) {
//                Log.i("updateFromDownload wait", (String) mainObject.get("id"));
//            } else {
//                String escaped_result = result.replace("\'", "\\'");
//               webview.loadUrl("javascript:gradeRecognizedText(\"" + "this is a recognized text for testing purpose. No rain No water. He is eating fruit water! Tiger we are thirsty." + "\")");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

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


    private void initWebview() {
        webview = (WebView) findViewById(R.id.webview_elem);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new MyWebAppInterface(this), "AndroidJJ");

        webview.getSettings().setSupportZoom(true);
        //webview.getSettings().setBuiltInZoomControls(true);
        webview.setWebChromeClient(new WebChromeClient()); // to make alert popup in HTML
        webview.setWebViewClient(new MyWebviewClient(this));

        webview.getSettings().setUseWideViewPort(true);
        webview.setInitialScale(1);

        webview.loadUrl(this.getString(R.string.webview_url));
    }


    private void initRecognition() {
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new MyRecognitionListener());

        //https://stackoverflow.com/questions/21859079/speechrecognizer-insufficient-permissions-error-with-glass
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)
            return;
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Record Audio Permission is required", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9);

            }
        }
    }

    public void startRecognition() {
        Log.i("Info", "Recognition Started kor");

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,4);
        // secret parameters that when added provide audio url in the result
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        recognizerIntent.putExtra("android.speech.extra.GET_AUDIO", true);

        this.sr.startListening(recognizerIntent);
    }


//--------------------------------- Auxiliary Functionality-----------------------------------------


    /**
     * These are for checking whether the app is in foreground or background.
     */
    private boolean appInForegroundMode;

    @Override
    protected void onPause() {
        super.onPause();
        this.appInForegroundMode = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.appInForegroundMode = true;
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
