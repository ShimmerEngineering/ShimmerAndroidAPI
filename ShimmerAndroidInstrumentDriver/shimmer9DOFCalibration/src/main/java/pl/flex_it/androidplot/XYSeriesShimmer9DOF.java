package pl.flex_it.androidplot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.androidplot.series.XYSeries;

public class XYSeriesShimmer9DOF implements XYSeries {
	private List<Number> dataX;
    private List<Number> dataY;
    private int seriesIndex;
    private String title;

    public XYSeriesShimmer9DOF(List<Number> datasource, int seriesIndex, String title) {
        this.dataY = datasource;
        this.seriesIndex = seriesIndex;
        this.title = title;
    }
    
    public XYSeriesShimmer9DOF(List<Number> datasourceX, List<Number> datasourceY, int seriesIndex, String title) {
        this.dataX = datasourceX;
    	this.dataY = datasourceY;
        this.seriesIndex = seriesIndex;
        this.title = title;
    }
    
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return dataY.size();
    }

    @Override
    public Number getY(int index) {
        return dataY.get(index);
    }

    @Override
    public Number getX(int index) {
    	if(this.title.equals("MagPlot")){
    		if (index >= dataX.size()) return dataX.get(dataX.size()-1);
    		else return dataX.get(index);
    	}
    	else return index;
    }
    
    public void updateData(List<Number> datasourceX){ //dont need to use this cause, the reference is only stored, modifying the datasource externally will cause this to be updated as well
    	this.dataY=datasourceX;
    }
    
    public void updateData(List<Number> datasourceX, List<Number> datasourceY){ //dont need to use this cause, the reference is only stored, modifying the datasource externally will cause this to be updated as well
    	this.dataX=datasourceX;
    	this.dataY=datasourceY;
    }
   
}