package com.example.gpslog;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DatabaseHandler extends SQLiteOpenHelper {

	public DatabaseHandler(Context context) {
		super(context, "tracks.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        String CREATE_TRACKS_TABLE = "CREATE TABLE tracks (" +
        		"time TEXT ," +
        		"latitude REAL," +
        		"longitude REAL," +
        		"speed REAL,"+
        		"updateStatus TEXT," +
        		"serial TEXT)";
        db.execSQL(CREATE_TRACKS_TABLE);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tracks");
        onCreate(db);
	}
	
	public void insertRow(String time, Double latitude, Double longitude, Float speed, String serial) {
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", time);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("speed", speed);
        values.put("updateStatus", "no");
        values.put("serial", serial);
        
        db.insert("tracks", null, values);
        db.close(); 
	}

	public List<Track> getAllTracks() {
	    List<Track> trackList = new ArrayList<Track>();
        String selectQuery = "SELECT latitude, longitude, speed, time FROM tracks";
 
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        System.out.println("lskfajsd;lkfjaslkdfjalskdfjsldkdfjl----------------" + cursor.getCount());
 
        if (cursor.moveToFirst()) {
            do {
                Track track = new Track();
                track.time = cursor.getString(0);
                track.latitude = Double.parseDouble(cursor.getString(1));
                track.longitude = Double.parseDouble(cursor.getString(2));
                //track.speed = Float.parseFloat(cursor.getString(3));
                //track.serial = cursor.getString(4);
                trackList.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return trackList;
    }

    /**
     * used in AndroidDatabaseManager
     * @return
     */
	
	public ArrayList<Cursor> getData(String Query){
		//get writable database
		SQLiteDatabase sqlDB = this.getWritableDatabase();
		String[] columns = new String[] { "mesage" };
		//an array list of cursor to save two cursors one has results from the query 
		//other cursor stores error message if any errors are triggered
		ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
		MatrixCursor Cursor2= new MatrixCursor(columns);
		alc.add(null);
		alc.add(null);
		
		
		try{
			String maxQuery = Query ;
			//execute the query results will be save in Cursor c
			Cursor c = sqlDB.rawQuery(maxQuery, null);
			

			//add value to cursor2
			Cursor2.addRow(new Object[] { "Success" });
			
			alc.set(1,Cursor2);
			if (null != c && c.getCount() > 0) {

				
				alc.set(0,c);
				c.moveToFirst();
				
				return alc ;
			}
			return alc;
		} catch(SQLException sqlEx){
			Log.d("printing exception", sqlEx.getMessage());
			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		} catch(Exception ex){

			Log.d("printing exception", ex.getMessage());

			//if any exceptions are triggered save the error message to cursor an return the arraylist
			Cursor2.addRow(new Object[] { ""+ex.getMessage() });
			alc.set(1,Cursor2);
			return alc;
		}

		
	}
	
    /**
     * Compose JSON out of SQLite records
     * @return
     */
    public String composeJSONfromSQLite(){
        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        
        String selectQuery = "SELECT  * FROM tracks where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
            	
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("time", cursor.getString(0));
				map.put("latitude", cursor.getString(1));
				map.put("longitude", cursor.getString(2));
				map.put("speed", cursor.getString(3));
				map.put("serial", cursor.getString(5));
				
				System.out.print( "inside JSON file first data " + cursor.getString(0) + " Second data " + cursor.getString(1) + " third data " + cursor.getString(2) + " fourth data " + cursor.getString(3)+ " fifth data" + cursor.getString(4));
                trackList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        return gson.toJson(trackList);
    }
    
    /**
     * Get SQLite records that are yet to be Synced
     * @return
     */
    public int dbSyncCount(){
        int count = 0;
        String selectQuery = "SELECT  * FROM tracks where updateStatus = '"+"no"+"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        count = cursor.getCount();
        database.close();
        return count;
    }
    
    /**
     * Update Sync status against each User ID
     */
    public void updateSyncStatus(String time, String status){
        SQLiteDatabase database = this.getWritableDatabase();     
        String updateQuery = "Update tracks set updateStatus = '"+ status +"' where time="+"'"+ time +"'";
        Log.d("query",updateQuery);        
        database.execSQL(updateQuery);
        database.close();
    }
    
    /**
     * Get Sync status of SQLite
     * @return
     */
    public String getSyncStatus(){
        String msg = null;
        if(this.dbSyncCount() == 0){
            msg = "SQLite and Remote MySQL DBs are in Sync!";
        }else{
            msg = "DB Sync needed\n";
        }
        return msg;
    }
    
    
}
