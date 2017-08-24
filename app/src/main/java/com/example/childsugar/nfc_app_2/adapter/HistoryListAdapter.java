package com.example.childsugar.nfc_app_2.adapter;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.content.Context;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.childsugar.nfc_app_2.HistoryData;
import com.example.childsugar.nfc_app_2.R;
import com.example.childsugar.nfc_app_2.Util;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kirill on 5/11/17.
 */

public class HistoryListAdapter implements ListAdapter {

    private Context context;
    private ArrayList<HistoryData> historyDatas;

    public HistoryListAdapter(Context context, ArrayList<HistoryData> historyDatas) {
        this.context = context;
        this.historyDatas = historyDatas;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
        return historyDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_history, parent, false);

        TextView foodTitle = (TextView) itemView.findViewById(R.id.foot_title);
        TextView glucoseLevel = (TextView) itemView.findViewById(R.id.glucose_level);
        TextView date = (TextView) itemView.findViewById(R.id.date);

        if (position != 0) {
            if (historyDatas.get(position).getHistoryId() == historyDatas.get(position - 1).getHistoryId()) {
                date.setVisibility(View.GONE);
                glucoseLevel.setVisibility(View.GONE);
            } else {
                date.setVisibility(View.VISIBLE);
                glucoseLevel.setVisibility(View.VISIBLE);
            }
        } else {
            foodTitle.setVisibility(View.VISIBLE);
            glucoseLevel.setVisibility(View.VISIBLE);
            date.setVisibility(View.VISIBLE);
        }

        if (historyDatas.get(position).getFoodName() != null && !historyDatas.get(position).getFoodName().isEmpty()) {
            foodTitle.setText(historyDatas.get(position).getFoodName() + " " + historyDatas.get(position).getAmount());
        } else {
            foodTitle.setVisibility(View.GONE);
        }
        if (historyDatas.get(position).getGlucoseLevel() != 0) {
            glucoseLevel.setText(historyDatas.get(position).getGlucoseLevel() + " mg/dl");
        } else {
            glucoseLevel.setVisibility(View.GONE);
        }

        if (historyDatas.get(position).getTime() != 0) {
            date.setText(Util.makeDisplayDateTime(new Date(historyDatas.get(position).getTime())));
        } else {
            date.setVisibility(View.GONE);
        }

        return itemView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }


}
