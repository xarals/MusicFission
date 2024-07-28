package com.xaral.musicfission.ytmusicapi;

import okhttp3.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class RequestService {
    private static final JSONObject defaultBody;

    static {
        try {
            defaultBody = new JSONObject("{\"browseId\": \"UCDJmm4_R_o-KbnqQkhjvUiw\", \"context\": {\"client\": {\"clientName\": \"WEB_REMIX\", \"clientVersion\": \"1.20240225.01.00\", \"hl\": \"en\"}, \"user\": {}}}");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static JSONObject makeRequest(String url, JSONObject body) throws JSONException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .callTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
        JSONObject finalBody = defaultBody;
        for (int i = 0; i < body.names().length(); i++)
            finalBody.put(body.names().getString(i), body.get(body.names().getString(i)));
        RequestBody requestBody = RequestBody.create(JSON, finalBody.toString());
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .addHeader("accept-language", "en-US,en")
                .post(requestBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                throw new JSONException("Internet Error");
            return new JSONObject(response.body().string());
        } catch (Exception e) {
            throw new JSONException("Internet Error");
        }
    }
}
