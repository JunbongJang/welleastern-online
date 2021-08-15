package com.welleasern.online.Functionality;

import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.welleasern.online.Activities.ZhWebviewActivity;
import com.welleasern.online.Model.SpeakingState;

import java.util.ArrayList;

/**
 * Author: Junbong Jang
 * Date: 5/1/2019
 *
 * Uses the Internal Voice Recognizer provided by Google.
 */
public class MyRecognitionListener implements android.speech.RecognitionListener
{
    private static final String TAG = "Android: ";

    public void onReadyForSpeech(Bundle params)
    {
        Log.d(TAG, "onReadyForSpeech");
    }
    public void onBeginningOfSpeech()
    {
        Log.d(TAG, "onBeginningOfSpeech");
    }
    public void onRmsChanged(float rmsdB)
    {
        Log.d(TAG, "onRmsChanged");
    }
    public void onBufferReceived(byte[] buffer)
    {
        Log.d(TAG, "onBufferReceived");
    }
    public void onEndOfSpeech()
    {
        Log.d(TAG, "onEndofSpeech");
    }
    public void onError(int error)
    {
        Log.d(TAG,  "error " +  error);
        if(SpeakingState.recognition_on) {
            SpeakingState.recognition_start_trigger = true;
        }
    }
    public void onResults(Bundle results)
    {
        Log.i("Info", "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        String escaped_result = matches.get(0).replace("\'", "\\'");
        Log.i("Info", escaped_result);

        ZhWebviewActivity.webview.loadUrl("javascript:gradeRecognizedText(\"" + escaped_result + "\")");

        if(SpeakingState.recognition_on) {
            SpeakingState.recognition_start_trigger = true;
        }
    }
    public void onPartialResults(Bundle partialResults)
    {
        Log.d(TAG, "onPartialResults");
    }
    public void onEvent(int eventType, Bundle params)
    {
        Log.d(TAG, "onEvent " + eventType);
    }
}
