package com.example.sunshine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Build;
import android.preference.PreferenceManager;

public class MainActivity extends ActionBarActivity {
	
	private static final String LOG_TAG = MainActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new ForeCastFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		if (id == R.id.action_map) {
			openPreferredLocationOnMap();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	void openPreferredLocationOnMap(){
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		String location = sharedPrefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
		
		Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
				.appendQueryParameter("q", location)
				.build();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geoLocation);
		
		if(intent.resolveActivity(getPackageManager()) !=null){
			startActivity(intent);
		}
		else {
			Log.d(LOG_TAG, "counld not call");
		}
	}
	
	
}
