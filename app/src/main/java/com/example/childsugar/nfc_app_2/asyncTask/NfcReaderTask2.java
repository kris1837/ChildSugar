package com.example.childsugar.nfc_app_2.asyncTask;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.childsugar.nfc_app_2.DBHelper;
import com.example.childsugar.nfc_app_2.R;
import com.example.childsugar.nfc_app_2.Util;
import com.example.childsugar.nfc_app_2.activity.FoodListActivity;
import com.example.childsugar.nfc_app_2.activity.MainActivity;
import com.example.childsugar.nfc_app_2.log.RemoteLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by kirill on 5/22/17.
 */

public class NfcReaderTask2 extends AsyncTask<Tag, Void, Float> {
    private String lectura, buffer;
    private float currentGlucose = 0f;

    Activity activity;

    public NfcReaderTask2(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPostExecute(Float result) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(activity.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
        //if (result > 1) {
            try {
                double glucoseLevel = result;
                DBHelper db = DBHelper.getInstance(activity);
                final long historyId = db.addorUpdateHistoryGlucoseData(glucoseLevel);
                Button foodPicker = (Button) activity.findViewById(R.id.food_picker);
                if (foodPicker != null) {
                    foodPicker.setVisibility(View.VISIBLE);
                    foodPicker.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(activity, FoodListActivity.class);
                            intent.putExtra(FoodListActivity.HISTORY_ID, historyId);
                            activity.startActivity(intent);
                            if (activity instanceof MainActivity) {
                                ((MainActivity) activity).updateList();
                            }
                        }
                    });
                }

            } catch (NumberFormatException e) {
                Toast.makeText(activity, "Please scan again", Toast.LENGTH_LONG).show();
            }
        /*} else {
            Toast.makeText(activity, "Please scan again", Toast.LENGTH_LONG).show();
        }*/
    }


    @Override
    protected Float doInBackground(Tag... params) {
        Tag tag = params[0];

        NfcV nfcvTag = NfcV.get(tag);
        Log.d("socialdiabetes", "Enter NdefReaderTask: " + nfcvTag.toString());
        RemoteLogger.appendLog(activity, "Enter NdefReaderTask: " + nfcvTag.toString());

        Log.d("socialdiabetes", "Tag ID: " + tag.getId());
        RemoteLogger.appendLog(activity, "Tag ID: " + tag.getId());

        try {
            nfcvTag.connect();
        } catch (IOException e) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Error opening NFC connection!", Toast.LENGTH_SHORT).show();
                }
            });

            return -1f;
        }

        lectura = "";

        byte[][] bloques = new byte[40][8];
        byte[] allBlocks = new byte[40 * 8];


        Log.d("socialdiabetes", "---------------------------------------------------------------");
        RemoteLogger.appendLog(activity, "---------------------------------------------------------------");
        //Log.d("socialdiabetes", "nfcvTag ID: "+nfcvTag.getDsfId());

        //Log.d("socialdiabetes", "getMaxTransceiveLength: "+nfcvTag.getMaxTransceiveLength());
        try {

            // Get system information (0x2B)
            byte[] cmd = new byte[]{
                    (byte) 0x00, // Flags
                    (byte) 0x2B // Command: Get system information
            };
            byte[] systeminfo = nfcvTag.transceive(cmd);

            //Log.d("socialdiabetes", "systeminfo: "+systeminfo.toString()+" - "+systeminfo.length);
            //Log.d("socialdiabetes", "systeminfo HEX: "+bytesToHex(systeminfo));

            systeminfo = Arrays.copyOfRange(systeminfo, 2, systeminfo.length - 1);

            byte[] memorySize = {systeminfo[6], systeminfo[5]};
            Log.d("socialdiabetes", "Memory Size: " + Util.bytesToHex(memorySize) + " / " + Integer.parseInt(Util.bytesToHex(memorySize).trim(), 16));

            byte[] blocks = {systeminfo[8]};
            Log.d("socialdiabetes", "blocks: " + Util.bytesToHex(blocks) + " / " + Integer.parseInt(Util.bytesToHex(blocks).trim(), 16));
            RemoteLogger.appendLog(activity, "blocks: " + Util.bytesToHex(blocks) + " / " + Integer.parseInt(Util.bytesToHex(blocks).trim(), 16));

            int totalBlocks = Integer.parseInt(Util.bytesToHex(blocks).trim(), 16);

            for (int i = 3; i <= 40; i++) { // Leer solo los bloques que nos interesan
                    /*
                    cmd = new byte[] {
	                    (byte)0x00, // Flags
	                    (byte)0x23, // Command: Read multiple blocks
	                    (byte)i, // First block (offset)
	                    (byte)0x01  // Number of blocks
	                };
	                */
                // Read single block
                cmd = new byte[]{
                        (byte) 0x00, // Flags
                        (byte) 0x20, // Command: Read multiple blocks
                        (byte) i // block (offset)
                };

                byte[] oneBlock = nfcvTag.transceive(cmd);
                Log.d("socialdiabetes", "userdata: " + oneBlock.toString() + " - " + oneBlock.length);
                RemoteLogger.appendLog(activity, "userdata: " + oneBlock.toString() + " - " + oneBlock.length);
                oneBlock = Arrays.copyOfRange(oneBlock, 1, oneBlock.length);
                bloques[i - 3] = Arrays.copyOf(oneBlock, 8);


                Log.d("socialdiabetes", "userdata HEX: " + Util.bytesToHex(oneBlock));
                RemoteLogger.appendLog(activity, "userdata HEX: " + Util.bytesToHex(oneBlock));

                lectura = lectura + Util.bytesToHex(oneBlock) + "\r\n";
            }

            String s = "";
            for (int i = 0; i < 40; i++) {
                Log.d("socialdiabetes", Util.bytesToHex(bloques[i]));
                RemoteLogger.appendLog(activity, Util.bytesToHex(bloques[i]));
                s = s + Util.bytesToHex(bloques[i]);
            }

            Log.d("socialdiabetes", "S: " + s);
            RemoteLogger.appendLog(activity, "S: " + s);

            Log.d("socialdiabetes", "Next read: " + s.substring(4, 6));
            RemoteLogger.appendLog(activity, "Next read: " + s.substring(4, 6));
            int current = Integer.parseInt(s.substring(4, 6), 16);
            Log.d("socialdiabetes", "Next read: " + current);
            RemoteLogger.appendLog(activity, "Next read: " + current);
            Log.d("socialdiabetes", "Next historic read " + s.substring(6, 8));
            RemoteLogger.appendLog(activity, "Next historic read " + s.substring(6, 8));

            String[] bloque1 = new String[16];
            String[] bloque2 = new String[32];
            Log.d("socialdiabetes", "--------------------------------------------------");
            RemoteLogger.appendLog(activity, "--------------------------------------------------");
            int ii = 0;
            for (int i = 8; i < 8 + 15 * 12; i += 12) {
                Log.d("socialdiabetes", s.substring(i, i + 12));
                RemoteLogger.appendLog(activity, s.substring(i, i + 12));
                bloque1[ii] = s.substring(i, i + 12);

                final String g = s.substring(i + 2, i + 4) + s.substring(i, i + 2);

                if (current == ii) {
                    currentGlucose = glucoseReading(Integer.parseInt(g, 16));
                }
                ii++;


            }
            lectura = lectura + "Current approximate glucose " + currentGlucose;
            Log.d("socialdiabetes", "Current approximate glucose " + currentGlucose);
            RemoteLogger.appendLog(activity, "Current approximate glucose " + currentGlucose);

            Log.d("socialdiabetes", "--------------------------------------------------");
            RemoteLogger.appendLog(activity, "--------------------------------------------------");
            ii = 0;
            for (int i = 188; i < 188 + 31 * 12; i += 12) {
                Log.d("socialdiabetes", s.substring(i, i + 12));
                RemoteLogger.appendLog(activity, s.substring(i, i + 12));
                bloque2[ii] = s.substring(i, i + 12);
                ii++;
            }
            Log.d("socialdiabetes", "--------------------------------------------------");
            RemoteLogger.appendLog(activity, "--------------------------------------------------");

        } catch (IOException e) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "Error reading NFC!", Toast.LENGTH_SHORT).show();
                }
            });

            return -1f;
        }

        //addText(lectura);
        if (currentGlucose > 70) {
            currentGlucose = currentGlucose - 30;
        }

        addText("Current approximate glucose " + currentGlucose + " mg/dL");

        try {
            nfcvTag.close();
        } catch (IOException e) {
                /*
                Abbott.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error closing NFC connection!", Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
                */
        }


        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        File myFile = new File("/sdcard/fsl_" + dateFormat.format(date) + ".log");
        try {
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(lectura);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
        }


        return currentGlucose;
    }


    private void addText(final String s) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                TextView result = (TextView) activity.findViewById(R.id.result);
                if (result != null) {
                    result.setText(s);
                }
            }
        });

    }

    private void GetTime(Long minutes) {
        Long t3 = minutes;
        Long t4 = t3 / 1440;
        Long t5 = t3 - (t4 * 1440);
        Long t6 = (t5 / 60);
        Long t7 = t5 - (t6 * 60);
    }

    private float glucoseReading(int val) {
        // ((0x4531 & 0xFFF) / 6) - 37;
        int bitmask = 0x0FFF;
        return Float.valueOf(Float.valueOf((val & bitmask) / 6) - 37);
    }
}

