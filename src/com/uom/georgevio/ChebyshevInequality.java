package com.uom.georgevio;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Provides Chebyshev's Inequality method to find outliers for 
 * non-normally distributed data sets
 */
public class ChebyshevInequality {// implements IOutlierDetector {

	DecimalFormat df = new DecimalFormat();
	
    /* threshold to determine the minimum of values within a data set 
     * must lie in. 90% = 0.9
     */
    private double probabilityThreshold = 0.90;

    public List<Double> getOutlierScoreIterator(double valueToCheck, Iterator iter) {
    	List<Double> myList = (List<Double>) iter; 
    	int count = 0;
		while(iter.hasNext()) {
			int  k = (int)iter.next();
			double d = k; //straight casting from int to double
			count++;
		}
		return myList;
    }
    /**
     * Check if given value is an outlier in the data set using Chebyshev's Inequality
     *
     * @param valueToCheck   the given values to check
     * @param timeSeriesData the time series data
     * @return true if value is an outlier. Otherwise false.
     */
    public boolean isOutlier(double valueToCheck, double[] timeSeriesData, int nodeId) {
        double outlierScore = getOutlierScore(valueToCheck, timeSeriesData, nodeId);
        return isOutlierByChebyshevsInequality(valueToCheck, outlierScore);
    }

    /**
     * Calculates the outlier score using value to be checked and time series data
     *
     * @param valueToCheck   the given values to check
     * @param timeSeriesData the time series data
     * @return an outlier score
     * @throws Exception
     */
    public double getOutlierScore(double valueToCheck, double[] timeSeriesData, int nodeId) {
    	df.setMaximumFractionDigits(3); //max double number digits
    	
    	// get mean and standard deviation
        DescriptiveStatistics statistics = new DescriptiveStatistics(timeSeriesData);
        double sampleMean = statistics.getMean();
        double sampleStdDev = statistics.getStandardDeviation();

        // get k (number of standard deviations away from the mean)
        double k = getK(probabilityThreshold);

        validateChebyshev(valueToCheck, sampleMean, sampleStdDev, k);

        double acceptableDeviation = k * sampleStdDev;
        double min = sampleMean - acceptableDeviation;
        double max = sampleMean + acceptableDeviation;
        double currentDeviation = Math.abs(valueToCheck - sampleMean);

        double outlierScore = currentDeviation / acceptableDeviation;
        if(outlierScore > 1.) {
        	debugBoth("--------Node:"+nodeId+" isOutlier outside limits--------");
	        debugBoth("outlierScore = " + df.format((outlierScore)));
	        debugBoth(df.format(Math.floor(acceptableDeviation)) + " acceptableDeviation");
	        debugBoth(valueToCheck + " valueToCheck");
	        debugBoth(df.format(Math.floor(min)) + " min");
	        debugBoth(df.format(Math.floor(max)) + " max");
	        debugBoth("--------------------------");
        }
        return outlierScore;
    }

    /**
     * Calculates the k (number of standard deviations away from the mean)
     * for the specified probability
     *
     * @param probability the probability that a minimum of just 'probability' percent
     *                    of values within a data set must lie within k standard deviations of the mean.
     * @return the number of standard deviations away from the mean for the given probability.
     */
    public double getK(double probability) {

        if (Math.abs(probability) > 1) {
            return 0;
        }

        double k = Math.sqrt(1. / (1. - probability));
        return k;
    }

    /**
     * Check if given value is an outlier
     *
     * @param valueToCheck the value to check
     * @param outlierScore the score of the outlier
     * @return true if value is an outlier. Otherwise false.
     */
    private boolean isOutlierByChebyshevsInequality(double valueToCheck, double outlierScore) {
        // value in range of min and max
        //boolean isOutlier = !((valueToCheck > min) && (valueToCheck < max));
        //boolean isOutlier = outlierScore > 1.;
        //return !((valueToCheck > min) && (valueToCheck < max));
        return outlierScore > 1.;
    }

    /**
     * Check the condition to be able to use Chebyshev's Inequality Theorem
     *
     * @param valueToCheck the given values to check
     * @param sampleMean   the mean of the data set
     * @param sampleStdDev the standard deviation of the data set
     * @param k            the number of standard deviations away from the mean
     */
    private void validateChebyshev(double valueToCheck, double sampleMean, double sampleStdDev, double k) {
        // standard deviations more than 1;
        if (sampleStdDev <= 1) {
            //debug("Chebyshev's Inequality does not work, because stdDev < 1");
        }

        
        //TODO: George: What is this?
        
        //double zScore = new ZScore().getZScore(valueToCheck, sampleMean, sampleStdDev);
        // as long as the z score’s absolute value is less than or equal to k
        //if (k >= Math.abs(zScore)) {
            //throw new Exception("Chebyshev's Inequality wont work, because k < z-score");
        //}

    }
/***************************************************************************/    
	private void debugBoth(String message) {
		Main.debug(message);
		Main.debugEssential(message);
	}
/***************************************************************************/ 
	private void debug(String message){
		Main.debug((message));
	}
}