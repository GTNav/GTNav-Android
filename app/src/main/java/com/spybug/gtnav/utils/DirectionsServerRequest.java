package com.spybug.gtnav.utils;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.mapbox.geocoder.MapboxGeocoder;
import com.mapbox.geocoder.service.models.GeocoderFeature;
import com.mapbox.geocoder.service.models.GeocoderResponse;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;
import com.spybug.gtnav.BuildConfig;
import com.spybug.gtnav.interfaces.OnEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit.Response;

import static com.spybug.gtnav.utils.HelperUtil.haveNetworkConnection;

/**
 * Background task to communicate with the map server
 */

public class DirectionsServerRequest extends AsyncTask<Object, Void, List<LatLng>> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<LatLng>, String> mCallBack;
    private final LatLng CENTER_CAMPUS = new LatLng(33.776714, -84.399065);

    private enum UserLocationUsage {START, END, NONE}

    public DirectionsServerRequest(Context context, OnEventListener<List<LatLng>, String> callback) {
        contextRef = new WeakReference<>(context);
        mCallBack = callback;
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected List<LatLng> doInBackground(Object[] objects) {
        List<LatLng> points = new ArrayList<>();

        if (!hasNetwork) {
            errorCode = 1;
            return points;
        }

        String start = ((String) objects[0]).toLowerCase();
        String end = ((String) objects[1]).toLowerCase();
        String mode = ((String) objects[2]).toLowerCase();
        Location user_location = (Location) objects[3];
        String mapboxApiKey = (String) objects[4];
        LatLngBounds bounds = (LatLngBounds) objects[5];


        String inputLine;
        String stringUrl;
        String result;
        String routeGeometry;
        UserLocationUsage userLocationUsage;
        MapboxGeocoder geo_client_start = null;
        MapboxGeocoder geo_client_end = null;

        String curLocationStr = "current location";

        if (start.equals(curLocationStr) && end.equals(curLocationStr)) {
            return points;
        }
        else if (start.equals(curLocationStr)) {
            userLocationUsage = UserLocationUsage.START;
        }
        else if (end.equals(curLocationStr)) {
            userLocationUsage = UserLocationUsage.END;
        }
        else {
            userLocationUsage = UserLocationUsage.NONE;
        }

        if (userLocationUsage != UserLocationUsage.START) {
            geo_client_start = new MapboxGeocoder.Builder()
                    .setAccessToken(mapboxApiKey)
                    .setLocation(start)
                    .build();
        }

        if (userLocationUsage != UserLocationUsage.END) {
            geo_client_end = new MapboxGeocoder.Builder()
                    .setAccessToken(mapboxApiKey)
                    .setLocation(end)
                    .build();
        }

        try {
            Response<GeocoderResponse> geo_response_start = null;
            Response<GeocoderResponse> geo_response_end = null;
            List<GeocoderFeature> start_results, end_results;

            if (geo_client_start != null) {
                geo_response_start = geo_client_start.execute();
            }

            if (geo_client_end != null) {
                geo_response_end = geo_client_end.execute();
            }

            LatLng start_result = null;
            LatLng end_result = null;

            if (geo_client_start != null) {
                start_results = geo_response_start.body().getFeatures();
                if (start_results.size() == 0) {
                    errorCode = 2;
                    return points;
                }

                start_result = getPointClosestCampus(start_results, bounds, user_location);

                if (start_result == null) {
                    errorCode = 2;
                    return points;
                }

            }
            if (geo_client_end != null) {
                end_results = geo_response_end.body().getFeatures();
                if (end_results.size() == 0) {
                    errorCode = 2;
                    return points;
                }
                end_result = getPointClosestCampus(end_results, bounds, user_location);

                if (end_result == null) {
                    errorCode = 2;
                    return points;
                }
            }

            String origin = null;
            String destination = null;

            if (geo_response_start != null) {
                origin = String.format("%s,%s",
                        start_result.getLongitude(), start_result.getLatitude());
            }
            if (geo_response_end != null) {
                destination = String.format("%s,%s",
                        end_result.getLongitude(), end_result.getLatitude());
            }

            if (origin == null) {
                origin = String.format("%s,%s",
                        user_location.getLongitude(), user_location.getLatitude());
            }
            else if (destination == null) {
                destination = String.format("%s,%s",
                        user_location.getLongitude(), user_location.getLatitude());
            }

            stringUrl = String.format("%sdirections?origin=%s&destination=%s&mode=%s",
                    BuildConfig.API_URL,
                    origin,
                    destination,
                    mode);

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

            if (result != null) {
                try {
                    JSONObject directionsResultJson = new JSONObject(result);
                    routeGeometry = directionsResultJson.getJSONArray("routes").getJSONObject(0).getString("geometry");
                    List<Position> positionList =  PolylineUtils.decode(routeGeometry, 5);

                    // Convert Positions List into LatLng
                    for (Position pos : positionList) {
                        points.add(new LatLng(pos.getLatitude(), pos.getLongitude()));
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

    private LatLng getPointClosestCampus(List<GeocoderFeature> geocoderFeatures, LatLngBounds bounds,
                                     Location user_location) {

        List<LatLng> inBoundsPoints = new ArrayList<>();
        for (GeocoderFeature point : geocoderFeatures) {
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            if (bounds.contains(latLng)) {
                inBoundsPoints.add(latLng);
            }
        }

        double minDistance = -1;
        LatLng closePoint = CENTER_CAMPUS;
        if (user_location != null) {
            closePoint = new LatLng(user_location.getLatitude(), user_location.getLongitude());
        }

        LatLng result = null;
        for (LatLng point : inBoundsPoints) {
            double dist = closePoint.distanceTo(point);
            if (minDistance == -1 || dist < minDistance) {
                minDistance = dist;
                result = point;
            }
        }

        return result;
    }

    protected void onPostExecute(List<LatLng> result) {
        if (mCallBack != null) {
            if (errorCode != 0) {
                if (errorCode == 1) {
                    String info = "You are not connected to the internet. Please try again later.";
                    mCallBack.onFailure(info);
                } else if (errorCode == 2) {
                    String info = "Could not find location, please try again.";
                    mCallBack.onFailure(info);
                }
            }
            else {
                mCallBack.onSuccess(result);
            }
        }
    }
}
