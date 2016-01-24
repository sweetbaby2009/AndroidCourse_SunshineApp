package nintao.com.android.study.sunshine;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailsActivityFragment extends Fragment {

    private ShareActionProvider mShareActionProvider;
    private final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private static final String FORECAST_SHARE = " #SunshineApp";
    private String mWeatherStr;


    public DetailsActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set the menu to be available
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        //Inspect the Intent which trig the Details Activity
        Intent openDetails = getActivity().getIntent();
        if (openDetails!=null && openDetails.hasExtra(openDetails.EXTRA_TEXT)){
            mWeatherStr = openDetails.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) rootView.findViewById(R.id.details_content_text)).setText(mWeatherStr);
        }

        return rootView;
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

        if (mShareActionProvider !=null){
            mShareActionProvider.setShareIntent(createShareIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }

    }





}


