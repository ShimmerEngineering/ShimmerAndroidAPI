package com.shimmerresearch.shimmerserviceexample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.Plot;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;

import static android.R.attr.width;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlotFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlotFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlotFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static XYPlot dynamicPlot;
    public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(4);
    public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(4);
    public static int X_AXIS_LENGTH = 500;
    private Paint transparentPaint, outlinePaint;
    static LineAndPointFormatter lineAndPointFormatter1, lineAndPointFormatter2, lineAndPointFormatter3;
    private static Paint LPFpaint;




    public PlotFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlotFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlotFragment newInstance(String param1, String param2) {
        PlotFragment fragment = new PlotFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plot, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        initPlot();

        super.onActivityCreated(savedInstanceState);
    }

    /**
     * This function sets up the graph
     */
    private void initPlot() {
        dynamicPlot = (XYPlot) getView().findViewById(R.id.dynamicPlot);

        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));
        lineAndPointFormatter1 = new LineAndPointFormatter(Color.rgb(51, 153, 255), null, null); // line color, point color, fill color
        LPFpaint = lineAndPointFormatter1.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter1.setLinePaint(LPFpaint);
        lineAndPointFormatter2 = new LineAndPointFormatter(Color.rgb(245, 146, 107), null, null);
        LPFpaint = lineAndPointFormatter2.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter2.setLinePaint(LPFpaint);
        lineAndPointFormatter3 = new LineAndPointFormatter(Color.rgb(150, 150, 150), null, null);
        LPFpaint = lineAndPointFormatter3.getLinePaint();
        LPFpaint.setStrokeWidth(3);
        lineAndPointFormatter3.setLinePaint(LPFpaint);
        transparentPaint = new Paint();
        transparentPaint.setColor(Color.TRANSPARENT);
        //lineAndPointFormatter1.setLinePaint(p);
        dynamicPlot.setDomainStepMode(XYStepMode.SUBDIVIDE);
        //dynamicPlot.setDomainStepValue(series1.size());
        dynamicPlot.getLegendWidget().setTableModel(new DynamicTableModel(1, 4));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        dynamicPlot.getLegendWidget().setSize(new SizeMetrics(width/2, SizeLayoutType.ABSOLUTE, height/3, SizeLayoutType.ABSOLUTE));
        // thin out domain/range tick labels so they dont overlap each other:
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setTicksPerRangeLabel(3);
        dynamicPlot.disableAllMarkup();
        Paint gridLinePaint = new Paint();
        gridLinePaint.setColor(Color.parseColor("#969696")); // black
        Paint transparentLinePaint = new Paint();
        transparentLinePaint.setColor(Color.TRANSPARENT);
        dynamicPlot.setRangeBoundaries(-15, 15, BoundaryMode.AUTO); // freeze the range boundary:
        dynamicPlot.setDomainBoundaries(0, X_AXIS_LENGTH, BoundaryMode.FIXED); // freeze the domain boundary:
//        dynamicPlot.getGraphWidget().setMargins(0, 20, 10, 10);
        dynamicPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
        dynamicPlot.getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().setGridLinePaint(transparentPaint);
        dynamicPlot.getGraphWidget().setDomainOriginLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(gridLinePaint);
//        dynamicPlot.getGraphWidget().setDomainLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setDomainLabelPaint(transparentLinePaint);
        dynamicPlot.getGraphWidget().getDomainLabelPaint().setTextSize(20);
        dynamicPlot.getDomainLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getDomainOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getGraphWidget().setClippingEnabled(false);
        dynamicPlot.getGraphWidget().setRangeOriginLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(gridLinePaint);
        dynamicPlot.getGraphWidget().setRangeLabelPaint(gridLinePaint);
        dynamicPlot.getGraphWidget().getRangeLabelPaint().setTextSize(20);
        dynamicPlot.getRangeLabelWidget().pack();
        outlinePaint = dynamicPlot.getGraphWidget().getRangeOriginLinePaint();
        outlinePaint.setStrokeWidth(3);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());

    }



}
