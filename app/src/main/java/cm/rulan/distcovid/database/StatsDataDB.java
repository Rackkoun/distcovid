package cm.rulan.distcovid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cm.rulan.distcovid.model.DiscovidModelObject;

import java.util.ArrayList;
import java.util.List;

public class StatsDataDB extends SQLiteOpenHelper {

    private static final String TAG = "StatsDB";
    private static final String DBNAME ="discovid.db";
    private static final int DBVERSION = 1;

    // Datenbanken-Tabelle
    private static final String WARNING_TABLE = "warning";

    // Columns's name
    private static final String _ID = "_id";
    private static final String COLUMN_DISTANCE = "distance"; // in meter
    private static final String COLUMN_DATETIME = "datetime";
    private static final String COLUMN_DURING = "during"; // in minutes

    // drop table
    private static final String DROP_WARNING_TABLE = "DROP TABLE IF EXISTS " + WARNING_TABLE;

    private SQLiteDatabase db;

    public StatsDataDB(Context context){
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            String sqlQuery = "CREATE TABLE "+ WARNING_TABLE +
                    "("+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_DISTANCE + " REAL NOT NULL, " +
                    COLUMN_DURING + " INTEGER, " +
                    COLUMN_DATETIME + " TEXT);";

            db.execSQL(sqlQuery);
            Log.i(TAG, "DB "+ getDatabaseName()+ " created");
        }catch (Exception e){
            Log.e(TAG, "Error while creating der DB: "+ e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL(DROP_WARNING_TABLE);

            onCreate(db);
            Log.d(TAG, "DB "+ getDatabaseName()+ " re-created");
        }catch (Exception e){
            Log.e(TAG, "Error while re-creating the DB: " + e.getMessage());
        }
    }

    // insert value int the DB
    public void insertValue(double distance, int during, long datetime){
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_DISTANCE, distance);
            values.put(COLUMN_DURING, during);
            values.put(COLUMN_DATETIME, datetime);

            long id = db.insert(WARNING_TABLE, null, values);
            db.setTransactionSuccessful();
            Log.i(TAG, "Values ("+
            id+", "+distance+", "+ during +", "+ datetime+", ): successfully inserted");
        }catch (Exception e){
            Log.i(TAG, "Error while inserting values");
        }
    }

    // read db content
    public List<DiscovidModelObject> getWarnings(){
        SQLiteDatabase db = getReadableDatabase();
        List<DiscovidModelObject> warnings = new ArrayList<>();

        Cursor cursor = db.query(WARNING_TABLE,
                null, null, null,
                null, null, null);
        if (cursor.moveToFirst()){
            Log.i(TAG, "Cursor is moved to first elements");
            do {
                long id = cursor.getLong(0);
                double distance = cursor.getDouble(1);
                int during = cursor.getInt(2);
                long datetime = cursor.getLong(3);

                DiscovidModelObject modelObject = new DiscovidModelObject(distance, during, datetime);
                modelObject.set_id(id);
                Log.i(TAG, "Object got:\n"+modelObject.toString());
                warnings.add(modelObject);
            }while (cursor.moveToNext());

        }
        cursor.close(); // very important
        return warnings;
    }
}
