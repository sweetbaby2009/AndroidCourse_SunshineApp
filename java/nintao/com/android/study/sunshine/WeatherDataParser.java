package nintao.com.android.study.sunshine;

import android.text.format.Time;
import android.util.Log;
import java.text.SimpleDateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataParser {
    /**
     * Return a certain format of data after extracting JSON information out from the result.
     */
    String mUnits = null;
    JSONObject mWeatherJsonObj = null;
    WeatherDataParser(String units){
        mUnits = units;

    }
    private final String LOG_TAG = WeatherDataParser.class.getSimpleName();

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {

        if (mUnits.equals("imperial")){
            high = (high*1.8) + 32;
            low =  (low*1.8) + 32;
        } else if (!mUnits.equals("metric")){
            Log.e(LOG_TAG, "Unit type not found:" + mUnits);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedLow + "~" + roundedHigh;
        // will output 23/8 format for temperatures
        return highLowStr;
    }

    private void getJsonObject(String weatherJsonStr) throws JSONException{
        //extract the weather info to an Json Array
        mWeatherJsonObj = new JSONObject(weatherJsonStr);
    }

    public String getCityNameFromJson(String weatherJsonStr)
            throws JSONException {
        final String OWM_CITY_NAME = "name";

        if (mWeatherJsonObj == null){
            getJsonObject(weatherJsonStr);
        }

        String cityName = mWeatherJsonObj.getString(OWM_CITY_NAME);
        return cityName;
    }
    public String[] getWeatherDataFromJson(String weatherJsonStr, int numDays)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        if (mWeatherJsonObj == null){
            getJsonObject(weatherJsonStr);
        }

        //get weather list
        JSONArray weatherJsonArray = mWeatherJsonObj.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        //
        Time dayTime = new Time();
        dayTime.setToNow();
        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        //return result of the weather info.
        String[] resultStrs = new String[numDays];

        //extract details from each item of the Jason Array
        for(int dayIndex = 0; dayIndex < weatherJsonArray.length(); dayIndex++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            //get single day info
            JSONObject dayForecast = weatherJsonArray.getJSONObject(dayIndex);

            //get date time info
            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay+dayIndex);
            day = getReadableDateString(dateTime);

            // get weather forecast description
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            //get high/low temp
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double maxTemp = temperatureObject.getDouble(OWM_MAX);
            double minTemp = temperatureObject.getDouble(OWM_MIN);

            //format high/log temp
            highAndLow = formatHighLows(maxTemp, minTemp);


            resultStrs[dayIndex] = day + " / " + description + " / " + highAndLow;
        }

        /*
         for (String s : resultStrs) {
            (LOG_TAG, "Forecast entry: " + s);
         }
          */
         return resultStrs;
    }

}
