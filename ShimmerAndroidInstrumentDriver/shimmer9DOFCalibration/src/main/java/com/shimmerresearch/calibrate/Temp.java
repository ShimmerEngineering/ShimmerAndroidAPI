package com.shimmerresearch.calibrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import pl.flex_it.androidplot.XYSeriesShimmer9DOF;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.shimmerresearch.slidingmenu.R;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

	public class Temp extends Fragment {
		
	    private static XYPlot xyPlot;
	    private XYSeriesShimmer9DOF series;
	    private LineAndPointFormatter series1Format;
	    private Button testButton;
	    private ArrayList<Number> ALdata1, ALdata2;
	    private int Adata1[], Adata2[];
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_test, container, false);
	        
	        // Import plot from the layout
	        //xyPlot = (XYPlot) rootView.findViewById(R.id.xyPlot);
	        xyPlot.setDomainBoundaries(-5, 5, BoundaryMode.FIXED); // freeze the domain boundary:
	        xyPlot.setRangeBoundaries(-5, 5, BoundaryMode.FIXED);
	        Log.d("Thresh", "Here alright123!");
	        //testButton = (Button) rootView.findViewById(R.id.btnTest);

	        testButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
						ALdata1.clear();
						ALdata2.clear();
						xyPlot.clear();
						xyPlot.redraw();
						plotDataMethod();
				}
			});
	        
	        
	 		ALdata1 = new ArrayList<Number>();
	 		ALdata2 = new ArrayList<Number>();
	 		ALdata1.clear();
	 		ALdata2.clear();
	 		
	 		Adata1 = new int[]{0,1,0,-1,-3,-5};
	 		Adata2 = new int[]{1,0,-1,0,1,2};
	 		
	        series = new XYSeriesShimmer9DOF(ALdata1, ALdata2, 0, "Sightings in USA");
	        series1Format = new LineAndPointFormatter(Color.TRANSPARENT, Color.BLACK, null); // line color, point color, fill color
	        xyPlot.addSeries(series, series1Format);
	        
	        plotDataMethod();
	        
	        return rootView;
	    }
		
		private void plotDataMethod() {

			for(int i=0; i<Adata1.length; i++){
				ALdata1.add(Adata1[i]);
				ALdata2.add(Adata2[i]);
				/*
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				Log.d("Thresh", Integer.toString(Adata1[i]));
				Log.d("Thresh", Integer.toString(Adata2[i]));
				series.updateData(ALdata1, ALdata2);
				xyPlot.redraw();
			}

		}
   
	}



