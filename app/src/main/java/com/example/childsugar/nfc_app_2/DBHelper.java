package com.example.childsugar.nfc_app_2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper sInstance;
    SimpleDateFormat sqlDateFormat = new SimpleDateFormat("dd.MM.yyyy;HH:mm");

    //region ########################## static Strings ##########################
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_LASTSCAN = "lastscan";
    public static final String TABLE_SENSOR = "sensor";
    public static final String TABLE_SCANS = "scan";
    public static final String TABLE_DIET = "diet";

    public static final String KEY_HISTORY_ID = "_id";
    public static final String KEY_HISTORY_DATE = "date";
    public static final String KEY_HISTORY_GLUCOSELEVEL = "glucoselevel";
    public static final String KEY_HISTORY_COMMENT = "comment";

    public static final String KEY_LASTSCAN_ID = "_id";
    public static final String KEY_LASTSCAN_TYPE = "type";  //h:history; t:trent; p:prediction
    public static final String KEY_LASTSCAN_DATE = "date";
    public static final String KEY_LASTSCAN_SENSORTIME = "sensortime";
    public static final String KEY_LASTSCAN_GLUCOSELEVEL = "glucoselevel";
    public static final String KEY_LASTSCAN_FIRSTHB = "firsthb";
    public static final String KEY_LASTSCAN_THIRDB = "thirdb";
    public static final String KEY_LASTSCAN_FOURTHB = "fourthb";
    public static final String KEY_LASTSCAN_FIFTHB = "fifthb";
    public static final String KEY_LASTSCAN_ERROR = "error";

    public static final String KEY_SENSOR_ID = "_id";
    public static final String KEY_SENSOR_SENSORID = "sensorid";
    public static final String KEY_SENSOR_VERSION = "version";
    public static final String KEY_SENSOR_TIMELEFT = "timeleft";
    public static final String KEY_SENSOR_LSST = "lastscansensortime";
    public static final String KEY_SENSOR_STARTDATE = "startdate";
    public static final String KEY_SENSOR_EXPIREDATE = "expiredate";

    public static final String KEY_SCAN_ID = "_id";
    public static final String KEY_SCAN_DATE = "date";
    public static final String KEY_SCAN_GLUCOSELEVEL = "glucoselevel";
    public static final String KEY_SCAN_PREDICTION = "prediction";
    public static final String KEY_SCAN_COMMENT = "comment";
    public static final String _ID = "_id";
    public static final String KEY_FOOD = "food"; // string
    public static final String KEY_AMOUNT = "amount"; // string
    public static final String KEY_FOOD_TYPE = "type"; // int
    public static final String REF_KEY_HISTORY = "history_ref"; //

    private static final String DATABASE_NAME = "liapp.db";
    private static final int DATABASE_VERSION = 1;
    //endregion

    public static synchronized DBHelper getInstance(Context context) {

        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        String CREATE_DIET_TABLE = " CREATE TABLE " + TABLE_DIET
                + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                REF_KEY_HISTORY + " INTEGER, " +
                KEY_FOOD + " TEXT NOT NULL DEFAULT ''," +
                KEY_AMOUNT + " TEXT NOT NULL DEFAULT ''," +
                KEY_FOOD_TYPE + " INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (" + REF_KEY_HISTORY + ") REFERENCES " + TABLE_HISTORY + " (" + KEY_HISTORY_ID + ") "
                + ")";

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY +
                "(" +
                KEY_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Define a primary key
                KEY_HISTORY_DATE + " INTEGER, " +
                KEY_HISTORY_GLUCOSELEVEL + " REAL," +
                KEY_HISTORY_COMMENT + " TEXT" +
                ")";

        String CREATE_LASTSCAN_TABLE = "CREATE TABLE " + TABLE_LASTSCAN +
                "(" +
                KEY_LASTSCAN_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_LASTSCAN_TYPE + " TEXT," +
                KEY_LASTSCAN_DATE + " TEXT," +
                KEY_LASTSCAN_SENSORTIME + " INTEGER," +
                KEY_LASTSCAN_GLUCOSELEVEL + " INTEGER," +
                KEY_LASTSCAN_FIRSTHB + " TEXT," +
                KEY_LASTSCAN_THIRDB + " TEXT," +
                KEY_LASTSCAN_FOURTHB + " TEXT," +
                KEY_LASTSCAN_FIFTHB + " TEXT," +
                KEY_LASTSCAN_ERROR + " TEXT" +
                ")";

        String CREATE_SENSOR_TABLE = "CREATE TABLE " + TABLE_SENSOR +
                "(" +
                KEY_SENSOR_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_SENSOR_SENSORID + " INTEGER," +
                KEY_SENSOR_VERSION + " INTEGER," +
                KEY_SENSOR_TIMELEFT + " INTEGER," +
                KEY_SENSOR_LSST + " INTEGER," +
                KEY_SENSOR_STARTDATE + " TEXT," +
                KEY_SENSOR_EXPIREDATE + " TEXT" +
                ")";

        String CREATE_SCANS_TABLE = "CREATE TABLE " + TABLE_SCANS +
                "(" +
                KEY_SCAN_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_SCAN_DATE + " TEXT," +
                KEY_SCAN_GLUCOSELEVEL + " INTEGER," +
                KEY_SCAN_PREDICTION + " INTEGER," +
                KEY_SCAN_COMMENT + " INTEGER" +
                ")";

        db.execSQL(CREATE_HISTORY_TABLE);
        db.execSQL(CREATE_LASTSCAN_TABLE);
        db.execSQL(CREATE_SENSOR_TABLE);
        db.execSQL(CREATE_SCANS_TABLE);
        db.execSQL(CREATE_DIET_TABLE);

    }


    public static final String JOIN_FOOD =  TABLE_HISTORY + " LEFT JOIN " + TABLE_DIET
            + " ON " + TABLE_HISTORY + "." + KEY_HISTORY_ID + "=" + TABLE_DIET + "." + REF_KEY_HISTORY;

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LASTSCAN);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIET);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public ArrayList<HistoryData> getHistoryData() {
        ArrayList<HistoryData> history = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + JOIN_FOOD + " ORDER BY " + KEY_HISTORY_DATE + " DESC";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        DatabaseUtils.dumpCursor(cursor);
        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    HistoryData historyData = new HistoryData();
                    historyData.setGlucoseLevel(cursor.getDouble(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
                    historyData.setTime(cursor.getLong(cursor.getColumnIndex(KEY_HISTORY_DATE)));
                    historyData.setHistoryId(cursor.getLong(cursor.getColumnIndex(REF_KEY_HISTORY)));
                    historyData.setFoodName(cursor.getString(cursor.getColumnIndex(KEY_FOOD)));
                    historyData.setAmount(cursor.getString(cursor.getColumnIndex(KEY_AMOUNT)));
                    historyData.setFoodtype(cursor.getInt(cursor.getColumnIndex(KEY_FOOD_TYPE)));
                    // Adding to list
                    history.add(historyData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return history;
    }

    //region ########################## add data ##########################
    public void addorUpdateHistoryGlucoseData(GlucoseData glucosedata) {

        SQLiteDatabase db = getWritableDatabase();


        db.beginTransaction();

        try {

            ContentValues values = new ContentValues();
            values.put(KEY_HISTORY_DATE, glucosedata.getDate());
            values.put(KEY_HISTORY_GLUCOSELEVEL, glucosedata.getGlucoseLevel());
            values.put(KEY_HISTORY_COMMENT, glucosedata.getComment());

            int rows = db.update(TABLE_HISTORY, values, KEY_HISTORY_DATE + "= ?", new String[]{String.valueOf(glucosedata.getDate())});

            if (rows <= 0) {
                db.insertOrThrow(TABLE_HISTORY, null, values);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
    }

    public long insertFood(FoodData foodData, long id) {
        long result = 0;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(REF_KEY_HISTORY, id);
            values.put(KEY_FOOD, foodData.getName());
            values.put(KEY_AMOUNT, foodData.getAmount());
            values.put(KEY_FOOD_TYPE, foodData.getFoodType());

            result = db.insertOrThrow(TABLE_DIET, null, values);


            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
        return result;
    }

    public long addorUpdateHistoryGlucoseData(Double glucoseData) {
        long id = 0;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues values = new ContentValues();
            values.put(KEY_HISTORY_DATE, new Date().getTime());
            values.put(KEY_HISTORY_GLUCOSELEVEL, glucoseData);
            values.put(KEY_HISTORY_COMMENT, "");

            id = db.insertOrThrow(TABLE_HISTORY, null, values);

            db.setTransactionSuccessful();
        } catch (Exception e) {
        } finally {
            db.endTransaction();
        }
        return id;
    }



    //region ########################## get Data ##########################
    // Get complete History
    public ArrayList<GlucoseData> getCompleteHistory() {
        ArrayList<GlucoseData> history = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_HISTORY + " ORDER BY " + KEY_HISTORY_DATE + " DESC";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    GlucoseData glucosedata = new GlucoseData();
                    glucosedata.setID(cursor.getInt(cursor.getColumnIndex(KEY_HISTORY_ID)));
                    glucosedata.setDate(cursor.getLong(cursor.getColumnIndex(KEY_HISTORY_DATE)));
                    glucosedata.setGlucoseLevel(cursor.getDouble(cursor.getColumnIndex(KEY_HISTORY_GLUCOSELEVEL)));
                    glucosedata.setComment(cursor.getString(cursor.getColumnIndex(KEY_HISTORY_COMMENT)));

                    // Adding to list
                    history.add(glucosedata);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return history;
    }

    public double getHistoryGlucoseMax() {
        double maxGlucose = 0;
        String selectQuery = "SELECT " + "MAX("+ KEY_HISTORY_GLUCOSELEVEL +") AS " + "max_glucose" + " FROM " + TABLE_HISTORY ;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                    maxGlucose = cursor.getDouble(cursor.getColumnIndex("max_glucose"));
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return maxGlucose;
    }

    public double getHistoryGlucoseMin() {
        double minGlucose = 0;
        String selectQuery = "SELECT " + "MIN("+ KEY_HISTORY_GLUCOSELEVEL +") AS " + "min_glucose" + " FROM " + TABLE_HISTORY ;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                minGlucose = cursor.getDouble(cursor.getColumnIndex("max_glucose"));
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return minGlucose;
    }





    public long getTimeIncrement() {
        double timeMax = getHistoryGlucoseMax();
        double timeMin = getHistoryGlucoseMin();
        if (timeMax < 0) {timeMax = timeMax * (-1);}
        if (timeMin < 0) {timeMin = timeMin * (-1);}
        double timeTotal = timeMax + timeMin;
        return (int) (timeTotal / 7);
    }


    // Get last scan
    public List<ScanData> getLastScan() {
        List<ScanData> lastscan = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_LASTSCAN;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

        try {
            if (cursor.moveToFirst()) {
                do {
                    ScanData Lastscandata = new ScanData();
                    Lastscandata.setID(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_ID)));
                    Lastscandata.setType(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_TYPE)));
                    Lastscandata.setDate(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_DATE)));
                    Lastscandata.setSensorTime(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_SENSORTIME)));
                    Lastscandata.setGlucoseLevel(cursor.getInt(cursor.getColumnIndex(KEY_LASTSCAN_GLUCOSELEVEL)));
                    Lastscandata.setFirsthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIRSTHB)));
                    Lastscandata.setThirdb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_THIRDB)));
                    Lastscandata.setFourthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FOURTHB)));
                    Lastscandata.setFifthb(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_FIFTHB)));
                    Lastscandata.setError(cursor.getString(cursor.getColumnIndex(KEY_LASTSCAN_ERROR)));

                    // Adding to list
                    lastscan.add(Lastscandata);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log.d(TAG, "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return lastscan;
    }






}
