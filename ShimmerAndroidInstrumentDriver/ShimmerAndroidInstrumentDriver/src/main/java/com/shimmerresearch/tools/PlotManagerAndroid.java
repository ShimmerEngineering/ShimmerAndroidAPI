/* Rev 0.2
 * Changes since 0.1
 * - Added removeSignalFromDevice function
 */

package com.shimmerresearch.tools;

import android.graphics.Color;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;
import com.shimmerresearch.guiUtilities.AbstractPlotManager;

import java.util.ArrayList;
import java.util.List;

import pl.flex_it.androidplot.XYSeriesShimmer;

public class PlotManagerAndroid extends AbstractPlotManager {
	
	public static List<XYSeriesShimmer> mListofSeries = new ArrayList<XYSeriesShimmer>();
	int mNumberOfRowPropertiestoCheck = 3;
	XYPlot mDynamicPlot = null;
	int mXAxisLimit = 500;
	boolean mClearGraphatLimit = false;

	public void setXAxisLimit(int limit,boolean clearGraph){
		mXAxisLimit = limit;
		for (XYSeriesShimmer xys:mListofSeries){
			xys.setXAxisLimit(mXAxisLimit);
			xys.setClearGraphatLimit(clearGraph);
		}
	}

	@Override
	public void setTraceLineStyleAll(PLOT_LINE_STYLE plot_line_style) {

	}

	/**Constructor
	 * 
	 */
	public PlotManagerAndroid(){
	
	}
	
	/**Constructor 
	 * @param clearGraphatLimit Clears the graph once the X Axis limit is reached for the series
	 */
	public PlotManagerAndroid(boolean clearGraphatLimit){
		mClearGraphatLimit=clearGraphatLimit;
	}
	
	/**Constructor
	 * @param limit Sets the X axis limit for the series
	 */
	public PlotManagerAndroid(int limit){
		mXAxisLimit = limit;
	}
	
	/**Constructor
	 * @param limit Sets the X axis limit for the series
	 * @param clearGraphatLimit Clears the graph once the X Axis limit is reached for the series
	 */
	public PlotManagerAndroid(int limit, boolean clearGraphatLimit){
		mClearGraphatLimit=clearGraphatLimit;
		mXAxisLimit = limit;
	}
	
	/**Constructor
	 * @param propertiestoPlot Sets the properties to plot 
	 * @param limit Sets the X axis limit for the series
	 * @param chart the XYPlot in main UI thread so the series can be added
	 * @throws Exception if the list of signals have duplicates
	 */
	public PlotManagerAndroid(List<String[]> propertiestoPlot, int limit, XYPlot chart) throws Exception {
		mXAxisLimit = limit;
		for (int i=0;i<propertiestoPlot.size();i++){
			addSignal(propertiestoPlot.get(i),chart);
		}
	}

	/**Constructor
	 * 
	 * @param propertiestoPlot Sets the properties to plot 
	 * @param limit Sets the X axis limit for the series
	 * @param chart the XYPlot in main UI thread so the series can be added
	 * @param clearGraphatLimit Clears the graph once the X Axis limit is reached for the series
	 * @throws Exception if the list of signals have duplicates
	 */
	public PlotManagerAndroid(List<String[]> propertiestoPlot, int limit, XYPlot chart, boolean clearGraphatLimit) throws Exception {
		mXAxisLimit = limit;
		mClearGraphatLimit=clearGraphatLimit;
		for (int i=0;i<propertiestoPlot.size();i++){
			addSignal(propertiestoPlot.get(i),chart);
		}
	}
	
	/** Constructor
	 * @param propertiestoPlot Sets the properties to plot
	 * @param listofColors List of colors to use for the plots
	 * @param limit Sets the X axis limit for the series 
	 * @param chart the XYPlot in main UI thread so the series can be added
	 * @throws Exception if the list of signals have duplicates
	 */
	public PlotManagerAndroid(List<String[]> propertiestoPlot, List<int[]> listofColors, int limit, XYPlot chart) throws Exception {
		mXAxisLimit = limit;
		for (int i=0;i<propertiestoPlot.size();i++){
			addSignal(propertiestoPlot.get(i),chart,listofColors.get(i));
		}
	}
	
