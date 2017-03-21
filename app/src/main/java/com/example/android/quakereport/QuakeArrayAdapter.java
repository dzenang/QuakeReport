package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by dzenang on 27.2.2017.
 *
 * Array adapter to populate list of Earthquake objects
 */

public class QuakeArrayAdapter extends ArrayAdapter {

    private enum LocationPart {FIRST, SECOND }

    // Constructors
    public QuakeArrayAdapter(Context context, ArrayList<Earthquake> earthquakes){
        super(context, 0, earthquakes);
    }

    /**
     *
     * @param position Position in the list of data to show in ListView
     * @param convertView Recycled view to populate
     * @param parent ListView to inflate
     * @return The view for the position in ListView
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Earthquake currentEarthquake = (Earthquake) getItem(position);

        // Populating list item view with data from item on given position from data list
        TextView magnitudeTextView = (TextView) listItemView.findViewById(R.id.magnitude_tv);
        double magnitude = currentEarthquake.getMagnitude();
        magnitudeTextView.setText(formatMagnitude(magnitude));

        // Setting correct color for magnitude circle
        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeTextView.getBackground();
        int magnitudeColor = getMagnitudeColor(magnitude);
        magnitudeCircle.setColor(magnitudeColor);

        // Show location offset and primary location
        String location = currentEarthquake.getLocation();
        TextView offsetLocationTextView = (TextView) listItemView.findViewById(R.id.offset_tv);
        TextView primaryLocationTextView = (TextView) listItemView.findViewById(R.id.primary_location_tv);

        if (isOnlyPrimary(location)){
            offsetLocationTextView.setText("Near the");
            primaryLocationTextView.setText(location);
        } else {
            offsetLocationTextView.setText(splitLocation(location, LocationPart.FIRST));
            primaryLocationTextView.setText(splitLocation(location, LocationPart.SECOND));
        }

        // Show date and time
        Date dateObject = new Date(currentEarthquake.getMilliseconds());
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date_tv);
        dateTextView.setText(formatDate(dateObject));

        TextView timeTextView = (TextView) listItemView.findViewById(R.id.time_tv);
        timeTextView.setText(formatTime(dateObject));

        return listItemView;
    }

    /**
     * Returns formated date string (i.e. "Jan 05, 2007") from a date object
     */
    private String formatDate (Date date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, y");
        return dateFormatter.format(date);
    }

    /**
     * Returns formated time string (i.e. "4:17 AM") from a date objects
     */
    private String formatTime (Date date){
        SimpleDateFormat dateFormatter = new SimpleDateFormat("H:mm a");
        return dateFormatter.format(date);
    }

    /**
     * Returns location offset or primary location from location string
     */
    private String splitLocation (String location, LocationPart part) {
        int indexOfSplit = location.indexOf("of");

        // If location contains "of" return first or second part of it
        if (indexOfSplit != -1) {
            indexOfSplit += 2;
            if (part == LocationPart.FIRST) {
                return location.substring(0, indexOfSplit);
            } else if (part == LocationPart.SECOND){
                return location.substring(indexOfSplit + 1, location.length());
            }
        }
        return "";
    }

    /**
     *  Returns true if location has only primary location
     */
    private boolean isOnlyPrimary (String location) {
        // If splitLocation returns empty string then it is only primary
        if ( splitLocation(location, LocationPart.FIRST).equals("")){
            return true;
        }
        return false;
    }

    /**
     *  Returns formated magnitude (i.e. "6.2") from a double value
     */
    private String formatMagnitude (double magnitude) {
        DecimalFormat formatter = new DecimalFormat("0.0");
        return formatter.format(magnitude);
    }

    /**
     *  Returns color value for magnitude circle based on magnitude value
     */
    private int getMagnitudeColor(double magnitude){

        int magnitudeColorResourceId;
        switch ((int) Math.floor(magnitude)) {
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;
        }
        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
    }
}











