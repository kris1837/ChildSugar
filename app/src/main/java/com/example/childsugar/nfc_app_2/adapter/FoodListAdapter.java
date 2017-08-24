package com.example.childsugar.nfc_app_2.adapter;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.content.Context;
import android.widget.TextView;

import com.example.childsugar.nfc_app_2.FoodData;
import com.example.childsugar.nfc_app_2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirill on 5/11/17.
 */

public class FoodListAdapter implements ListAdapter {
    private Context context;
    private ArrayList<FoodData> foodDatas;

    public FoodListAdapter(Context context, ArrayList<FoodData> foodDatas){
        this.context = context;
        this.foodDatas = foodDatas;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return foodDatas.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_food_picker, parent, false);

        // Spinner element
        final Spinner spinner = (Spinner) itemView.findViewById(R.id.spinner);

        // Spinner Drop down elements
        final List<String> categories = foodDatas.get(position).getAmountCategories();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, R.layout.adapter_spinner, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(R.layout.adapter_drop_down_spinner);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);


        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        TextView textView = (TextView) itemView.findViewById(R.id.textView);

        imageView.setImageResource(foodDatas.get(position).getImageResource());
        textView.setText(foodDatas.get(position).getName());

        CheckBox checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
        checkBox.setChecked(foodDatas.get(position).isChecked());
        if (foodDatas.get(position).isChecked()) {spinner.setVisibility(View.VISIBLE);} else {spinner.setVisibility(View.GONE);}

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                foodDatas.get(position).setChecked(isChecked);
                if (isChecked) {spinner.setVisibility(View.VISIBLE);} else {spinner.setVisibility(View.GONE);}
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                foodDatas.get(position).setAmount((String) spinner.getAdapter().getItem(spinnerPosition));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return itemView;
    }

    public ArrayList<FoodData> getFoodDatas() {
        ArrayList<FoodData> resultData = new ArrayList<>();
        for (int i = 0; i < foodDatas.size(); i++) {
            if (foodDatas.get(i).isChecked()) {
                resultData.add(foodDatas.get(i));
            }
        }
        return resultData;
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
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
