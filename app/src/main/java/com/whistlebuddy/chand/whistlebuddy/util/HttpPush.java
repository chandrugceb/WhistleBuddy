package com.whistlebuddy.chand.whistlebuddy.util;

/**
 * Created by chand on 27-01-2018.
 */
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpPush {
    private final String ContentType = "application/json";
    private final String Authorization = "key=AIzaSyDyDft2S01ofEKsnOthABb4qRpDXbEE_kw";
    // HTTP POST request
    public void sendPost(String message, String RegId, String SenderId) throws Exception {
        new NetworkAsyncTask().execute(message, RegId, SenderId);
    }

    class NetworkAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try{
            String url = "https://fcm.googleapis.com/fcm/send";
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", ContentType);
            con.setRequestProperty("Authorization", Authorization);
            String urlParameters = "{\r\n" +
                    "   \"notification\": {\r\n" +
                    "      \"title\": \"Alert!\",\r\n" +
                    "      \"body\": \""+params[0]+"\",\r\n" +
                    "   },\r\n" +
                    "   \"data\": {\r\n" +
                    "      \"senderid\": \""+params[2]+"\",\r\n" +
                    "   },\r\n" +
                    "   \"to\": \""+params[1]+"\"\r\n" +
                    "}";
            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            //System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());}
            catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

}
