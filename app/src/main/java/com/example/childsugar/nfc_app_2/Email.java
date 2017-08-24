package com.example.childsugar.nfc_app_2;

import android.widget.Toast;

import java.io.File;

/**
 * Created by kirill on 5/26/17.
 */

public class Email {
    private String emailSendFrom;
    private String  emailSendTo;
    private String emailSubject;
    private String messageBody;
    private File messageFile;

    public Email(String emailSendFrom, String emailSendTo, String emailSubject, String messageBody, File messageFile) {
        this.emailSendFrom = emailSendFrom;
        this.emailSendTo = emailSendTo;
        this.emailSubject = emailSubject;
        this.messageBody = messageBody;
        this.messageFile = messageFile;
    }

    public String getEmailSendFrom() {
        return emailSendFrom;
    }

    public String getEmailSendTo() {
        return emailSendTo;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public File getMessageFile() {
        return messageFile;
    }
}
