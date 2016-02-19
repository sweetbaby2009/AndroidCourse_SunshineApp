package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

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
                Log.v(LOG_TAG, "Detail Fragment is created as in main activity.");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_details, new DetailsActivityFragment())
                        .commit();
            }
        } else{
            mTwoPane = false;
        }


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        String postcode = Utility.getPreferredLocation(this);
        if (postcode!=null && !postcode.equals(mPostcode)){
            WeatherFragment fragment = (WeatherFragment)getSupportFragmentManager().
                    findFragmentById(R.id.fragment_weather);
            if (null != fragment){
                fragment.onLocationChanged();
            }
            mPostcode = postcode;
        }

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


}