	/** Constructor
	 * @param propertiestoPlot Sets the properties to plot
	 * @param listofColors List of colors to use for the plots
	 * @param limit Sets the X axis limit for the series 
	 * @param chart the XYPlot in main UI thread so the series can be added
	 * @param clearGraphatLimit Clears the graph once the X Axis limit is reached for the series
	 * @throws Exception if the list of signals have duplicates
	 */
	public PlotManagerAndroid(List<String[]> propertiestoPlot, List<int[]> listofColors, int limit, XYPlot chart, boolean clearGraphatLimit) throws Exception {
		mXAxisLimit = limit;
		mClearGraphatLimit=clearGraphatLimit;
		for (int i=0;i<propertiestoPlot.size();i++){
			addSignal(propertiestoPlot.get(i),chart,listofColors.get(i));
		}
	}
	
	
	/**This is important where in cases an activity is closed and recreated. The new chart can be updated here.
	 * @param chart the chart to be updated
	 */
	public void updateDynamicPlot(XYPlot chart){
		mDynamicPlot = chart;
		for (int i=0;i<mListofSeries.size();i++){
			int [] colorrgbaray = mListOfTraceColorsCurrentlyUsed.get(i);
			LineAndPointFormatter lapf = new LineAndPointFormatter(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]);
			lapf = new LineAndPointFormatter(Color.rgb(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]), null, null);
			mDynamicPlot.addSeries(mListofSeries.get(i), lapf);
		}
	}

	/**Adds a signal to the plot manager. Note that the signal array should be equivalent to the standard Shimmer format DeviceName->Property->Format->Unit (e.g. the four columns of the csv file)
	 *
	 * @param signal
	 * @param chart
	 * @throws Exception if the signal already exist
	 */
	public void addSignal(String [] signal, XYPlot chart) throws Exception{
		if (!checkIfPropertyExist(signal)){
		//Add the series first, cause the filter might find the signal before the series is added
		int index = mListofSeries.size();
		String name = signal[0]+ " " + signal[1]+" " + signal[2] + " " + signal[3];
		//List<Number> data = new ArrayList<Number>();
		List<Number> d = new ArrayList<Number>();
		XYSeriesShimmer xys = new XYSeriesShimmer(d);
		xys.setTitle(name);
		xys.setXAxisLimit(mXAxisLimit);
		xys.setClearGraphatLimit(mClearGraphatLimit);
		mListofSeries.add(xys);
		super.addSignalGenerateRandomColor(signal);
		int [] colorrgbaray = mListOfTraceColorsCurrentlyUsed.get(index);
		LineAndPointFormatter lapf = new LineAndPointFormatter(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]);
		lapf = new LineAndPointFormatter(Color.rgb(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]), null, null);
		chart.addSeries(mListofSeries.get(index),lapf);
		mDynamicPlot = chart;
		
		for (XYSeriesShimmer xy: mListofSeries){
			xy.clearData();
		}
		}else {
			throw new Exception("Error: " + joinChannelStringArray(signal) +" Signal/Property already exist.");
		}
	}
	
	
	/**Adds a signal to the plot manager. Note that the signal array should be equivalent to the standard Shimmer format DeviceName->Property->Format->Unit (e.g. the four columns of the csv file)
	 * 
	 * @param signal 
	 * @param chart
	 * @throws Exception if the signal already exist
	 */
	public void addSignal(String [] signal, XYPlot chart,int[] color) throws Exception{
		if (!checkIfPropertyExist(signal)){
		//Add the series first, cause the filter might find the signal before the series is added
		
		String name = joinChannelStringArray(signal);
		//List<Number> data = new ArrayList<Number>();
		List<Number> d = new ArrayList<Number>();
		int index = mListofSeries.size();
		XYSeriesShimmer xys = new XYSeriesShimmer(d);
		xys.setTitle(name);
		xys.setXAxisLimit(mXAxisLimit);
		mListofSeries.add(xys);
		super.addSignalandColor(signal,color);
		int [] colorrgbaray = mListOfTraceColorsCurrentlyUsed.get(index);
		LineAndPointFormatter lapf = new LineAndPointFormatter(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]);
		lapf = new LineAndPointFormatter(Color.rgb(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]), null, null);
		chart.addSeries(mListofSeries.get(index),lapf);
		mDynamicPlot = chart;
		
		for (XYSeriesShimmer xy: mListofSeries){
			xy.clearData();
		}
		} else {
			throw new Exception("Error: " + joinChannelStringArray(signal) +" Signal/Property already exist.");
		}
	}
	
	/** Removes all signals
	 *
	 */
	public void removeAllSignals(){
		super.removeAllSignals();
		if (mDynamicPlot!=null){
			mDynamicPlot.clear();
		}
		mListofSeries.clear();
	}
	
	
	/** Removes a signal
	 * @param signal
	 */
	public void removeSignal(String[] signal){
		for (int i=0;i<mListofPropertiestoPlot.size();i++){
			String[] prop = mListofPropertiestoPlot.get(i);
			boolean found = true;
			for (int p=0;p<mNumberOfRowPropertiestoCheck;p++){
				if (prop[p].equals(signal[p])){
					
				} else {
					found = false;
				}
			}
			if (found){
				mDynamicPlot.removeSeries(mListofSeries.get(i));
				mListofSeries.remove(i);
				super.removeSignal(i);
			}
			
		}
	}
	
	public void removeSignalFromDevice(String namedevice){
		
		List<XYSeriesShimmer> tempListOfSeries = new ArrayList<XYSeriesShimmer>();
		ArrayList<String[]> propertiesToRemove = new ArrayList<String[]>();
		ArrayList<Integer> listOfIndex = new ArrayList<Integer>();
		ArrayList<int[]> tempListOfColors = new ArrayList<int[]>();
		for (int i=0;i<mListofPropertiestoPlot.size();i++){
			String[] prop = mListofPropertiestoPlot.get(i);
			if(prop[0].equals(namedevice)){
				listOfIndex.add(i);
				propertiesToRemove.add(prop);
				tempListOfSeries.add(mListofSeries.get(i));
				tempListOfColors.add(mListOfTraceColorsCurrentlyUsed.get(i));
			}
		}
		
		
		for(int j=listOfIndex.size()-1; j>=0; j--){
			mDynamicPlot.removeSeries(mListofSeries.get(listOfIndex.get(j)));
//			mListofSeries.remove(listOfIndex.get(j));
//			super.removeSignal(listOfIndex.get(j));
		}		
		mListofSeries.removeAll(tempListOfSeries);
		super.removeCollectionOfSignal(propertiesToRemove, tempListOfColors);
			
	}
		
	/**This plots the data of the specified signals  
	 * 
	 * @param ojc ObjectCluster holding the data
	 * @throws Exception When signal is not found
	 */
	public void filterDataAndPlot (ObjectCluster ojc) throws Exception {
		// TODO Auto-generated method stub
		
		for (int i=0;i<mListofPropertiestoPlot.size();i++){
			String[] props = mListofPropertiestoPlot.get(i);
			if (ojc.getShimmerName().equals(props[0])){
				
				FormatCluster f = ObjectCluster.returnFormatCluster(ojc.getCollectionOfFormatClusters(props[1]), props[2]);
				if (f!=null){
					mListofSeries.get(i).addData(f.mData);
				} else {
					throw new Exception("Signal not found");
				}
			}
		}
		if (mDynamicPlot!=null){
			mDynamicPlot.redraw();
		}
	}
	
	
	
	

}


