package com.example.android.zeissdataglasses;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import static com.example.android.zeissdataglasses.Cognitive.getImageToText;
import static com.example.android.zeissdataglasses.Cognitive.getSpeech;
import static com.example.android.zeissdataglasses.Cognitive.getToken;

/**
 * Created by khomenkos on 1/20/18.
 */

public class Ocr extends AsyncTask<Void, Void, String> {
    String filepath;
    Camera2BasicFragment senderActivity;

    public Ocr(Camera2BasicFragment senderActivity, String filepath) {
        this.filepath = filepath;
        this.senderActivity = senderActivity;
    }

    @Override
    protected String doInBackground(Void... params) {
        String text = getImageToText(this.filepath);
        String token = getToken();

        String mp3Filename = getSpeech(text, token, senderActivity.getContext());

        Log.i("ZEISSS", "doInBackground - " + mp3Filename);

        return mp3Filename;
    }

    @Override
    protected void onPostExecute(String mp3Filename) {
        Log.i("ZEISSS", "On post execute - " + mp3Filename);
        senderActivity.playSound(mp3Filename);
    }
}
