package com.spybug.gtnav;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.geocoder.MapboxGeocoder;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.geocoder.service.models.GeocoderResponse;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit.Response;

import static com.spybug.gtnav.HelperUtil.haveNetworkConnection;

//import retrofit.Response;

/**
 * Background task to communicate with the map server
 */

public class DirectionsServerRequest extends AsyncTask<Object, Void, Object> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;

    DirectionsServerRequest(Context context) {
        contextRef = new WeakReference<>(context);
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        LatLng[] points = new LatLng[0];

        if (!hasNetwork) {
            errorCode = 1;
            return points;
        }

        View view = (View) objects[0];
        String mapboxApiKey = (String) objects[1];
        EditText startText = view.findViewById(R.id.start_location);
        EditText endText = view.findViewById(R.id.end_location);

        String start = startText.getText().toString();
        String end = endText.getText().toString();
        String inputLine;
        String stringUrl;
        String result;
        String routeGeometry;

        MapboxGeocoder geo_client1 = new MapboxGeocoder.Builder()
                .setAccessToken(mapboxApiKey)
                .setLocation(start)
                .setProximity( -84.3963,33.7756)
                .build();

        MapboxGeocoder geo_client2 = new MapboxGeocoder.Builder()
                .setAccessToken(mapboxApiKey)
                .setLocation(end)
                .setProximity(-84.3963, 33.7756)
                .build();

        try {
            Response<GeocoderResponse> geo_response_start = geo_client1.execute();
            Response<GeocoderResponse> geo_response_end = geo_client2.execute();

            List<GeocoderFeature> start_results = geo_response_start.body().getFeatures();
            List<GeocoderFeature> end_results = geo_response_end.body().getFeatures();
            GeocoderFeature start_result, end_result;

            if (start_results.size() == 0 || end_results.size() == 0) {
                errorCode = 2;
                return points;
            }
            else {
                start_result = start_results.get(0);
                end_result = end_results.get(0);
            }

            stringUrl = String.format("%sdirections?origin=%s,%s&destination=%s,%s&mode=%s",
                    BuildConfig.API_URL,
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

    protected void onPostExecute(Object result) {
        if (errorCode != 0) {
            if (errorCode == 1) {
                String info = "You are not connected to the internet. Please try again later.";
                Toast.makeText(contextRef.get(), info, Toast.LENGTH_LONG).show();
            } else if (errorCode == 2) {
                String info = "Could not find location, please try again.";
                Toast.makeText(contextRef.get(), info, Toast.LENGTH_LONG).show();
            }
        }
    }
}
