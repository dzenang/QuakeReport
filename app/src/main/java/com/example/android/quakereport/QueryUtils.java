package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
public final class QueryUtils {

    /** Tag for log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query USGS site and return list of {@link Earthquake} objects for given url string
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {

        Log.i(LOG_TAG, "fetchEarthquakeData");

        // TODO: 10.3.2017 delete this sleep when testing done
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        URL urlObject = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(urlObject);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream.", e);
        }

        List<Earthquake> earthquakes = extractEarthquakes(jsonResponse);
        return earthquakes;
    }

    /**
     * Creates url object from url string
     */
    private static URL createUrl(String urlString) {
        URL url = null;

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating url:", e);
        }
        return url;
    }

    /**
     * Creates http request and returns {@link String} response
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If url is null return early
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromInputStream(inputStream);

            } else if (responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                // Handle redirection
                Log.d(LOG_TAG, "Server returned redirection error: " + responseCode);
                URL newUrl = createUrl(urlConnection.getHeaderField("Location"));
                jsonResponse = makeHttpRequest(newUrl);
            } else {
                Log.e(LOG_TAG, "Server returned error: " + responseCode);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error during http request.", e);
        } finally {
            // Close connection and input stream
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Converts {@link InputStream} into String which contains JSON response from server
     */
    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader streamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(streamReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Earthquake> extractEarthquakes(String json) {

        // If json string is empty or null return early
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        // Create an empty ArrayList that we can start adding earthquakes to
        List<Earthquake> earthquakes = new ArrayList<>();

        // Try to parse the json string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject root = new JSONObject(json);
            JSONArray features = root.getJSONArray("features");

            for(int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject properties = feature.getJSONObject("properties");
                double magnitude = properties.getDouble("mag");
                String location = properties.getString("place");
                long date = properties.getLong("time");
                String url = properties.getString("url");
                earthquakes.add(new Earthquake(magnitude, location, date, url));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

}