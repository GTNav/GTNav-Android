package com.spybug.gtnav;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.geocoder.MapboxGeocoder;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.geocoder.service.models.GeocoderResponse;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.IOException;
import java.util.List;

import retrofit.Response;

/**
 * Background task to communicate with the map server
 */

public class MapServerRequest extends AsyncTask {

    private static final String serverpath = "http://localhost:8080";
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;

    protected void onPreExecute() {
        //TODO: cancel task if no internet
    }

    @Override
    protected Object doInBackground(Object... objects) {
        View view = (View) objects[0];
        String mapboxApiKey = (String) objects[1];
        EditText startText = view.findViewById(R.id.start_location);
        EditText endText = view.findViewById(R.id.end_location);

        LatLng[] points = new LatLng[0];
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

            Waypoint origin = new Waypoint(start_result.getLongitude(), start_result.getLatitude());
            Waypoint destination = new Waypoint(end_result.getLongitude(), end_result.getLatitude());

            MapboxDirections client = new MapboxDirections.Builder()
                    .setAccessToken(mapboxApiKey)
                    .setOrigin(origin)
                    .setDestination(destination)
                    .setProfile(DirectionsCriteria.PROFILE_WALKING)
                    .build();

            Response<DirectionsResponse> response = client.execute();
            Log.v("Directions Result", "Getting directions successful!");

            DirectionsRoute route = response.body().getRoutes().get(0);

            // Convert Waypoints List into LatLng[]
            List<Waypoint> waypoints = route.getGeometry().getWaypoints();
            points = new LatLng[waypoints.size()];
            for (int i = 0; i < waypoints.size(); i++) {
                points[i] = new LatLng(
                        waypoints.get(i).getLatitude(),
                        waypoints.get(i).getLongitude());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return points;
    }

    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
    }

}
