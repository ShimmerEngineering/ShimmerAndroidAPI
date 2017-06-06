package com.shimmerresearch.shimmerserviceexample;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.shimmerresearch.android.shimmerService.ShimmerService;
import com.shimmerresearch.driver.ShimmerDevice;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignalsToPlotFragment extends ListFragment {


    public SignalsToPlotFragment() {
        // Required empty public constructor
    }



    public void buildSignalsToPlotList(Context context, final ShimmerService shimmerService, final String bluetoothAddress, final XYPlot dynamicPlot) {



    }

}
