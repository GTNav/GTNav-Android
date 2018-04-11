package com.spybug.gtnav.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.spybug.gtnav.BuildConfig;
import com.spybug.gtnav.interfaces.OnEventListener;
import com.spybug.gtnav.models.BikeStation;
import com.spybug.gtnav.models.BusStop;

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
 * Background task to communicate with the map server
 */

public class BusStopServerRequest extends AsyncTask<Object, Void, List<BusStop>> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<BusStop>, String> mCallBack;

    public BusStopServerRequest(Context context, OnEventListener<List<BusStop>, String> callback) {
        contextRef = new WeakReference<>(context);
        mCallBack = callback;
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected List<BusStop> doInBackground(Object[] objects) {
        List<BusStop> busStops = new ArrayList<>();
        String routeName = (String) objects[0];

        if (!hasNetwork) {
            errorCode = 1;
            return busStops;
        }

        String inputLine;
        String stringUrl;
        String result;

        stringUrl = String.format("%sstops?route=%s", BuildConfig.API_URL, routeName);

        try {
            URL myUrl = new URL(stringUrl);
            HttpURLConnection connection =(HttpURLConnection) myUrl.openConnection();

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

        if (result != null && !result.equals("")) {
            try {
                JSONArray stops = new JSONArray(result);

                for (int i = 0; i < stops.length(); i++) {
                    JSONObject stop = stops.getJSONObject(i);
                    BusStop newStop = new BusStop(stop.getString("StopTag"),
                            stop.getString("Title"), routeName,
                            stop.getDouble("Latitude"), stop.getDouble("Longitude"));
                    busStops.add(newStop);
                }
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        return busStops;
    }

    protected void onPostExecute(List<BusStop> result) {
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
