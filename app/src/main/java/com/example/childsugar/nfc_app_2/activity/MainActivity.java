package com.example.childsugar.nfc_app_2.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.crashlytics.android.Crashlytics;
import com.example.childsugar.nfc_app_2.AppStatus;
import com.example.childsugar.nfc_app_2.DBHelper;
import com.example.childsugar.nfc_app_2.Email;
import com.example.childsugar.nfc_app_2.HistoryData;
import com.example.childsugar.nfc_app_2.Util;
import com.example.childsugar.nfc_app_2.asyncTask.NfcReaderTask2;
import com.example.childsugar.nfc_app_2.asyncTask.SendEmail;
import com.example.childsugar.nfc_app_2.fragment.HistoryFragment;
import com.example.childsugar.nfc_app_2.fragment.ScanFragment;
import com.example.childsugar.nfc_app_2.log.RemoteLogger;
import com.example.childsugar.nfc_app_2.R;

import io.fabric.sdk.android.Fabric;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements ScanFragment.ScanFragmentInterface {
    public static final String TAG = "MainActivity";

    // NFC
    private static final int PENDING_INTENT_TECH_DISCOVERED = 1;
    private NfcAdapter mNfcAdapter;


    ScanFragment scanFragment;
    HistoryFragment historyFragment;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    Toolbar toolbar;
    DBHelper db;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        /*if (new Date(1496054293).compareTo(new Date()) > 0) {
            System.exit(0);
        }*/


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        RemoteLogger.appendLog(this, "NFC app");

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {

            Toast.makeText(this, getResources().getString(R.string.error_nfc_device_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;

        } else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.error_nfc_disabled), Toast.LENGTH_LONG).show();
            resolveIntent(this.getIntent(), false);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        NfcManager nfcManager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
        if (nfcManager != null) {
            mNfcAdapter = nfcManager.getDefaultAdapter();
        }

        if (mNfcAdapter != null) {
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }
            try {
                mNfcAdapter.isEnabled();
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }

            PendingIntent pi = createPendingResult(PENDING_INTENT_TECH_DISCOVERED, new Intent(), 0);
            if (pi != null) {
                try {

                    mNfcAdapter.enableForegroundDispatch(
                            this,
                            pi,
                            new IntentFilter[]{
                                    new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
                            },
                            new String[][]{
                                    new String[]{"android.nfc.tech.NfcV"}
                            });
                } catch (NullPointerException e) {
                    // Drop NullPointerException
                }
            }
        }

        if (historyFragment != null) {
            historyFragment.updateList();
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_share:
                Util.TempFileInfo infoShare = makeHistoryDataFile(this);
                if (infoShare != null) {
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/*");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(infoShare.path));
                    startActivity(Intent.createChooser(sharingIntent, "share file with"));
                } else {
                    Toast.makeText(this, "File is not generated", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.email:
                return sendReportEmail(this);
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void setAlaram() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<Boolean> alarams = new ArrayList<>(4);
        alarams.add(0, sharedPref.getBoolean("morning_9", true));
        alarams.add(1, sharedPref.getBoolean("evening_2", true));
        alarams.add(2, sharedPref.getBoolean("evening_7", true));
        alarams.add(3, sharedPref.getBoolean("evening_10", true));

    }

    public static boolean sendReportEmail(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (AppStatus.getInstance(context).isOnline()) {
            Util.TempFileInfo infoEmail = makeHistoryDataFile(context);
            if (infoEmail != null) {
                String emailSendFrom = sharedPref.getString("email", "");
                if (emailSendFrom.isEmpty()) {
                    Toast.makeText(context, "Please key in First email", Toast.LENGTH_LONG).show();
                    return true;
                }
                String emailSendTo = sharedPref.getString("email2", "");
                if (emailSendTo.isEmpty()) {
                    emailSendTo = emailSendFrom;
                }
                String emailSubject = "Child Sugar Report";
                String messageBody = "Child Sugar Report";

                Email email = new Email(emailSendFrom, emailSendTo, emailSubject, messageBody, infoEmail.file);
                new SendEmail(context).execute(email);
            }
        } else {
            Toast.makeText(context,"You are not online!!!!", Toast.LENGTH_LONG).show();
        }
        return true;
    }

    public static Util.TempFileInfo makeHistoryDataFile(Context context) {
        Util.TempFileInfo info = null;
        DBHelper db = DBHelper.getInstance(context);
        ArrayList<HistoryData> historyDatas = db.getHistoryData();
        try {
            String text = makeFileContent(historyDatas, context);
            info = Util.createTempFile(Util.getExternalStoragePublicDirectory(), ".txt");
            if (writeStringAsFile(text, info)) {
                return info;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String makeFileContent(ArrayList<HistoryData> historyDatas, Context context) {
        StringBuilder result = new StringBuilder("Results from Child Sugar");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref != null) {
            result.append(String.format("%n"));
            String userName = sharedPref.getString("name", "Not set");
            result.append("User name: ");
            result.append(userName);
        }
        result.append(String.format("%n"));
        for (int i = 0; i < historyDatas.size(); i++) {
            if (i != 0) {
                if (historyDatas.get(i).getHistoryId() == historyDatas.get(i - 1).getHistoryId()) {
                    if (historyDatas.get(i).getFoodName() != null && !historyDatas.get(i).getFoodName().isEmpty()) {
                        result.append(", ");
                        result.append(historyDatas.get(i).getFoodName() + " " + historyDatas.get(i).getAmount());
                    }
                } else {
                    result.append(String.format("%n"));
                    result.append("Time: ");
                    String date = new SimpleDateFormat("dd/MM/yyyy_HH:mm").format(historyDatas.get(i).getTime());
                    result.append(date);
                    result.append(" Glucose Level: ");
                    result.append(historyDatas.get(i).getGlucoseLevel());
                    result.append(" mg/dL ");
                    if (historyDatas.get(i).getFoodName() != null && !historyDatas.get(i).getFoodName().isEmpty()) {
                        result.append("Nutrition: " + historyDatas.get(i).getFoodName() + " " + historyDatas.get(i).getAmount());
                    }
                }
            } else {
                result.append(String.format("%n"));
                result.append("Time: ");
                String date = new SimpleDateFormat("dd/MM/yyyy_HH:mm").format(historyDatas.get(i).getTime());
                result.append(date);
                result.append(" Glucose Level: ");
                result.append(historyDatas.get(i).getGlucoseLevel());
                result.append(" mg/dL ");
                if (historyDatas.get(i).getFoodName() != null && !historyDatas.get(i).getFoodName().isEmpty()) {
                    result.append("Nutrition: " + historyDatas.get(i).getFoodName() + " " + historyDatas.get(i).getAmount());
                }
            }


        }
        return result.toString();
    }

    public static boolean writeStringAsFile(final String fileContents, Util.TempFileInfo info) {
        boolean result = false;
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(info.file, true);
            FileWriter out;
            try {
                out = new FileWriter(fos.getFD());
                out.write(fileContents);
                out.close();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
            } finally {
                fos.getFD().sync();
                fos.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            try {
                // Disable foreground dispatch:
                //mNfcAdapter.disableForegroundDispatch(this);
            } catch (NullPointerException e) {
                // Drop NullPointerException
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent, true);
    }

    private void resolveIntent(Intent data, boolean foregroundDispatch) {
        this.setIntent(data);
        String action = data.getAction();
        if ((data.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            Tag tag = data.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            Date now = new Date();

            if (foregroundDispatch) {  //10000 = 10sec
                String[] techList = tag.getTechList();
                String searchedTech = NfcV.class.getName();

                // ###################### read Tag ######################
                new NfcReaderTask2(this).execute(tag);
                // ######################################################
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PENDING_INTENT_TECH_DISCOVERED:
                // Resolve the foreground dispatch intent:
                resolveIntent(data, true);
                break;
        }
    }

    @Override
    public void updateList() {
        if (historyFragment != null) {
            historyFragment.updateList();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_placeholder, container, false);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    scanFragment = ScanFragment.newInstance();
                    return scanFragment;

                case 1:
                    historyFragment = HistoryFragment.newInstance();
                    return historyFragment;

            }

            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SCAN";

                case 1:
                    return "HISTORY";
            }

            return null;
        }
    }

}
