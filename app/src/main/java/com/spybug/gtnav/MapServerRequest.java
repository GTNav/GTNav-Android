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
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
        String inputLine;
        String stringUrl;
        String result;
        String routeGeometry;

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

            stringUrl = String.format("https://gtnavtest.azurewebsites.net/directions?origin=%s,%s&destination=%s,%s&mode=%s",
                    start_result.getLongitude(), start_result.getLatitude(),
                    end_result.getLongitude(), end_result.getLatitude(),
                    "walking");

            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection =(HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while((inputLine = reader.readLine()) != null){
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
            }
            catch(IOException e){
                e.printStackTrace();
                result = null;
            }

            List<Position> positionList = null;

            if (result != null) {
                try {
                    JSONObject directionsResultJson = new JSONObject(result);
                    routeGeometry = directionsResultJson.getJSONArray("routes").getJSONObject(0).getString("geometry");
                    positionList =  PolylineUtils.decode(routeGeometry, 5);

                    // Convert Positions List into LatLng[]
                    points = new LatLng[positionList.size()];
                    for (int i = 0; i < positionList.size(); i++) {
                        points[i] = new LatLng(
                                positionList.get(i).getLatitude(),
                                positionList.get(i).getLongitude());
                    }
                }
                catch(JSONException ex) {
                    ex.printStackTrace();
                }


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
