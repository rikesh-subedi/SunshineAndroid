/**
 * 
 */
package com.example.sunshine.data;

import com.example.sunshine.data.WeatherContract.LocationEntry;
import com.example.sunshine.data.WeatherContract.WeatherEntry;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.OpenableColumns;

/**
 * @author I077592
 *
 */
public class WeatherProvider extends ContentProvider {
	
	private static final int WEATHER = 1;
	private static final int WEATHER_WITH_LOCATION =2;
	private static final int WEATHER_WITH_LOCATION_AND_DATE = 3;
	private static final int LOCATION = 4;
	private static final int LOCATION_ID = 5;
	private static UriMatcher sUriMatcher = buildUriMatcher();
	private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;
	static {
		sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
		sWeatherByLocationSettingQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME+" INNER JOIN "+
				WeatherContract.LocationEntry.TABLE_NAME + " ON "+
				WeatherContract.WeatherEntry.TABLE_NAME + "."+WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = "+
				WeatherContract.LocationEntry.TABLE_NAME+ "."+WeatherContract.LocationEntry._ID
				
				);
	}
	private static final String sLocationSettingSelection =
			WeatherContract.LocationEntry.TABLE_NAME +
				"."+WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "= ?";
	private static final String sLocationSettingWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ? ";
	private static final String sLocationSettingWithDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME+
                    "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ? ";
	
	private static WeatherDbHelper dbHelper;
	private static UriMatcher buildUriMatcher() {
		UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		String authority = WeatherContract.CONTENT_AUTHORITY;
		uriMatcher.addURI(authority,WeatherContract.PATH_WEATHER, WEATHER);
		uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER+"/*", WEATHER_WITH_LOCATION);
		uriMatcher.addURI(authority, WeatherContract.PATH_WEATHER+"/*/*", WEATHER_WITH_LOCATION_AND_DATE);
		
		uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
		uriMatcher.addURI(authority, WeatherContract.PATH_LOCATION+"/#", LOCATION_ID);
		
		return uriMatcher;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new WeatherDbHelper(getContext());
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
	
		// Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case WEATHER_WITH_LOCATION_AND_DATE:
            {
                retCursor = getWeatherByLocationDateSetting(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case WEATHER_WITH_LOCATION: {
                retCursor =  getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case WEATHER: {
                retCursor = dbHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location/*"
            case LOCATION_ID: {
                retCursor = dbHelper.getReadableDatabase().query(WeatherContract.LocationEntry.TABLE_NAME, projection, WeatherContract.LocationEntry._ID +"="+ContentUris.parseId(uri), null, null, null, null);
                
                break;
            }
            // "location"
            case LOCATION: {
            	 retCursor = dbHelper.getReadableDatabase().query(
            			 WeatherContract.LocationEntry.TABLE_NAME,
                         projection,
                         selection,
                         selectionArgs,
                         null,
                         null,
                         sortOrder
                 );
                break;
            }
 
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
	    final int match = sUriMatcher.match(uri);
	    switch(match) {
	    case WEATHER: return WeatherEntry.CONTENT_TYPE; 
	    case WEATHER_WITH_LOCATION: return WeatherEntry.CONTENT_TYPE; 
	    case WEATHER_WITH_LOCATION_AND_DATE: return WeatherEntry.CONTENT_ITEM_TYPE;
	    case LOCATION: return LocationEntry.CONTENT_TYPE;
	    case LOCATION_ID: return LocationEntry.CONTENT_ITEM_TYPE;
	   
	    default: throw new UnsupportedOperationException("Unknown URI:" +uri);
	    }
		
		
		
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Uri returnUri = null;
		// TODO Auto-generated method stub
		switch(sUriMatcher.match(uri)){
		case WEATHER: {
			long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
			if(_id >0  ) returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
			else throw new android.database.SQLException("Failed to insert row into :"+uri);
			
			
			break;
		}
		case LOCATION:{
			long _idLoc = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
			if(_idLoc > 0) returnUri = WeatherContract.LocationEntry.buildLocationUri(_idLoc);
			else throw new android.database.SQLException("Failed to insert row into :"+uri);
			break;
		}
		default: throw new android.database.SQLException("Failed to insert row into :"+uri); 
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int rowsDeleted;
		// TODO Auto-generated method stub
		switch(sUriMatcher.match(uri)){
		case WEATHER: {
			
			rowsDeleted = db.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
			
			break;
		}
		case LOCATION:{
			rowsDeleted = db.delete(WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
			break;
		}
		default: throw new UnsupportedOperationException("Unknown uri: "+uri); 
		}
		if(null == selection || 0 !=rowsDeleted) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		
		return rowsDeleted;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	private Cursor getWeatherByLocationDateSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String date = WeatherContract.WeatherEntry.getDateFromUri(uri);
        String[] selectionArgs;
        String selection;
        if(date == null){
        	selection = sLocationSettingSelection;
        	selectionArgs = new String[]{locationSetting};
        } 
        else {
        	selection = sLocationSettingWithDateSelection;
        	selectionArgs = new String[] {locationSetting, date};
        }
        return sWeatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }
	 private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
	        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
	        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
	        
	        String[] selectionArgs;
	        String selection;
	        if(startDate == null ){
	        	selection = sLocationSettingSelection;
	        	selectionArgs = new String[]{locationSetting};
	        }
	        else {
	        	selection = sLocationSettingWithDateSelection;
	        	selectionArgs = new String[] {locationSetting, startDate};
	        }
	        return sWeatherByLocationSettingQueryBuilder.query(dbHelper.getReadableDatabase(),
	                projection,
	                selection,
	                selectionArgs,
	                null,
	                null,
	                sortOrder
	        );
	    }

}