/*package com.shimmerresearch.advance;

import info.monitorenter.gui.chart.ITrace2D;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

public class PlotManager extends AbstractPlotManager {
	
	int x = 0;
	
	
	public PlotManager(List<String[]> propertiestoPlot) {
		super(propertiestoPlot);
		// TODO Auto-generated constructor stub
		x=0;
	}

	public PlotManager(List<String[]> propertiestoPlot,List<int[]> listofColors) {
		super(propertiestoPlot,listofColors);
		// TODO Auto-generated constructor stub
		x=0;
	}
	
	public List<ITrace2D> FilterData(ObjectCluster ojc,List<ITrace2D> traces) {
		// TODO Auto-generated method stub
		
		for (int i=0;i<mListofPropertiestoPlot.size();i++){
			String[] props = mListofPropertiestoPlot.get(i);
			if (ojc.mMyName.equals(props[0])){
				FormatCluster f = ObjectCluster.returnFormatCluster(ojc.mPropertyCluster.get(props[1]), props[2]);
				int [] colorrgbaray = mListofColors.get(i);
				Color color = new Color(colorrgbaray[0], colorrgbaray[1], colorrgbaray[2]);
				traces.get(i).setColor(color);
				String name = props[0]+ " " + props[1]+" " + props[2] + " " + props[3];
				traces.get(i).setName(name);
				traces.get(i).addPoint(x, f.mData);
			}
		}
		x++;
		return traces;
		
	}

}
*/
