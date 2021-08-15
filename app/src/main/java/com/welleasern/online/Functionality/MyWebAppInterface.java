package com.welleasern.online.Functionality;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.welleasern.online.Model.SpeakingState;
import com.welleasern.online.R;

/**
 * Author: Junbong Jang
 * Date: 5/4/2019
 *
 * Provides the functionality for the Android webview to communicate with the HTML webpage.
 */
public class MyWebAppInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public MyWebAppInterface(Context c) {
        this.mContext = c;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void startRecognition() {
        showToast(this.mContext.getString(R.string.recognition_status_start));
        SpeakingState.recognition_start_trigger = true;
    }

    @JavascriptInterface
    public void stopRecognition() {
        showToast(this.mContext.getString(R.string.recognition_status_stop));
        SpeakingState.recognition_stop_trigger = true;
    }

    @JavascriptInterface
    public void startRecording() {
        showToast(this.mContext.getString(R.string.record_status_start));
        SpeakingState.record_myvoice_trigger = true;
    }

    @JavascriptInterface
    public void stopRecording() {
        showToast(this.mContext.getString(R.string.record_status_stop));
        SpeakingState.stop_record_myvoice_trigger = true;
    }

    /**
     * For SpeechMatics, voice Recognition
     */
    @JavascriptInterface
    public void submitMyVoice() {
        SpeakingState.save_filename = "";
        SpeakingState.send_myvoice_trigger = true;
    }

    /**
     * For Recording in Speaking FinalTest
     * @param filename
     */
    @JavascriptInterface
    public void submitMyVoiceWithName(String filename) {
        SpeakingState.save_filename = filename;
        SpeakingState.send_myvoice_trigger = true;
    }

    @JavascriptInterface
    public void startPlaying() {
        SpeakingState.play_myvoice_trigger = true;
    }

    @JavascriptInterface
    public void stopPlaying() {
        SpeakingState.stop_play_myvoice_trigger = true;
    }

    @JavascriptInterface
    public void respondToJavascript() {
        SpeakingState.respond_to_javascript = true;
    }
}
