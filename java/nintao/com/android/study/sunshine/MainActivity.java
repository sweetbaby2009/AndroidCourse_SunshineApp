package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements WeatherFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String mPostcode = null;
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //when false: set the default value only when the application is never called in the past
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, true);

        mPostcode = Utility.getPreferredLocation(this);

        // if it's a two pane mode, then add the detail fragment when creating the main activity.
        if (findViewById(R.id.fragment_details) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                Log.v(LOG_TAG, "Detail Fragment is being created as in main activity.");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_details, new DetailsActivityFragment())
                        .commit();
                Log.v(LOG_TAG, "Detail Fragment is created as in main activity.");
            }
        } else{
            mTwoPane = false;
            //remove the shadow for the one pane scenario
            getSupportActionBar().setElevation(0f);
        }
        WeatherFragment weatherFragment = (WeatherFragment) getSupportFragmentManager().
                findFragmentById(R.id.fragment_weather);
        weatherFragment.setUseTodayView(!mTwoPane);

    }

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String postcode = Utility.getPreferredLocation(this);
        if (postcode!=null && !postcode.equals(mPostcode)){
            WeatherFragment ff = (WeatherFragment)getSupportFragmentManager().
                    findFragmentById(R.id.fragment_weather);
            if (null != ff){
                ff.onLocationChanged();
            }
            DetailsActivityFragment df = (DetailsActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(postcode);
            }
            mPostcode = postcode;
        }

    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailsActivityFragment.DETAIL_URI, contentUri);

            DetailsActivityFragment detailFragment = new DetailsActivityFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_details, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailsActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }

}
