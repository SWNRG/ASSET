package com.uom.georgevio;

public class Object2Double {

	
	public double[] convert(Object[] sentArray){
		double[] doubleSentArray = new double[sentArray.length];
		for(int i = 0; i < sentArray.length; i++){
			int curInt = (int)sentArray[i];
			double curD = (double) curInt;
			doubleSentArray[i] = curD;
		}
		return doubleSentArray;
	}
}
