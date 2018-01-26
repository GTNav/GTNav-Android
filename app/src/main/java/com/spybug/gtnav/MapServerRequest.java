package com.spybug.gtnav;

import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Background task to communicate with the map server
 */

public class MapServerRequest extends AsyncTask {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        String start = (String) objects[0];
        String end = (String) objects[1];
        View view = (View) objects[2];
        EditText startText = view.findViewById(R.id.start_location);
        EditText endText = view.findViewById(R.id.end_location);
        startText.setText(end);
        endText.setText(start);
        //TODO
        return view;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object o) {
        //TODO
    }

}
