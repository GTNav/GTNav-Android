package com.spybug.gtnav;

import android.content.Context;
import android.os.AsyncTask;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.commons.utils.PolylineUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.spybug.gtnav.HelperUtil.haveNetworkConnection;


/**
 * Background task to get a bus route
 */

public class BusRouteServerRequest extends AsyncTask<Object, Void, List<LatLng>> {

    private WeakReference<Context> contextRef;
    private boolean hasNetwork = true;
    private int errorCode = 0;
    private OnEventListener<List<LatLng>, String> mCallBack;

    BusRouteServerRequest(Context context, OnEventListener<List<LatLng>, String> callback) {
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

        String routeGeometry = "yccmEznbbO??M??J?J?~A?ZAh@AL?B?DCLINEHSXSVGJCDILMTSb@Yr@Mb@CLE\\E^AFGr@QlB" +
                "AF?FADAPKhAq@rHAJAF?DE^Kh@ITIRWb@WZCBSNKFQHA@UFE@SBOB[@]?IAa@AeDK??q@Cg@CI?a@AyAGI??T@zF@x" +
                "A}A?{@CE?a@AG?_@AC?m@??vA?PAjD?dBEDmAAcBAGU?}B?[?Q?I?e@?c@AwA?S?O@O?{F?OAMGOHI|@w@j@g@\\]b@c@d" +
                "@e@PQJIHKESEQCYAW?A?[@[@YDc@Hs@@IHu@BQ@QBM@k@@gE@y@?S?S?O?W?E?u@?K@{@@UBUBMFUBKL[N[DIFIFIFGPMRMJGFC" +
                "JG`Ai@BCBADGDEBEBIBIDO@S?q@@aB?W@W?g@?O?I?Q?M@iB@k@@oC`@AF?p@EZCxAKJ?j@CL?J?J?V@\\?V?F?R?Z@|@?nA@T?R?N?F?" +
                "~BApA@jA@`@?^?P?`@@F?`@B@?ZB\\Hf@Dj@@V@vA?h@??_A?OAMACACECGAE@GBGH?JAL?NBPA^wA?WAk@Ag@E]I?lD?HExA?x@?H?rDAp@?xB[?{@AgBA";

        List<Position> positionList =  PolylineUtils.decode(routeGeometry, 5);

        // Convert Positions List into List<LatLng>
        for (int i = 0; i < positionList.size(); i++) {
            points.add(new LatLng(
                    positionList.get(i).getLatitude(),
                    positionList.get(i).getLongitude()));
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
