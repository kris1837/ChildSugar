package com.example.childsugar.nfc_app_2;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by kirill on 5/25/17.
 */

public class NetworkService extends Service implements NetworkChangeReceiver.ConnectivityEvent, Runnable{

    private static final String TAG = "NetworkService";
    private static final String SCHEME = "http";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {

    }

    @Override
    public void onConnect() {

    }

    @Override
    public void onDisconnect() {

    }

    /**
     * Posts a request to "apiName"
     * Returns null on error (exception), or response from server on success
     *
     * @param apiName         Name of API to post to
     * @param queryParameters For building up HTTP query parameters
     * @return Response from server, or null on error
     */
    public static String serverPost(String apiName, Map<String, String> queryParameters) {
        Log.d(TAG, "POST API=" + apiName);

        String response = null;

        URL url = null;

        try {
            // TODO Pick up PARAM_URL from preferences
            Uri.Builder builder = makeUri(apiName);
            url = new URL(builder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			/* YTH, 25 Feb 2017 - removed custom time-outs
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			*/

            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            builder = new Uri.Builder();
            for (Map.Entry<String, String> kvp : queryParameters.entrySet()) {
                builder.appendQueryParameter(kvp.getKey(), kvp.getValue());
            }

            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            if (conn != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String fragment;
                while ((fragment = r.readLine()) != null) {
                    sb.append(fragment);
                }

                conn.disconnect();

                response = sb.toString();
            }

            // Log request
            int count = 0;
            StringBuilder sb = new StringBuilder("POST REQ=");
            sb.append(url.toString());
            sb.append(" (");
            for (Map.Entry<String, String> param : queryParameters.entrySet()) {
                if (count++ > 0)
                    sb.append(", ");
                sb.append(param.getKey());
                sb.append("=");
                sb.append(param.getValue());
            }
            sb.append("), RESP=");
            sb.append(response);
            Log.d(TAG, sb.toString());

            // Log data usage


        } catch (MalformedURLException e) {
            Log.e(TAG, "POST REQ=" + url.toString() + ", ERR=" + e.getMessage());
            if (url != null)
                Crashlytics.log(url.toString());
            Crashlytics.logException(e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "POST REQ=" + url.toString() + ", ERR=" + e.getMessage());

            if (url != null)
                Crashlytics.log(url.toString());
            Crashlytics.logException(e);
        } catch (IOException e) {
            if (e.getMessage() != null)
                Log.e(TAG, "POST REQ=" + url.toString() + ", ERR=" + e.getMessage());

            if (url != null)
                Crashlytics.log(url.toString());
            Crashlytics.logException(e);
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e(TAG, "POST REQ=" + url.toString() + ", ERR=" + e.getMessage());

            if (url != null)
                Crashlytics.log(url.toString());
            Crashlytics.logException(e);
        }


        return response;
    }

    /**
     * Posts a request with file to "apiName"
     * Returns null on error (exception), or response from server on success
     *
     * @param apiName         Name of API to post to
     * @param queryParameters For building up HTTP query parameters
     * @return Response from server, or null on error
     */
    public static String serverPostWithFile(String apiName, Map<String, String> queryParameters, String fileField, File uploadFile, MultipartUtility.Progress progress) {
        final String charset = "UTF-8";

        long fileSize = uploadFile.length(), written = 0;

        MultipartUtility.Response response = null;
        String urlString = makeURL(apiName);
        try {
            // long startTime = System.currentTimeMillis();

            MultipartUtility multipart = new MultipartUtility(urlString, charset, progress);

            for (Map.Entry<String, String> kvp : queryParameters.entrySet()) {
                multipart.addFormField(kvp.getKey(), kvp.getValue());
            }

            written = multipart.addFilePart(fileField, uploadFile);

            response = multipart.finish();

            // long endTime = System.currentTimeMillis();

            // logServerRequest(App.getInstance(), urlString, apiName, queryParameters, response.httpStatus, response.response, startTime, endTime);

            if (response.httpStatus == HttpURLConnection.HTTP_OK) {
                return response.response;
            }
        } catch (IOException ex) {
            if (ex.getMessage() != null) {
                Log.e(TAG, ex.getMessage());
            }

            Crashlytics.log(urlString);
            Crashlytics.logException(ex);

            return null;
        }


        return null;
    }

    /**
     * Construct a Uri
     * TODO Server should not be hardcoded
     *
     * @param function Final path component in the Uri
     * @return
     */
    private static Uri.Builder makeUri(String function) {
        Uri.Builder builder = makeUri("im.cornetresearch.com", function);
        return builder;
    }

    private static Uri.Builder makeUri(String server, String function) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(server)
                .appendPath("api")
                .appendPath("im");

        String[] components = function.split("/");
        for (String c : components)
            builder.appendPath(c);

        return builder;
    }

    private static String makeURL(String function) {
        String url = SCHEME + "://" + "im.cornetresearch.com" + "/api/im/" + function;
        return url;
    }
}
