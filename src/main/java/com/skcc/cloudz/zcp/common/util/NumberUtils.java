package com.skcc.cloudz.zcp.common.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtils {

	public static String percentFormat(double used, double hard) {
		double value = 0;
		if (used > 0 && hard > 0) {
			value = used / hard * 100;
		}
		
		NumberFormat nf =  NumberFormat.getPercentInstance();
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern("##.##");

		StringBuilder sb = new StringBuilder();
		sb.append(df.format(value));//.append("%");
		
		return sb.toString();
	}
}
