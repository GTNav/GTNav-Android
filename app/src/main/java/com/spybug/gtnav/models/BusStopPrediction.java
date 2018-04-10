package com.spybug.gtnav.models;

import java.util.List;

public class BusStopPrediction extends BusStop {
    public BusStopPrediction(String id, List<Integer> predictions) {
        super(id, "", 0, 0);
        super.estimation_times = predictions;
    }
}
