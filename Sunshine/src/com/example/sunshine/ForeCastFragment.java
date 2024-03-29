package com.example.sunshine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class ForeCastFragment extends Fragment {
	
	void updateWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
		new FetchWeatherTask().execute(location);
	}
	@Override
	public void onStart() {
		updateWeather();
		super.onStart();
	}
	private ArrayList<String> weatherList;
	private ArrayAdapter<String> ad ;
	public ForeCastFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		if(item.getItemId() == R.id.action_refresh){
			updateWeather();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.forecastfragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		String[] wl = {"Today-Sunny-80/66","Tomorrow-Cloudy-75/65","Tuesday-Rainy-30/38", "Wednesday-Sunny-85/34"};
		weatherList = new ArrayList<String>(Arrays.asList(wl));
		ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
		ad = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,  weatherList);
		listView.setAdapter(ad);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String forecast = ad.getItem(position);
				Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
				Intent newIntent;
				newIntent = new Intent(getActivity(), DetailActivity.class)
								.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(newIntent);
			}
		});
		
		
		
		return rootView;
	}
	public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

		private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
		/* The date/time conversion code is going to be moved outside the asynctask later,
		 * so for convenience we're breaking it out into its own method now.
		 */
		private String getReadableDateString(long time){
		    // Because the API returns a unix timestamp (measured in seconds),
		    // it must be converted to milliseconds in order to be converted to valid date.
		    Date date = new Date(time * 1000);
		    SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
		    return format.format(date).toString();
		}
		 
		/**
		 * Prepare the weather high/lows for presentation.
		 */
		private String formatHighLows(double high, double low) {
		    // For presentation, assume the user doesn't care about tenths of a degree.
		    
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			String unitType = sharedPrefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
			
			if(unitType.equals(getString(R.string.pref_units_imperial))){
				high = (high * 1.8) + 32;
				low = (low * 1.8) + 32;
			}
			else if(!unitType.equals(getString(R.string.pref_units_metric))){
				Log.d(LOG_TAG, "unit type not found:"+unitType);
			}
			
			
			
			long roundedHigh = Math.round(high);
		    long roundedLow = Math.round(low);
		 
		    String highLowStr = roundedHigh + "/" + roundedLow;
		    return highLowStr;
		}
		 
		/**
		 * Take the String representing the complete forecast in JSON Format and
		 * pull out the data we need to construct the Strings needed for the wireframes.
		 *
		 * Fortunately parsing is easy:  constructor takes the JSON string and converts it
		 * into an Object hierarchy for us.
		 * @throws JSONException 
		 */
		private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException
		        {
		 
		    // These are the names of the JSON objects that need to be extracted.
		    final String OWM_LIST = "list";
		    final String OWM_WEATHER = "weather";
		    final String OWM_TEMPERATURE = "temp";
		    final String OWM_MAX = "max";
		    final String OWM_MIN = "min";
		    final String OWM_DATETIME = "dt";
		    final String OWM_DESCRIPTION = "main";
		 
		    JSONObject forecastJson = new JSONObject(forecastJsonStr);
		    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
		 
		    String[] resultStrs = new String[numDays];
		    for(int i = 0; i < weatherArray.length(); i++) {
		        // For now, using the format "Day, description, hi/low"
		        String day;
		        String description;
		        String highAndLow;
		 
		        // Get the JSON object representing the day
		        JSONObject dayForecast = weatherArray.getJSONObject(i);
		 
		        // The date/time is returned as a long.  We need to convert that
		        // into something human-readable, since most people won't read "1400356800" as
		        // "this saturday".
		        long dateTime = dayForecast.getLong(OWM_DATETIME);
		        day = getReadableDateString(dateTime);
		 
		        // description is in a child array called "weather", which is 1 element long.
		        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
		        description = weatherObject.getString(OWM_DESCRIPTION);
		 
		        // Temperatures are in a child object called "temp".  Try not to name variables
		        // "temp" when working with temperature.  It confuses everybody.
		        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
		        double high = temperatureObject.getDouble(OWM_MAX);
		        double low = temperatureObject.getDouble(OWM_MIN);
		 
		        highAndLow = formatHighLows(high, low);
		        resultStrs[i] = day + " - " + description + " - " + highAndLow;
		    }
		 
		    return resultStrs;
		}
		@Override
		protected String[] doInBackground(String... params) {
			
			// These two need to be declared outside the try/catch
			// so that they can be closed in the finally block.
			HttpURLConnection urlConnection = null;
			BufferedReader reader = null;
			String format = "json";
			String units = "metric";
			int numDays = 7;
			
			 
			// Will contain the raw JSON response as a string.
			String forecastJsonStr = null;
			try {
			    // Construct the URL for the OpenWeatherMap query
			    // Possible parameters are avaiable at OWM's forecast API page, at
			    // http://openweathermap.org/API#forecast
			    //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
			    final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
			    final String QUERY_PARAM = "q";
			    final String MODE_PARAM= "mode";
			    final String DAYS_PARAM="cnt";
			    final String UNITS_PARAM = "units";
			    
			    Uri builtUrl = Uri.parse(BASE_URL).buildUpon()
			    					.appendQueryParameter(QUERY_PARAM, params[0])
			    					.appendQueryParameter(MODE_PARAM, format)
			    					.appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
			    					.appendQueryParameter(UNITS_PARAM,units)
			    					.build();
			    					;
			    URL url = new URL(builtUrl.toString());
			    Log.v(LOG_TAG, builtUrl.toString());
			    					
			    // Create the request to OpenWeatherMap, and open the connection
			    urlConnection = (HttpURLConnection) url.openConnection();
			    urlConnection.setRequestMethod("GET");
			    urlConnection.connect();
			  
			 
			    // Read the input stream into a String
			    InputStream inputStream = urlConnection.getInputStream();
			    StringBuffer buffer = new StringBuffer();
			    if (inputStream == null) {
			        // Nothing to do.
			        forecastJsonStr = null;
			    }
			    reader = new BufferedReader(new InputStreamReader(inputStream));
			 
			    String line;
			    while ((line = reader.readLine()) != null) {
			        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
			        // But it does make debugging a *lot* easier if you print out the completed
			        // buffer for debugging.
			        buffer.append(line + "\n");
			    }
			 
			    if (buffer.length() == 0) {
			        // Stream was empty.  No point in parsing.
			        forecastJsonStr = null;
			    }
			    forecastJsonStr = buffer.toString();
			    
			} catch (IOException e) {
			    Log.e(LOG_TAG, "Error ", e);
			    // If the code didn't successfully get the weather data, there's no point in attemping
			    // to parse it.
			    forecastJsonStr = null;
			} finally{
			    if (urlConnection != null) {
			        urlConnection.disconnect();
			    }
			    if (reader != null) {
			        try {
			            reader.close();
			        } catch (final IOException e) {
			            Log.e(LOG_TAG, "Error closing stream", e);
			        }
			    }
			}
			
		
			try {
				return getWeatherDataFromJson(forecastJsonStr, numDays);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			ad.clear();
			if(result != null){
				for(String str: result){
					ad.add(str);
				}
				
			}
			super.onPostExecute(result);
		}
	
	}
}