package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
//import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import nintao.com.android.study.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class WeatherFragment extends Fragment {

    private ArrayAdapter<String> mWeatherListAdapter;
    private final String LOG_TAG = WeatherFragment.class.getSimpleName();
    final String  INPUT_COUNTRY = ",us";
    private String mPostcode = null;
    private String mUnit = null;


    public WeatherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the menu to be available
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart(){
        super.onStart();
        getWeather();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //inflate the rootView for this Fragment. This rootView item will be used to find all the views under it
        this.setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //first display the fake testing weather data

        String[] weatherArray = {
                "Sunday - Sunny - 12/20",
                "Monday - Sunny - 12/20",
                "Tuesday - Sunny - 12/20",
                "Wednesday - Sunny - 12/20",
                "Thursday - Sunny - 12/20",
                "Friday - Sunny - 12/20",
                "Saturday - Sunny - 12/20",
        };
        final List<String> weatherList = new ArrayList<>(Arrays.asList(weatherArray));
        mWeatherListAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_text, weatherList);

        //find the list view and set the adapter to the list view for data inflate
        ListView weatherListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        weatherListView.setAdapter(mWeatherListAdapter);

        //add one onItemClickListener to weatherListView.
        weatherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = mWeatherListAdapter.getItem(position);
                // This was used for testing
                // Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                Intent openDetails = new Intent(getActivity(), DetailsActivity.class);
                openDetails.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(openDetails);
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

    private void getWeather() {

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(),mWeatherListAdapter);
        String[] realWeatherData = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPostcode = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_value));
        mUnit = prefs.getString(getString(R.string.pref_unit_key), "no selection");

        if (mPostcode != null){
            try {
                //realWeatherData = weatherTask.execute(mPostcode + INPUT_COUNTRY).get();
                realWeatherData = weatherTask.execute(mPostcode + INPUT_COUNTRY,mUnit).get();
            } catch (ExecutionException e) {
                Log.e(LOG_TAG, "ExecutionException", e);
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "InterruptedException", e);
                e.printStackTrace();
            }
        }
        if (realWeatherData != null) {
            mWeatherListAdapter.clear();
            mWeatherListAdapter.addAll(realWeatherData);
        }
    }
}
