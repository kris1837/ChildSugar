package com.example.childsugar.nfc_app_2.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.childsugar.nfc_app_2.DBHelper;

import com.example.childsugar.nfc_app_2.R;
import com.example.childsugar.nfc_app_2.adapter.FoodListAdapter;
import com.example.childsugar.nfc_app_2.adapter.HistoryListAdapter;

/**
 * Created by kirill on 5/11/17.
 */

public class HistoryFragment extends Fragment {

    public HistoryFragment(){}
    ListView foodList;
    DBHelper db;

    HistoryListAdapter historyListAdapter;

    public static HistoryFragment newInstance(){
        return new HistoryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);
        db = DBHelper.getInstance(getActivity());
        foodList = (ListView) view.findViewById(R.id.history_list);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();



        if (foodList != null && db != null) {
            historyListAdapter = new HistoryListAdapter(getContext(), db.getHistoryData());
            foodList.setAdapter(historyListAdapter);
        }
    }

    public void updateList(){
        if (foodList != null && db != null) {
            historyListAdapter = new HistoryListAdapter(getContext(), db.getHistoryData());
            foodList.setAdapter(historyListAdapter);
        }
    }
}
