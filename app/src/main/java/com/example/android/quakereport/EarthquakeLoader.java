package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Created by dzenang on 8.3.2017.
 */

public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    // Members
    private String mUrl;
    private static final String LOG_TAG = EarthquakeLoader.class.getSimpleName();

    // Constructors
    public EarthquakeLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        Log.i(LOG_TAG, "onStartLoading");
        forceLoad();
    }

    @Override
    public List<Earthquake> loadInBackground() {

        Log.i(LOG_TAG, "loadInBackground");
        // If url is null return early
        if (TextUtils.isEmpty(mUrl)) {
            return null;
        }

        // Perform network request, parse data, return list of Earthquake objects
        List<Earthquake> earthquakeList =  QueryUtils.fetchEarthquakeData(mUrl);
        return earthquakeList;
    }
}
