package com.example.childsugar.nfc_app_2;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * Created by kirill on 4/26/17.
 */

public class Util {
    public static String getFormattedTime() {
        Calendar now = Calendar.getInstance();
        return String.format("%04d-%02d-%02d\t%02d:%02d:%02d\t",
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1,
                now.get(Calendar.DAY_OF_MONTH),
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND));
    }


    /*public static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }*/

    public static String byteToHex(byte byte_) {
        char[] hexChars = new char[2];

        int v = byte_ & 0xFF;
        hexChars[0] = hexArray[v >>> 4];
        hexChars[1] = hexArray[v & 0x0F];

        return new String(hexChars);
    }
    //endregion

    public static double convertmgdlTommoll (int mgdl) {
        double temp;
        temp = Math.round(mgdl * 0.0555 * 10.0);

        return (temp/10);
    }

    public static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    private static final String[] monStrs = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private static final String[] dowStrs = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

    public static String makeDisplayDateTime(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        int ampm = c.get(Calendar.AM_PM);
        int hour = c.get(Calendar.HOUR);
        if (hour == 0 && ampm == Calendar.PM)    // Make sure 12 pm is displayed as 12 pm and not 0 pm
            hour = 12;

        // If it's today, return current time
        if ((now.get(Calendar.YEAR) == c.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR))) {

            return String.format("today at %d:%02d %s", hour, c.get(Calendar.MINUTE), ampm == Calendar.AM ? "am" : "pm");
        }

        // Yesterday?
        if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR) + 1) {
            return String.format("yesterday at %d:%02d %s", hour, c.get(Calendar.MINUTE), ampm == Calendar.AM ? "am" : "pm");
        }

        // Within 7 day ?
        if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR) && now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR) + 7) {
            return String.format("%s at %d:%02d %s", dowStrs[c.get(Calendar.DAY_OF_WEEK) - 1], hour, c.get(Calendar.MINUTE), ampm == Calendar.AM ? "am" : "pm");
        }

        // Return date string
        return String.format("%d %s %d at %d:%02d %s",
                c.get(Calendar.DAY_OF_MONTH), monStrs[c.get(Calendar.MONTH)], c.get(Calendar.YEAR), hour, c.get(Calendar.MINUTE), ampm == Calendar.AM ? "am" : "pm");
    }

    public static String displaySimpleDate(long myTimeAsLong) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM : HH:mm");
        return sdf.format(new Date(myTimeAsLong));
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static final String DIRECTORY_MAIN = "Child Sugar";

    /**
     * Returns directory for file storage
     *
     * @return - if there is no error returns path to the file directory
     * else returns path to download folder.
     * Possible cose of errors are empty folders from previous versions.
     **/
    public static File getExternalStoragePublicDirectory() {
        String path = Environment.getExternalStorageDirectory().getPath();
        path += "/" + DIRECTORY_MAIN;
        File finalDirectory = new File(path);
        if (finalDirectory.exists()) {
            return finalDirectory;
        } else {
            boolean ok = finalDirectory.mkdirs();
            if (ok) {
                return finalDirectory;
            } else {

                Log.e(TAG, path + ":  Directory is not created file stored in " + Environment.DIRECTORY_DOWNLOADS);
                String test = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }
        }
    }

    public static class TempFileInfo {
        public String path = null;
        public File file = null;
    }

    public static TempFileInfo createTempFile(File dir, String suffix) throws IOException {
        TempFileInfo info = new TempFileInfo();
        // Create an image file name
        String timeStamp = new SimpleDateFormat("dd_MM_yyyy_HH_mm_").format(new Date());
        String fileName = "Child_Sugar_" + timeStamp;
        info.file = File.createTempFile(
                fileName,	/* prefix */
                suffix,     /* suffix */
                dir);  		/* directory */

        // Save a file: path for use with ACTION_VIEW intents
        info.path = "file://" + info.file.getAbsolutePath();
        return info;
    }
}
