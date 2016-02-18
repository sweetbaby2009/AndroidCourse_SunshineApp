package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
//import android.widget.Toast;


import nintao.com.android.study.sunshine.data.WeatherContract;


public class WeatherFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter mWeatherListAdapter;
    private final String LOG_TAG = WeatherFragment.class.getSimpleName();
    private static final int MY_LOADER_ID = 0;
    //final String  INPUT_COUNTRY = ",us";
    private String mPostcode = null;
    private String mUnit = null;
    private  boolean isInflated = false;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };

    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_LOCATION_COORD_LAT = 7;
    public static final int COL_LOCATION_COORD_LONG = 8;
    public static final int COL_LOCATION_CITY_NAME = 9;

    private TextView mCoordLat;
    private TextView mCoordLong;
    private TextView mCityNameView;

    public WeatherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart(){
        super.onStart();
        //after we have the db in place, we delete the getWeather from onStart to stop the app
        //fetching the weather every time it starts the fragment.
        //now it only aquire new weather when "refresh" is called.
        //getWeather();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //replaced by a CursorLoader
//      Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);


        //first display the fake testing weather data

//        String[] weatherArray = {
//                "Sunday - Sunny - 12/20",
//                "Monday - Sunny - 12/20",
//                "Tuesday - Sunny - 12/20",
//                "Wednesday - Sunny - 12/20",
//                "Thursday - Sunny - 12/20",
//                "Friday - Sunny - 12/20",
//                "Saturday - Sunny - 12/20",
//        };
//        final List<String> weatherList = new ArrayList<>(Arrays.asList(weatherArray));
//        mWeatherListAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_text, weatherList);


        mWeatherListAdapter = new ForecastAdapter(getActivity(), null, 0);

        //inflate the rootView for this Fragment. This rootView item will be used to find all the views under it
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //inflate the non-list views
        mCoordLat = (TextView) rootView.findViewById(R.id.main_coordlat_textview);
        mCoordLong = (TextView) rootView.findViewById(R.id.main_coordlong_textview);
        mCityNameView = (TextView) rootView.findViewById(R.id.main_city_name_textview);

        //find the list view and set the adapter to the list view for data inflate
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mWeatherListAdapter);

        //add one onItemClickListener to weatherListView.
//        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String text = mWeatherListAdapter.getItem(position);
//                // This was used for testing
//                // Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
//                Intent openDetails = new Intent(getActivity(), DetailsActivity.class);
//                openDetails.putExtra(Intent.EXTRA_TEXT, text);
//                startActivity(openDetails);
//            }
//        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailsActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(ForecastAdapter.COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_weather, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            getWeather();
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_viewOnMap) {
            showMap(Uri.parse("geo:0,0?").buildUpon()
                    .appendQueryParameter("q",mPostcode)
                    .build());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "Failed to call "+ mPostcode + "not found.");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void getWeather() {

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(),mWeatherListAdapter);
        String[] realWeatherData = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPostcode = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_value));
        mUnit = prefs.getString(getString(R.string.pref_unit_key), "no selection");

        if (mPostcode != null){
            //realWeatherData = weatherTask.execute(mPostcode + INPUT_COUNTRY).get();
            weatherTask.execute(mPostcode,mUnit);
        }
//        if (realWeatherData != null) {
////            mWeatherListAdapter.clear();
//            mWeatherListAdapter.addAll(realWeatherData);
//        }
    }

    protected void onLocationChanged(){
        getWeather();
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                //null,   //projection
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (!cursor.moveToFirst() || cursor == null) {
            return;
        }

        // Read date from cursor and update views for day of week and date
        String coordLat =Long.toString(cursor.getLong(COL_LOCATION_COORD_LAT));
        String coordLong =Long.toString(cursor.getLong(COL_LOCATION_COORD_LONG));

        mCoordLat.setText(coordLat);
        mCoordLong.setText(coordLong);

        //City name
        String description = cursor.getString(COL_LOCATION_CITY_NAME);
        mCityNameView.setText(description);

        //new content to list
        mWeatherListAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mWeatherListAdapter.swapCursor(null);
    }
}
