package com.example.aiclient;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostTask extends AsyncTask<Void, Void, String> {

    private String apiKey;
    private String message;
    private TaskListener listener;

    private String API_URL = "YOUR_LOCAL_IP_HERE"; // http://ip:port

    public interface TaskListener {
        void onResult(int responseCode, String body);
        void onError(String error);
    }

    public PostTask(String apiKey, String message, TaskListener listener) {
        this.apiKey = apiKey;
        this.message = message;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(Void... params) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API_URL);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Api-Key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            String escaped = message
            	    .replace("\\", "\\\\")
            	    .replace("\"", "\\\"")
            	    .replace("\n", "\\n")
            	    .replace("\r", "\\r");

            	String json = "{\"api_key\":\"" + apiKey + "\",\"message\":\"" + escaped + "\"}";

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes("UTF-8"));
            os.close();

            int code = conn.getResponseCode();

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            return code + "|" + sb.toString();

        } catch (Exception e) {
            return "ERR|" + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (result.startsWith("ERR|")) {
            listener.onError(result.substring(4));
        } else {
            int code = Integer.parseInt(result.substring(0, 3));
            String body = result.substring(4);
            listener.onResult(code, body);
        }
    }
}
