package com.shimmerresearch.shimmerserviceexample;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class EnabledSensorsFragment extends Fragment {


    public EnabledSensorsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("Enabled Sensors");
        return textView;
    }

    public static EnabledSensorsFragment newInstance() {
        EnabledSensorsFragment fragment = new EnabledSensorsFragment();
        return fragment;
    }


}
