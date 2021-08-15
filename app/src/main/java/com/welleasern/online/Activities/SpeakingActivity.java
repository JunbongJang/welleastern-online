package com.welleasern.online.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.ActionBar;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.welleasern.online.Functionality.MyRecognitionListener;
import com.welleasern.online.R;

import java.util.ArrayList;

/**
 * learned from https://www.techjini.com/blog/android-speech-to-text-tutorial-part1/
 * https://stackoverflow.com/questions/4975443/is-there-a-way-to-use-the-speechrecognizer-api-directly-for-speech-input
 */
public class SpeakingActivity extends AppCompatActivity {

    private TextView recognitionResultTextView;
    SpeechRecognizer sr;
    private int curTimeInSec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaking_main);

        initTimer();
        final Handler handler = new Handler();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                updateTimer(curTimeInSec);

                curTimeInSec++;
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(run);

        setupActionBar();

        Intent intent = getIntent();
        Log.i("Informat", "SpeakingActivity--");
        Log.i("Informat", intent.getStringExtra("path"));

        recognitionResultTextView = findViewById (R.id.center_text);
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new MyRecognitionListener());

        //https://stackoverflow.com/questions/21859079/speechrecognizer-insufficient-permissions-error-with-glass
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)
            return;
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Record audio is required", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9);

            }
        }
    }

    // https://developer.android.com/training/appbar/up-action
    private void setupActionBar() {
        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar = (Toolbar) findViewById(R.id.top_action_bar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void initTimer() {
        ((SeekBar)findViewById(R.id.timerSeekBar)).setMax(600); // 10 minutes
        this.curTimeInSec = 0;
    }

    private void updateTimer(int curTimeInSec) {
        int minutes = curTimeInSec / 60;
        int seconds = curTimeInSec % 60;
        String seconds_string = Integer.toString(seconds);
        String minutes_string = Integer.toString(minutes);
        if (seconds < 10) {
            seconds_string = "0" + seconds;
        }
        if (minutes < 10) {
            minutes_string = "0" + minutes;
        }
        ((TextView)findViewById(R.id.timerTextView)).setText(minutes_string + ":" + seconds_string);
        ((SeekBar)findViewById(R.id.timerSeekBar)).setProgress(curTimeInSec);
    }

    public void startNativeVoice(View view) {

    }

    public void stopNativeVoice(View view) {

    }

    public void playMyVoice(View view) {

    }

    public void stopMyVoice(View view) {

    }

    public void startRecognition(View view) {
        Log.i("Informat", "button pressed ");

        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,4);
        this.sr.startListening(recognizerIntent);
    }



    public void stopRecognition() {

    }
}
