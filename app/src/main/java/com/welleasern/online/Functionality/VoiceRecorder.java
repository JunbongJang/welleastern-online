package com.welleasern.online.Functionality;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;



/**
 * Author: Junbong Jang
 * Date: 5/9/2019
 *
 * Contains functions that records the voice and play it
 * Reference is //https://developer.android.com/guide/topics/media/mediarecorder
 */
public class VoiceRecorder {
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private static final String LOG_TAG = "VoiceRecorder";
    private static String fileName = null;

    public VoiceRecorder(String fileName) {
        // Record to the external cache directory for visibility
        this.fileName = fileName;
        Log.i(LOG_TAG, this.fileName);
    }

    public void startPlaying() {

        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    public void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public String getAudioFileName() {
        return fileName;
    }

}
