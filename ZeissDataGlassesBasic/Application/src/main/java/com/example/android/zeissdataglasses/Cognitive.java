package com.example.android.zeissdataglasses;

/**
 * Created by khomenkos on 1/20/18.
 */

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Cognitive {
    // **********************************************
    // *** Update or verify the following values. ***
    // **********************************************

    // Replace the subscriptionKey string value with your valid subscription key.
    public static final String subscriptionKey = "86ba3aa631c8430e8b465c9fc5bdf015";

    // Replace or verify the region.
    //
    // You must use the same region in your REST API call as you used to obtain your subscription keys.
    // For example, if you obtained your subscription keys from the westus region, replace
    // "westcentralus" in the URI below with "westus".
    //
    // NOTE: Free trial subscription keys are generated in the westcentralus region, so if you are using
    // a free trial subscription key, you should not need to change this region.
    //
    // Also, if you want to use the celebrities model, change "landmarks" to "celebrities" here and in
    // uriBuilder.setParameter to use the Celebrities model.
    public static final String ocrURL = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/ocr?detectOrientation=true&language=unk";
    public static final String tokenURL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    public static final String textToSpeechSubscriptionKey = "4ef4fd02de554dbb92fe94c28fe38c46";
    public static final String speechURL = "https://speech.platform.bing.com/synthesize";


    public static String getSpeech(String text, String token, Context context) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URI uri = new URI(speechURL);

            // Request parameters.
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/ssml+xml");
            request.setHeader("Authorization", "Bearer " + token);
            request.setHeader("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3");
            request.setHeader("User-Agent", "Readit");
//            request.setHeader("X-Microsoft-OutputFormat", "audio-16khz-32kbitrate-mono-mp3");

            // Request body.
            StringEntity reqEntity = new StringEntity("<speak version='1.0' xml:lang='en-US'><voice xml:lang='en-US' xml:gender='Female' name='Microsoft Server Speech Text to Speech Voice (en-US, ZiraRUS)'>" +
                    text
                    + "</voice></speak>");
            request.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {

                File outputDir = context.getCacheDir(); // context being the Activity pointer
                File targetFile = File.createTempFile("prefix", "extension", outputDir);

                Log.i("ZEISSSS", "temp file: " + targetFile.toString());

                OutputStream outStream = new FileOutputStream(targetFile);
                entity.writeTo(outStream);
                outStream.close();

                return targetFile.toString();


            }
        } catch (Exception e) {
            // Display error message.
            Log.e("ZEISSS", "Exception: we failed?" + e.getMessage());

        }

        return "";
    }

    public static String getToken() {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URI uri = new URI(tokenURL);
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Ocp-Apim-Subscription-Key", textToSpeechSubscriptionKey);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Format and display the JSON response.
                String token = EntityUtils.toString(entity);
                System.out.println("Token: " + token);
                return token;
            }
        } catch (Exception e) {
            // Display error message.
            System.out.println("Exception: " + e.getMessage());
        }
        return "";

    }

    public static String getImageToText(String image) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URI uri = new URI(ocrURL);

            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

            File file = new File(image);
            // Request body.
            InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
            request.setEntity(reqEntity);

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Format and display the JSON response.
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println("REST Response:\n");
                String text = getText(json);
                System.out.println("Text found: " + text);

                return text;


            }
        } catch (Exception e) {
            // Display error message.
            System.out.println("Exception: " + e.getMessage());
        }
        return "";
    }

    private static String getText(JSONObject json) throws JSONException {
        List result = new ArrayList();
        JSONArray regions = json.getJSONArray("regions");
        for (int r=0; r<regions.length(); r++) {
            JSONObject region = (JSONObject) regions.get(r);
            if (region != null) {
                JSONArray lines = region.getJSONArray("lines");
                for (int l=0; l<lines.length(); l++) {
                    JSONArray words = ((JSONObject) lines.get(l)).getJSONArray("words");
                    for (int w=0; w<words.length(); w++) {
                        String text = ((JSONObject) words.get(w)).getString("text");
                        result.add(text);
                    }
                }

            }
        }
        return TextUtils.join(" ", result);
    }
}