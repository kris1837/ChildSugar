package com.example.childsugar.nfc_app_2.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.childsugar.nfc_app_2.DBHelper;
import com.example.childsugar.nfc_app_2.GlucoseData;
import com.example.childsugar.nfc_app_2.HistoryData;
import com.example.childsugar.nfc_app_2.Util;
import com.example.childsugar.nfc_app_2.activity.FoodListActivity;
import com.example.childsugar.nfc_app_2.activity.MainActivity;
import com.example.childsugar.nfc_app_2.log.RemoteLogger;
import com.example.childsugar.nfc_app_2.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by kirill on 5/11/17.
 */

public class ScanFragment extends Fragment {

    Button foodPicker;

    public interface ScanFragmentInterface {
        void updateList();
    }

    ScanFragmentInterface scanFragmentInterface;

    private static final String TAG = "ScanFragment";
    private LineChart mChart;

    public ScanFragment() {
    }

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_result, container, false);

        TextView result = (TextView) view.findViewById(R.id.result);

        final EditText sugarLevel = (EditText) view.findViewById(R.id.sugarLevel);
        Button button = (Button) view.findViewById(R.id.btn_send_log);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendLog();
            }
        });

        Button manualEnter = (Button) view.findViewById(R.id.manual_enter);
        manualEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sugarLevel.getVisibility() == View.GONE) {
                    sugarLevel.setVisibility(View.VISIBLE);
                } else {
                    sugarLevel.setVisibility(View.GONE);
                    long historyId = 0;
                    double glucoseLevel = 0;
                    try {
                        glucoseLevel = Double.valueOf(sugarLevel.getText().toString());
                    } catch (NumberFormatException e) {
                        glucoseLevel = 0;
                    }
                    if (sugarLevel.getText().toString().isEmpty()) {
                        Toast.makeText(getActivity(), "Please key in glucose level", Toast.LENGTH_LONG).show();
                    } else {
                        if (glucoseLevel > 10000 || glucoseLevel < 1) {
                            Toast.makeText(getActivity(), "The value is not real please enter another value", Toast.LENGTH_LONG).show();
                        } else {
                            DBHelper db = DBHelper.getInstance(getActivity());
                            historyId = db.addorUpdateHistoryGlucoseData(glucoseLevel);
                            Intent intent = new Intent(getContext(), FoodListActivity.class);
                            intent.putExtra(FoodListActivity.HISTORY_ID, historyId);
                            getContext().startActivity(intent);
                            scanFragmentInterface.updateList();
                        }
                    }
                }
                /*mChart = (LineChart) view.findViewById(R.id.linechart);
                setChart();*/
            }
        });

        /*foodPicker = (Button) view.findViewById(R.id.food_picker);
        foodPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        mChart = (LineChart) view.findViewById(R.id.linechart);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setChart();
    }

    public static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm") ;
    SimpleDateFormat chartdateFormat = new SimpleDateFormat("HH:mm") ;

    private static Date addMinutesToDate(int minutes, Date beforeTime){
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS));
        return afterAddingMins;
    }

    public Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void setChart() {
        DBHelper db = DBHelper.getInstance(getContext());
        ArrayList<GlucoseData> historyDatas = db.getCompleteHistory();
        long increment = db.getTimeIncrement();
        if (increment == 0) {
            mChart.setNoDataText("No data yet");
        } else {
            //####################### Chart Data ########################
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = historyDatas.size() - 1; i >= 0; i--) {
                xVals.add(Util.displaySimpleDate(historyDatas.get(i).getDate()));
            }
            ArrayList<Entry> yVals = new ArrayList<Entry>();
            for (int i = 0, b = historyDatas.size() - 1; i < historyDatas.size(); i++, b--) {
                yVals.add(new Entry((float) historyDatas.get(b).getGlucoseLevel(), i));
            }
            LineDataSet set1;

            // create a dataset and give it a type
            set1 = new LineDataSet(yVals, "Glucose Level");
            set1.setFillAlpha(110);


            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);


            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(xVals, dataSets);

            // set data
            mChart.setData(data);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (foodPicker != null) {
            foodPicker.setVisibility(View.GONE);
        }
    }

    public void onSendLog() {
        Log.i(TAG, "Send Log");
        RemoteLogger.appendLog(getActivity(), "Send Log");

        RemoteLogger.launchSendLogWithAttachment(getActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            scanFragmentInterface = (MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SponsorListListener");
        }
    }
}
