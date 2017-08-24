package com.example.childsugar.nfc_app_2.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.childsugar.nfc_app_2.DBHelper;
import com.example.childsugar.nfc_app_2.FoodData;
import com.example.childsugar.nfc_app_2.R;
import com.example.childsugar.nfc_app_2.adapter.FoodListAdapter;

import java.util.ArrayList;

/**
 * Created by kirill on 5/11/17.
 */

public class FoodListActivity extends AppCompatActivity {
    public static final String HISTORY_ID = "history_id";
    private long historyId = 0;
    FoodListAdapter foodListAdapter;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        historyId = getIntent().getExtras().getLong(HISTORY_ID);

        ListView foodList = (ListView) findViewById(R.id.food_list);
        foodListAdapter = new FoodListAdapter(getApplicationContext(), getFoodData());
        foodList.setAdapter(foodListAdapter);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_save:
                ArrayList<FoodData> foodDatas = foodListAdapter.getFoodDatas();
                DBHelper db = DBHelper.getInstance(this);
                for (int i = 0; i < foodDatas.size(); i++) {
                    db.insertFood(foodDatas.get(i), historyId);
                }
                if (sharedPref.getString("send_email", "").equals("1")) {
                    MainActivity.sendReportEmail(this);
                }
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.food_picker_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuSave = menu.findItem(R.id.menu_save);
        menuSave.setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    private ArrayList<FoodData> getFoodData(){
        ArrayList<FoodData> foodDatas = new ArrayList<>();
        foodDatas.add(new FoodData("Juice", R.drawable.ic_012_juice, 1));
        foodDatas.add(new FoodData("Potatoes", R.drawable.ic_002_potatoes, 0));
        foodDatas.add(new FoodData("Eggs", R.drawable.ic_003_egg, 0));
        foodDatas.add(new FoodData("Meat", R.drawable.ic_004_meat, 0));
        foodDatas.add(new FoodData("Cereal", R.drawable.ic_005_cereals, 0));
        foodDatas.add(new FoodData("Cheese", R.drawable.ic_006_cheese, 0));
        foodDatas.add(new FoodData("Cake", R.drawable.ic_007_birthday_cake, 0));
        foodDatas.add(new FoodData("Tomatoes", R.drawable.ic_008_tomato, 0));
        foodDatas.add(new FoodData("Burrito", R.drawable.ic_009_burrito, 0));
        foodDatas.add(new FoodData("Sandwich", R.drawable.ic_010_sandwich, 0));
        foodDatas.add(new FoodData("Fish", R.drawable.ic_011_fish, 0));
        return foodDatas;
    }
}
