package com.spybug.gtnav.models;

import java.util.List;

public class BusStopPrediction extends BusStop {
    public BusStopPrediction(String id, String routeName, List<Integer> predictions) {
        super(id, "", routeName, 0, 0);
        super.estimation_times = predictions;
    }
}
