package com.spybug.gtnav;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geocoder.MapboxGeocoder;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.geocoder.service.models.GeocoderResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit.Response;

/**
 * Background task to communicate with the map server
 */

public class MapServerRequest extends AsyncTask {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        View view = (View) objects[0];
        String mapboxApiKey = (String) objects[1];
        EditText startText = view.findViewById(R.id.start_location);
        EditText endText = view.findViewById(R.id.end_location);

        String start = startText.getText().toString();
        String end = endText.getText().toString();


        MapboxGeocoder geo_client1 = new MapboxGeocoder.Builder()
                .setAccessToken(mapboxApiKey)
                .setLocation(start)
                .build();

        MapboxGeocoder geo_client2 = new MapboxGeocoder.Builder()
                .setAccessToken(mapboxApiKey)
                .setLocation(end)
                .build();

        try {
            Response<GeocoderResponse> geo_response_start = geo_client1.execute();
            Response<GeocoderResponse> geo_response_end = geo_client2.execute();

            GeocoderFeature start_result = geo_response_start.body().getFeatures().get(0);
            GeocoderFeature end_result = geo_response_end.body().getFeatures().get(0);

            startText.setText(start_result.getPlaceName());
            endText.setText(end_result.getPlaceName());

            Waypoint origin = new Waypoint(start_result.getLongitude(), start_result.getLatitude());
            Waypoint destination = new Waypoint(end_result.getLongitude(), end_result.getLatitude());
            
            MapboxDirections client = new MapboxDirections.Builder()
                    .setAccessToken(mapboxApiKey)
                    .setOrigin(origin)
                    .setDestination(destination)
                    .setProfile(DirectionsCriteria.PROFILE_WALKING)
                    .build();

            Response response = client.execute();
            Log.v("Directions Result", "Getting directions successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }



        //TODO
        return view;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object o) {
        //TODO
    }

}
