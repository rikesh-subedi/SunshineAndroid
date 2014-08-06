package com.example.sunshine.tests;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.sunshine.data.WeatherContract.LocationEntry;
import com.example.sunshine.data.WeatherContract.WeatherEntry;
import com.example.sunshine.data.WeatherDbHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestDb extends AndroidTestCase {
	private String LOG_TAG = TestDb.class.getName();
	public void testCreateDb() throws Throwable {
		mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
		SQLiteDatabase db  = new WeatherDbHelper(mContext).getWritableDatabase();
		assertEquals(true, db.isOpen());
		db.close();
		
	}
	String cityName = "North Pole";
	ContentValues getLocationContentValues() {
		String name = cityName;
		String testLocationString = "560093";
		double testLong = 65.56;
		double testLat =  96.67;
		ContentValues values = new ContentValues();
		values.put(LocationEntry.COLUMN_CITY_NAME, name);
		values.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationString);
		values.put(LocationEntry.COLUMN_COORD_LAT, testLat);
		values.put(LocationEntry.COLUMN_COORD_LONG, testLong);
		
		return values;
	}
	ContentValues getWeatherContentValues(long locationRowId) {
		 	ContentValues weatherValues = new ContentValues();
	        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
	        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
	        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
	        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
	        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
	        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
	        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
	        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
	        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
	        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
	        return weatherValues;
	}
	static public void validateCursor(ContentValues expectedValues, Cursor cursorValues){
		//Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
		Set<Entry<String, Object>> valueSet = expectedValues.valueSet();
		for( Entry<String, Object> entry: valueSet) {
			String columnName = entry.getKey();
			int idx = cursorValues.getColumnIndex(columnName);
			assertFalse(idx == -1);
			String expectedValue = entry.getValue().toString();
			assertEquals(expectedValue, cursorValues.getString(idx));
		}
	}
	public void testInsertReadDb() {
		
	
		WeatherDbHelper dbHelper;
		SQLiteDatabase db  =  new WeatherDbHelper(mContext).getWritableDatabase();
		ContentValues values = getLocationContentValues();		
		long locationRowId;
		locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
		assertTrue(locationRowId !=-1);
		Log.v(LOG_TAG , "new row id:"+ locationRowId);
		
		Cursor cursor = db.query(LocationEntry.TABLE_NAME, null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			validateCursor(values, cursor);
			ContentValues weatherValues = getWeatherContentValues(locationRowId);
			long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
	        assertTrue(weatherRowId != -1);
	        Log.v(LOG_TAG, "new row id: "+weatherRowId);
	        
	        Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
	        if(weatherCursor.moveToFirst()){
	        	validateCursor(weatherValues, weatherCursor);
	        } else {
	        	fail("no data returned");
	        }
			
		} else {
			fail("no values returned");
		}
		
		// Fantastic.  Now that we have a location, add some weather!
        
        
        
        
		db.close();
		
	}
}
