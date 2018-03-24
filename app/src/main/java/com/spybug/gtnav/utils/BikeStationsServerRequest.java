package com.spybug.gtnav.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.spybug.gtnav.BuildConfig;
import com.spybug.gtnav.interfaces.OnEventListener;
import com.spybug.gtnav.models.BikeStation;

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

public class BikeStationsServerRequest extends AsyncTask<Object, Void, List<BikeStation>> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<BikeStation>, String> mCallBack;

    public BikeStationsServerRequest(Context context, OnEventListener<List<BikeStation>, String> callback) {
        contextRef = new WeakReference<>(context);
        mCallBack = callback;
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected List<BikeStation> doInBackground(Object[] objects) {
        List<BikeStation> bikeStations = new ArrayList<>();

        if (!hasNetwork) {
            errorCode = 1;
            return bikeStations;
        }

        String inputLine;
        String stringUrl;
        String result;

        stringUrl = String.format("%sbikes", BuildConfig.API_URL);

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

        if (result != null) {
            try {
                JSONArray stations = new JSONArray(result);

                for (int i = 0; i < stations.length(); i++) {
                    JSONObject station = stations.getJSONObject(i);
                    BikeStation newStation = new BikeStation(station.getString("station_id"),
                            station.getString("name"),
                            station.getDouble("lat"), station.getDouble("lon"),
                            station.getInt("num_bikes_available"), station.getInt("num_bikes_disabled"),
                            station.getInt("num_docks_available"));
                    bikeStations.add(newStation);
                }
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        return bikeStations;
    }

    protected void onPostExecute(List<BikeStation> result) {
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
