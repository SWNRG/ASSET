package com.uom.georgevio;

import java.util.Iterator;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class DataAnalysis {

	public DataAnalysis() {
	}

	private double mean; //average
	private double standardDeviation;
	private double geoMean;
	private double median;
	
	public double getMean() {
		return mean;
	}
	
	public double getGeoMean() {
		return geoMean;
	}
	
	public double standardDeviation() {
		return standardDeviation;
	}
	
	public double stDev() {
		return standardDeviation;
	}
	
	public void analyzeData(Iterator iter) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		while(iter.hasNext()) {
			int  k = (int)iter.next();
			//double d = Double.parseDouble( (String) iter.next()); // not working
			double d = k; //straight casting from int to double
			ds.addValue(d);
		}
		
		this.median = ds.getPercentile(50);
		this.geoMean = ds.getGeometricMean();
	    this.mean = ds.getMean(); // be careful: AVERAGE
	    this.standardDeviation = ds.getStandardDeviation();
	}
	
}
