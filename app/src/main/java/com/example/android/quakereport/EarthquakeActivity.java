/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    private static final String USGS_URL =
            "http://earthquake.usgs.gov/fdsnws/event/1/query"; //?format=geojson&eventtype=earthquake&orderby=time&minmag=1.9&limit=10";
    private QuakeArrayAdapter mAdapter;
    private static final int EARTHQUAKE_LOADER_ID = 0;
    private TextView mEmptyStateTextView;
    private ProgressBar mLoadingSpinner;
    private NetworkInfo mActiveNetwork;
    private boolean mFirstLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the list ListView in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new QuakeArrayAdapter
        mAdapter = new QuakeArrayAdapter(this, new ArrayList<Earthquake>());

        // Set empty view on list view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty);
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // Set the adapter on the list view so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        // Setting on item click listener
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Earthquake currentEarthquake = (Earthquake) mAdapter.getItem(position);
                Uri webpage = Uri.parse(currentEarthquake.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        mFirstLoad = true;
    }

    @Override
    protected void onPostResume() {
        Log.i(LOG_TAG, "onPostResume");
        super.onPostResume();

        mLoadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);
        // Depending on having internet connection initialize loader or show "No internet connection"
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mActiveNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = mActiveNetwork != null && mActiveNetwork.isConnected();
        //&& (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE);

        if (isConnected && mFirstLoad) {
            // Prepare the loader.  Either re-connect with an existing one, or start a new one.
            Bundle args = new Bundle();
            args.putString("url", USGS_URL);
            Log.i(LOG_TAG, "onPostResume: Initializing loader with ID " + EARTHQUAKE_LOADER_ID);
            getLoaderManager().initLoader(EARTHQUAKE_LOADER_ID, args, this);

            mFirstLoad = false;
            mLoadingSpinner.setVisibility(View.VISIBLE);
            mAdapter.clear();
            mEmptyStateTextView.setVisibility(View.GONE);
        }
        else if (isConnected && !mFirstLoad){
            mLoadingSpinner.setVisibility(View.VISIBLE);
            mAdapter.clear();
            mEmptyStateTextView.setVisibility(View.GONE);
        }
        else if (!isConnected) {
            Log.i(LOG_TAG, "onPostResume: No internet connection");
            // Hide progress bar since data is loaded
            mLoadingSpinner.setVisibility(ProgressBar.GONE);
            mAdapter.clear();
            // Set empty state text to display "No internet connection."
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        Log.i(LOG_TAG, "onCreateLoader");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        // Read minimal magnitude from preferences
        String minMagnitude = sharedPref.getString(getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPref.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        Uri baseUri = Uri.parse(args.getString("url"));
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("orderby", orderBy);
        //?format=geojson&eventtype=earthquake&orderby=time&minmag=1.9&limit=10";


        return new EarthquakeLoader(EarthquakeActivity.this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> data) {
        Log.i(LOG_TAG, "onLoadFinished");

        // Hide progress bar since data is loaded
        mLoadingSpinner.setVisibility(ProgressBar.GONE);

        if (mActiveNetwork != null && mActiveNetwork.isConnected()) {
            // Set empty state text to display "No earthquakes found."
            mEmptyStateTextView.setText(R.string.no_earthquakes);
        }

        // Clear adapter of previous data
        mAdapter.clear();

        // If valid data in earthquakes, add them to adapter's data set
        // This will trigger list view to update
        if (data != null && !data.isEmpty()) {
            mAdapter.addAll(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.i(LOG_TAG, "onLoadReset");
        // Loader reset, remove data from ui by clearing adapters data set
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
