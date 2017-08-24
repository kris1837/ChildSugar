package com.example.childsugar.nfc_app_2.asyncTask;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;
import android.content.Context;

import com.example.childsugar.nfc_app_2.Email;
import com.example.childsugar.nfc_app_2.NetworkService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kirill on 5/25/17.
 */

public class SendEmail extends AsyncTask<Email, Void, String> {
    private Context activity;
    private File file;

    public SendEmail(Context activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Email... params) {
        // Call IM API to email file
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("from", params[0].getEmailSendFrom());
        queryParameters.put("to", params[0].getEmailSendTo());
        queryParameters.put("subject", params[0].getEmailSubject());
        queryParameters.put("body", params[0].getMessageBody());
        file = params[0].getMessageFile();
        String response;
        if (file != null ) {
            response = NetworkService.serverPostWithFile("email/SendMail", queryParameters, "userfile", file, null);
            return "Success";

        } else {
            return "File is empty";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(activity, s, Toast.LENGTH_LONG).show();
        if (file != null) {
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
