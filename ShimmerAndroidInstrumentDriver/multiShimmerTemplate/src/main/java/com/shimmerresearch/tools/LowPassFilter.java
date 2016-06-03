package com.shimmerresearch.tools;

public class LowPassFilter {

	double gain;
	double x[] = new double[3];
	double y[] = new double[3];
    double a, b;
    double samplingRate;
    double cornerFrequency;
    
    public LowPassFilter(double samplingrate, double cutoffFrequency) {
    	x[0] = 0;
    	x[1] = 0;
    	x[2] = 0;
    	y[0] = 0;
    	y[1] = 0;
    	y[2] = 0;
        samplingRate=samplingrate;
        cornerFrequency=cutoffFrequency;
        
        if (samplingRate == 51.2 && cornerFrequency == 5)	
        {
        	gain = 1.542807812e+01;
            a = -0.4213046261;
            b = 1.1620370772;
        } else if (samplingRate == 102.4 && cornerFrequency == 5)	
        {
        	gain = 5.197890536e+01;
            a = -0.6480567349;
            b = 1.5711024402;
        } else if (samplingrate == 128 && cornerFrequency == 5)
        {
            gain = 7.820233128e+01;
            a = -0.7067570632;
            b = 1.6556076929;
        }else if (samplingRate == 204.8 && cornerFrequency == 5)	
        {
        	gain = 1.887247719e+02;
            a = -0.8049825944;
            b = 1.7837877086;
        } else if (samplingRate > 249 && samplingRate < 257 && cornerFrequency == 5)			
        {
        	gain = 2.889601535e+02;
            a = -0.8406758501;
            b = 1.8268331110;
        } else if (samplingRate == 512 && cornerFrequency == 5)				
        { 
        	gain = 1.108844743e+03;
            a = -0.9168833468;
            b = 1.9132759887;
        } else if (samplingRate == 1024 && cornerFrequency == 5)			
        { 
        	gain = 4.342236967e+03;
            a = -0.9575402448;
            b = 1.9566190606;
        } else
        {
        	samplingRate = -1;
        	cornerFrequency = -1;
        }
    }
    
    public double filterData(double data) {
        if (samplingRate != -1 && cornerFrequency != -1) {
            x[0] = x[1]; x[1] = x[2];
            x[2] = data / gain;
            y[0] = y[1]; y[1] = y[2];
            y[2] = x[0] + x[2] + 2*x[1] + (a*y[0]) + (b*y[1]);
            return y[2];
        } else {
            return data;
        }
    }


}
