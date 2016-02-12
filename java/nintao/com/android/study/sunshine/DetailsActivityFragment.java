package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nintao.com.android.study.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ShareActionProvider mShareActionProvider;
    private final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private static final String FORECAST_SHARE = " #SunshineApp";
    private String mWeatherStr;
    private static final int DETAIL_LOADER = 0;


    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };


    public DetailsActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the menu to be available
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
//
//        //Inspect the Intent which trig the Details Activity
//        Intent openDetails = getActivity().getIntent();
//        if (openDetails!=null){
//            mWeatherStr = openDetails.getDataString();
//            ((TextView) rootView.findViewById(R.id.details_content_text)).setText(mWeatherStr);
//        }
//
//        return rootView;
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mWeatherStr+FORECAST_SHARE);
        return shareIntent;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu resource file.
        inflater.inflate(R.menu.fragment_details, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
         mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        //only allow sharing data after the Loader finished
        if (mWeatherStr !=null){
            mShareActionProvider.setShareIntent(createShareIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG,"onCreateLoader");

        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                //null,   //projection
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.v(LOG_TAG, "onLoadFinished");
        if (!cursor.moveToFirst()) {
            return;
        }

        String dataString = Utility.formatDate(cursor.getLong(ForecastAdapter.COL_WEATHER_DATE));
        String weatherDescription = cursor.getString(ForecastAdapter.COL_WEATHER_DESC);

        boolean unit = Utility.isMetric(getActivity());

        String highTemp = Utility.formatTemperature(getActivity(),
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MAX_TEMP), unit);
        String lowTemp = Utility.formatTemperature(getActivity(),
                cursor.getDouble(ForecastAdapter.COL_WEATHER_MIN_TEMP), unit);

        mWeatherStr = String.format("%s - %s - %s/%s",
                dataString, weatherDescription, highTemp, lowTemp);

        TextView detailsText = (TextView) getView().findViewById(R.id.details_content_text);
        detailsText.setText(mWeatherStr);

        if (mShareActionProvider !=null){
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){}


}


