package com.example.sunshine.tests;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.example.sunshine.data.WeatherContract.LocationEntry;
import com.example.sunshine.data.WeatherContract.WeatherEntry;
import com.example.sunshine.data.WeatherContract;
import com.example.sunshine.data.WeatherDbHelper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestProvider extends AndroidTestCase {
	private String LOG_TAG = TestDb.class.getName();
	
	
	public void testDeleteDb() throws Throwable {
		mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);

	}

	static public String TEST_CITY_NAME = "North Pole";
	static public String TEST_LOCATION = "99705";
	static public String TEST_DATE = "20141205";

	ContentValues getLocationContentValues() {
		String name = TEST_CITY_NAME;
		String testLocationString = TEST_LOCATION;
		double testLong = 65.56;
		double testLat = 96.67;
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
		weatherValues.put(WeatherEntry.COLUMN_DATETEXT, TEST_DATE);
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

	static public void validateCursor(ContentValues expectedValues,
			Cursor cursorValues) {
		// Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
		Set<Entry<String, Object>> valueSet = expectedValues.valueSet();
		for (Entry<String, Object> entry : valueSet) {
			String columnName = entry.getKey();
			int idx = cursorValues.getColumnIndex(columnName);
			assertFalse(idx == -1);
			String expectedValue = entry.getValue().toString();
			assertEquals(expectedValue, cursorValues.getString(idx));
		}
	}

	public void testGetType() {
		String type1 = mContext.getContentResolver().getType(
				WeatherContract.WeatherEntry.CONTENT_URI);
		assertEquals(type1, WeatherEntry.CONTENT_TYPE);
		String type2 = mContext.getContentResolver().getType(
				WeatherContract.WeatherEntry.buildWeatherLocation("12345"));
		assertEquals(type2, WeatherEntry.CONTENT_TYPE);
		String type3 = mContext.getContentResolver().getType(
				WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
						"12345", "20140602"));
		assertEquals(type3, WeatherEntry.CONTENT_ITEM_TYPE);
	}

	public void testInsertReadProvider() {

		WeatherDbHelper dbHelper;
		SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
		ContentValues values = getLocationContentValues();
		long locationRowId;
		/*locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);*/
		Uri locationInsertUri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
		locationRowId = ContentUris.parseId(locationInsertUri);
		assertTrue(locationRowId != -1);
		Log.v(LOG_TAG, "new row id:" + locationRowId);

		/*Cursor cursor = db.query(LocationEntry.TABLE_NAME, null, null, null,
				null, null, null);*/
		Cursor cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI, null, null, null, null);
		
		
		
		
		if (cursor.moveToFirst()) {
			validateCursor(values, cursor);
			ContentValues weatherValues = getWeatherContentValues(locationRowId);
			Uri insertUri = mContext.getContentResolver().insert(WeatherContract.WeatherEntry.CONTENT_URI, weatherValues);
			long weatherRowId = ContentUris.parseId(insertUri);
			/*long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null,
					weatherValues);*/
			assertTrue(weatherRowId != -1);
			Log.v(LOG_TAG, "new row id: " + weatherRowId);

			/*Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME, null,
					null, null, null, null, null);*/
			Cursor weatherCursor;
			weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI, null, null, null, null);
			if (weatherCursor.moveToFirst()) {
				validateCursor(weatherValues, weatherCursor);
			} else {
				fail("no data returned");
			}
			
			weatherCursor.close();
			weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(TEST_LOCATION), null, null, null, null);
			if (weatherCursor.moveToFirst()) {
				validateCursor(weatherValues, weatherCursor);
			} else {
				fail("no data returned");
			}
			weatherCursor.close();
			weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(TEST_LOCATION, TEST_DATE), null, null, null, null);
			if (weatherCursor.moveToFirst()) {
				validateCursor(weatherValues, weatherCursor);
			} else {
				fail("no data returned");
			}
			weatherCursor.close();
			weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE), null, null, null, null);
			if (weatherCursor.moveToFirst()) {
				validateCursor(weatherValues, weatherCursor);
			} else {
				fail("no data returned");
			}
		} else {
			fail("no values returned");
		}

		// Fantastic. Now that we have a location, add some weather!

		db.close();

	}
}
