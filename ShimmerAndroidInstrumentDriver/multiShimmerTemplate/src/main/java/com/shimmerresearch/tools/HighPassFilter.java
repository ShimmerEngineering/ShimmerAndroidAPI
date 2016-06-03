package com.shimmerresearch.tools;

public class HighPassFilter {

	double gain;
	double x[] = new double[3];
	double y[] = new double[3];
    double a, b;
    double samplingRate;
    double cornerFrequency;
    
    public HighPassFilter(double samplingFrequency, double cutoffFrequency) {
    	
    	x[0] = 0;
    	x[1] = 0;
    	x[2] = 0;
    	y[0] = 0;
    	y[1] = 0;
    	y[2] = 0;
        samplingRate=samplingFrequency;
        cornerFrequency=cutoffFrequency;

        if (samplingRate == 51.2 && cornerFrequency == 0.05)
        {
        	gain = 1.004348179e+00;
            a = -0.9913600352;
            b = 1.9913225484;
        } else if (samplingRate == 102.4 && cornerFrequency == 0.05)
        {
        	gain = 1.002171731e+00;
            a = -0.9956706459;
            b = 1.9956612539;
        } else if (samplingRate == 204.8 && cornerFrequency == 0.05)
        {
        	gain = 1.001085277e+00;
            a = -0.9978329750;
            b = 1.9978306244;
        } else if (samplingRate > 249 && samplingRate < 257 && cornerFrequency == 0.05)	
        {
        	gain = 1.000868127e+00;
            a = -0.9982660040;
            b = 1.9982644993;
        } else if (samplingRate == 512 && cornerFrequency == 0.05) 	
        { 
        	gain = 1.000433969e+00;
            a = -0.9991326258;
            b = 1.9991322495;
        } else if (samplingRate == 1024 && cornerFrequency == 0.05)
        {
        	gain = 1.000216961e+00;
            a = -0.9995662188;
            b = 1.9995661247;
        }  else  if (samplingRate == 51.2 && cornerFrequency == 0.5)
        {
        	gain = 1.044342976e+00;
            a = -0.9168833468;
            b = 1.9132759887;
        } else if (samplingRate == 102.4 && cornerFrequency == 0.5)	
        {
        	gain = 1.021930813e+00;
            a = -0.9575402448;
            b = 1.9566190606;
        } else if (samplingRate == 204.8 && cornerFrequency == 0.5)		
        {
        	gain = 1.010905925e+00;
            a = -0.9785398530;
            b = 1.9783070727;
        } else if (samplingRate > 249 && samplingRate < 257  && cornerFrequency == 0.5)	
        {
        	gain = 1.008715265e+00;
            a = -0.9827947193;
            b = 1.9826454185;
        } else if (samplingRate == 512 && cornerFrequency == 0.5) 
        {
        	gain = 1.004348179e+00;
            a = -0.9913600352;
            b = 1.9913225484;
        } else if (samplingRate == 1024 && cornerFrequency == 0.5)	
        {
        	gain = 1.002171731e+00;
            a = -0.9956706459;
            b = 1.9956612539;
        }
        /////
        else if (samplingRate == 51.2 && cornerFrequency == 5)	
        {
        	gain = 1.548382080e+00;
            a = -0.4213046261;
            b = 1.1620370772;
        } else if (samplingRate == 102.4 && cornerFrequency == 5)	
        {
        	gain = 1.242560489e+00;
            a = -0.6480567349;
            b = 1.5711024402;
        } else if (samplingRate == 204.8 && cornerFrequency == 5)	
        {
        	gain = 1.114587912e+00;
            a = -0.8049825944;
            b = 1.7837877086;
        } else if (samplingRate > 249 && samplingRate < 257  && cornerFrequency == 5)			
        {
        	gain = 1.090658548e+00;
            a = -0.8406758501;
            b = 1.8268331110;
        } else if (samplingRate == 512 && cornerFrequency == 5)				
        {
        	gain = 1.044342976e+00;
            a = -0.9168833468;
            b = 1.9132759887;
        } else if (samplingRate == 1024 && cornerFrequency == 5)			
        { 
        	gain = 1.021930813e+00;
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
            y[2] = x[0] + x[2] - 2*x[1] + (a*y[0]) + (b*y[1]);
            return y[2];
        } else {
            return data;
        }
    }
	


}
