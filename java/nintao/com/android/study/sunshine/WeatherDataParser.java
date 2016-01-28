package nintao.com.android.study.sunshine;
import nintao.com.android.study.sunshine.data.WeatherContract;
import nintao.com.android.study.sunshine.data.WeatherContract.WeatherEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataParser {
    /**
     * Return a certain format of data after extracting JSON information out from the result.
     */
    String mUnits = null;
    JSONObject mWeatherJsonObj = null;
    Context mContext;

    WeatherDataParser(Context context, String units){
        mUnits = units;
        mContext = context;

    }
    private final String LOG_TAG = WeatherDataParser.class.getSimpleName();

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
//    private String getReadableDateString(long time){
//        // Because the API returns a unix timestamp (measured in seconds),
//        // it must be converted to milliseconds in order to be converted to valid date.
//        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
//        return shortenedDateFormat.format(time);
//    }

    /**
     * Prepare the weather high/lows for presentation.
     */
//    private String formatHighLows(double high, double low) {
//
//        if (mUnits.equals("imperial")){
//            high = (high*1.8) + 32;
//            low =  (low*1.8) + 32;
//        } else if (!mUnits.equals("metric")){
//            Log.e(LOG_TAG, "Unit type not found:" + mUnits);
//        }
//
//        long roundedHigh = Math.round(high);
//        long roundedLow = Math.round(low);
//
//        String highLowStr = roundedLow + "~" + roundedHigh;
//        // will output 23/8 format for temperatures
//        return highLowStr;
//    }

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
    public String[] getWeatherDataFromJson(String weatherJsonStr, String locationSetting)
            throws JSONException {
        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";

        //under city tag
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";

        //Inside location coordinate tag
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All under tha temperature tag
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        // all under the weather tag
        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        if (mWeatherJsonObj == null){
            getJsonObject(weatherJsonStr);
        }

        try{

            //get weather list block
            JSONArray weatherJsonArray = mWeatherJsonObj.getJSONArray(OWM_LIST);

            //get the weather city block
            JSONObject cityJson = mWeatherJsonObj.getJSONObject(OWM_CITY);
            //Extract City name
            String cityName = cityJson.getString(OWM_CITY_NAME);

            //Extract Coordinate information
            JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
            double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

            long locationId = addLocation(locationSetting, cityName, cityLatitude, cityLongitude);

            // create the Vector to insert data into DB
            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherJsonArray.length());

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

            //extract details from each item of the Jason Array
            for(int dayIndex = 0; dayIndex < weatherJsonArray.length(); dayIndex++) {
                // For now, using the format "Day, description, hi/low"
                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;
                double high;
                double low;

                String description;
                int weatherId;

                //get single day info
                JSONObject dayForecast = weatherJsonArray.getJSONObject(dayIndex);

                //get date time info
                dateTime = dayTime.setJulianDay(julianStartDay + dayIndex);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                // get the weather city block content
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(dayIndex);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);


                //get the temperature block content
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double maxTemp = temperatureObject.getDouble(OWM_MAX);
                double minTemp = temperatureObject.getDouble(OWM_MIN);

                //create content values to fill into the DB
                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
                weatherValues.put(WeatherEntry.COLUMN_DATE, dateTime);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, maxTemp);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, minTemp);
                weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);

            }

            // if cVVector is not empty, add content to database
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                mContext.getContentResolver().bulkInsert(
                        WeatherContract.WeatherEntry.CONTENT_URI, cvArray);

            }

            // Sort order:  Ascending, by date.
            String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
            Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            // Students: Uncomment the next lines to display what what you stored in the bulkInsert

            //commented again in 4C

//                Cursor cur = mContext.getContentResolver().query(weatherForLocationUri,
//                        null, null, null, sortOrder);
//
//                cVVector = new Vector<>(cur.getCount());
//                if ( cur.moveToFirst() ) {
//                    do {
//                        ContentValues cv = new ContentValues();
//                        DatabaseUtils.cursorRowToContentValues(cur, cv);
//                        cVVector.add(cv);
//                    } while (cur.moveToNext());
//                }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + cVVector.size() + " Inserted");

            String[] resultStrs = convertContentValuesToUXFormat(cVVector);
            return resultStrs;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
//        for ( int i = 0; i < cvv.size(); i++ ) {
//            ContentValues weatherValues = cvv.elementAt(i);
//            String highAndLow = formatHighLows(
//                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MAX_TEMP),
//                    weatherValues.getAsDouble(WeatherEntry.COLUMN_MIN_TEMP));
//            resultStrs[i] = getReadableDateString(
//                    weatherValues.getAsLong(WeatherEntry.COLUMN_DATE)) +
//                    " - " + weatherValues.getAsString(WeatherEntry.COLUMN_SHORT_DESC) +
//                    " - " + highAndLow;
//        }
        return resultStrs;
    }

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat the latitude of the city
     * @param lon the longitude of the city
     * @return the row ID of the added location.
     */
    long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        return -1;
    }
        /*
        Students: This code will allow the FetchWeatherTask to continue to return the strings that
        the UX expects so that we can continue to test the application even once we begin using
        the database.
     */
}
