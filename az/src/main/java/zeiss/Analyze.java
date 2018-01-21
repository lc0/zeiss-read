package zeiss;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class Analyze {
    // **********************************************
    // *** Update or verify the following values. ***
    // **********************************************

    // Replace the analysysSubscriptionKey string value with your valid subscription key.
    public static final String analysysSubscriptionKey = "86ba3aa631c8430e8b465c9fc5bdf015";

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
    public static final String analysysUriBase = "https://westcentralus.api.cognitive.microsoft.com/vision/v1.0/analyze";


    public static void main(String[] args) {
        getAnalysys();
    }

    public static String getAnalysys() {
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

            File file = new File("/home/raz/Desktop/cat.jpg");
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
}