package com.spybug.gtnav.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;
import com.spybug.gtnav.BuildConfig;
import com.spybug.gtnav.interfaces.OnEventListener;

import org.json.JSONArray;
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

import static com.spybug.gtnav.utils.HelperUtil.haveNetworkConnection;


/**
 * Background task to get a bus route
 */

public class BusRouteServerRequest extends AsyncTask<Object, Void, List<LatLng>> {

    private WeakReference<Context> contextRef;
    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<LatLng>, String> mCallBack;

    public BusRouteServerRequest(Context context, OnEventListener<List<LatLng>, String> callback) {
        contextRef = new WeakReference<>(context);
        mCallBack = callback;
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected List<LatLng> doInBackground(Object[] objects) {
        List<LatLng> points = new ArrayList<>();
        String routeTag = (String)objects[0];

        if (!hasNetwork) {
            errorCode = 1;
            return points;
        }

        String inputLine;
        String stringUrl;
        String result;

        stringUrl = String.format("%sroutes?route=%s",
                BuildConfig.API_URL,
                routeTag);

        try {

            URL myUrl = new URL(stringUrl);
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();

            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.connect();

            InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();
            while((inputLine = reader.readLine()) != null){
                stringBuilder.append(inputLine);
            }
            reader.close();
            streamReader.close();
            result = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            result = null;
        }

        if (result != null) {
            try {
                JSONObject resultJSON = new JSONObject(result);
                String encodedString = resultJSON.getString("route");

                List<Position> positionList = PolylineUtils.decode(encodedString, 5);

                // Convert Positions List into LatLng
                for (Position pos : positionList) {
                    points.add(new LatLng(pos.getLatitude(), pos.getLongitude()));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return points;
    }

    protected void onPostExecute(List<LatLng> result) {
        if (mCallBack != null) {
            if (errorCode != 0) {
                if (errorCode == 1) {
                    String info = "You are not connected to the internet. Please try again later.";
                    mCallBack.onFailure(info);
                }
            }
            else {
                mCallBack.onSuccess(result);
            }
        }
    }
}
