package nintao.com.android.study.sunshine;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import nintao.com.android.study.sunshine.data.WeatherContract;

public class DetailsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (savedInstanceState == null) {
            Log.v("DetailsActivity", "Detail Fragment is being created as in Details activity.");

            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailsActivityFragment.DETAIL_URI, getIntent().getData());

            DetailsActivityFragment detailFragment = new DetailsActivityFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_details, detailFragment)
                    .commit();
            Log.v("DetailsActivity", "Detail Fragment is created done as in Details activity.");
            }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }


}
