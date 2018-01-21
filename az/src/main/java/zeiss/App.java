package zeiss;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class App {
    // **********************************************
    // *** Update or verify the following values. ***
    // **********************************************

    // Replace the analysysSubscriptionKey string value with your valid subscription key.
    public static final String subscriptionKey = "86ba3aa631c8430e8b465c9fc5bdf015";

    public static final String analysysSubscriptionKey = "86ba3aa631c8430e8b465c9fc5bdf015";
    public static final String analysysUriBase = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze";


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
    public static final String ocrURL = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/ocr";
    public static final String tokenURL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
    public static final String textToSpeechSubscriptionKey = "4ef4fd02de554dbb92fe94c28fe38c46";
    public static final String speechURL = "https://speech.platform.bing.com/synthesize";


    public static void main(String[] args) {
        String text = getImageToText("/home/raz/Desktop/toxic.png");
        String token = getToken();

        getSpeech(text, token);
    }

    private static void getSpeech(String text, String token) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URIBuilder uriBuilder = new URIBuilder(speechURL);

            // Request parameters.
            URI uri = uriBuilder.build();
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
                InputStream content = entity.getContent();
                System.out.println("Speech data size: "+content.available());
                File targetFile = new File("/home/raz/tmp/text.mp3");
                OutputStream outStream = new FileOutputStream(targetFile);
                entity.writeTo(outStream);
                outStream.close();


            }
        } catch (Exception e) {
            // Display error message.
            System.out.println("Exception: " + e.getMessage());
        }
    }

    private static String getToken() {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URIBuilder uriBuilder = new URIBuilder(tokenURL);

            // Request parameters.
            URI uri = uriBuilder.build();
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

    private static String getImageToText(String image) {
        HttpClient httpClient = new DefaultHttpClient();
        String text = "";

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URIBuilder uriBuilder = new URIBuilder(ocrURL);

            uriBuilder.setParameter("language", "unk");
            uriBuilder.setParameter("detectOrientation ", "true");

            // Request parameters.
            URI uri = uriBuilder.build();
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
                text = getText(json);
                System.out.println("Text found: " + text);
            }
        } catch (Exception e) {
            // Display error message.
            System.out.println("Exception: " + e.getMessage());
        }

        if (text.isEmpty()){
            text = getAnalysys(image);
        }
        return text;
    }

    private static String getAnalysys(String image) {
        HttpClient httpClient = new DefaultHttpClient();

        try {
            // NOTE: You must use the same location in your REST call as you used to obtain your subscription keys.
            //   For example, if you obtained your subscription keys from westus, replace "westcentralus" in the
            //   URL below with "westus".
            URIBuilder uriBuilder = new URIBuilder(analysysUriBase);
            uriBuilder.setParameter("visualFeatures", "Categories,Description,Color");
            uriBuilder.setParameter("language", "en");

            // Request parameters.
            URI uri = uriBuilder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers.
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", analysysSubscriptionKey);

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
                JSONArray captions = json.getJSONObject("description").getJSONArray("captions");
                String result = "";
                for (Object caption : captions) {
                    if (caption != null){
                        JSONObject jsonCaption = (JSONObject) caption;
                        if (jsonCaption.getDouble("confidence") > 0.50d) {
                            result += ((JSONObject) caption).getString("text");
                        }
                    }
                }
                System.out.println(result);
                return result;
            }
        } catch (Exception e) {
            // Display error message.
            System.out.println(e.getMessage());
        }
        return "";
    }

    private static String getText(JSONObject json) {
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
        return String.join(" ", result);
    }
}