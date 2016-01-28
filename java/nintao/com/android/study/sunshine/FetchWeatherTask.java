package nintao.com.android.study.sunshine;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    //defined the log tag name to be this class name so that it won't change until redefine
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    private final Context mContext;

    public FetchWeatherTask(Context context, ArrayAdapter<String> forecastAdapter) {
        mContext = context;
        mForecastAdapter = forecastAdapter;
    }

    private boolean DEBUG = true;


    @Override
    protected String[] doInBackground(String... params) {

        //if no postCode provided, no action
        if (params.length == 0){
            return null;
        }

        String format = "json";
        String location = params[0];
        //String units = params[1];
        String unitSelected = params[1];
        String units = "metric";
        int numberOfDays = 14;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are available at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String DAYS_PARAM = "cnt";
            final String UNITS_PARAM = "units";
            final String FORMAT_PARAM = "modee";
            final String POSTCODE_PARAM = "q";
            final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String APPID_PARAM = "APPID";
            final String APP_ID = "2bd900111f79c3314191afe4cc83dda0";

            Uri buildUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(POSTCODE_PARAM,location)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numberOfDays))
                    .appendQueryParameter(APPID_PARAM, APP_ID)
                    .build();

            String myUri = buildUri.toString();
            Log.v(LOG_TAG, myUri);
            URL url = new URL(myUri);
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
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
                return null;
            }
            forecastJsonStr = buffer.toString();

            //Log.v(LOG_TAG, "Got Weather JSON String: " + forecastJsonStr);

            WeatherDataParser dataParser = new WeatherDataParser(unitSelected);
            String[] weatherDate = dataParser.getWeatherDataFromJson(forecastJsonStr,location);
            //String cityName = dataParser.getCityNameFromJson(forecastJsonStr);
            return weatherDate;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } catch (JSONException e) {
        Log.e(LOG_TAG, e.getMessage(), e);
        e.printStackTrace();
        } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG,"Error closing stream", e);
                    }
                }
            }
    return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null && mForecastAdapter != null) {
            mForecastAdapter.clear();
            for(String dayForecastStr : result) {
                mForecastAdapter.add(dayForecastStr);
            }
            // New data is back from the server.  Hooray!
        }
    }

}
