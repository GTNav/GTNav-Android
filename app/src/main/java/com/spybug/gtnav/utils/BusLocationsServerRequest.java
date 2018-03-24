package com.spybug.gtnav.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.spybug.gtnav.BuildConfig;
import com.spybug.gtnav.interfaces.OnEventListener;
import com.spybug.gtnav.models.Bus;

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

public class BusLocationsServerRequest extends AsyncTask<Object, Void, List<Bus>> {

    private static final String REQUEST_METHOD = "GET";
    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECTION_TIMEOUT = 15000;
    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<Bus>, String> mCallBack;

    public BusLocationsServerRequest(Context context, OnEventListener<List<Bus>, String> callback) {
        contextRef = new WeakReference<>(context);
        mCallBack = callback;
    }

    protected void onPreExecute() {
        hasNetwork = haveNetworkConnection(contextRef.get());
    }

    @Override
    protected List<Bus> doInBackground(Object[] objects) {
        List<Bus> busList = new ArrayList<>();
        String routeTag = (String)objects[0];

        if (!hasNetwork) {
            errorCode = 1;
            return busList;
        }

        String inputLine;
        String stringUrl;
        String result;

        stringUrl = String.format("%sbuses?route=%s",
                BuildConfig.API_URL,
                routeTag);

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
                JSONArray vehicles = new JSONArray(result);

                for (int i = 0; i < vehicles.length(); i++) {
                    JSONObject vehicle = vehicles.getJSONObject(i);
                    Bus newBus = new Bus(vehicle.getInt("id"),
                            vehicle.getDouble("lat"), vehicle.getDouble("lon"),
                            vehicle.getInt("heading"), vehicle.getString("dirTag"));
                    busList.add(newBus);
                }
            }
            catch(JSONException ex) {
                ex.printStackTrace();
            }
        }

        return busList;
    }

    protected void onPostExecute(List<Bus> result) {
        if (mCallBack != null) {
            if (errorCode != 0) {
                if (errorCode == 1) {
                    String info = "You are not connected to the internet. Please try again later.";
                    mCallBack.onFailure(info);
                }
            }
            else if (result.size() > 0) {
                mCallBack.onSuccess(result);
            }
        }
    }
}
