package com.skcc.cloudz.zcp.iam.common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;

public class NumberUtils {

	private static String percentFormat(double used, double hard) {
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
	
	public static BigDecimal percent(double used, double hard) {
		if (used == 0)
			return new BigDecimal(0);
		if (hard == 0)
			return new BigDecimal(0);
		BigDecimal value = new BigDecimal(percentFormat(used, hard));

		return value;
	}

	/**
	 * 1 = 1000m, 1Core = 1000milliccore
	 * 
	 * @param value
	 * @return cpu size string with m or '' e.g) 1, 200m, 300m, 2
	 */
	public static String formatCpu(double value) {
		if (value == 0)
			return StringUtils.substringBefore(String.valueOf(value), ".");

		if (value < 1) {	// Millicore case
			double formattedValue = value * 1000;
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "m";
		} else {		// Core case
			String decimalValue = StringUtils.substringBefore(String.valueOf(value), ".");
			String decimalPoint = StringUtils.substringAfter(String.valueOf(value), ".");
			StringBuilder sb = new StringBuilder();
			sb.append(decimalValue);
			if(!StringUtils.isEmpty(decimalPoint) && !StringUtils.equals(decimalPoint, "0")) {
				sb.append(".");
				sb.append(StringUtils.substring(decimalPoint, 0, 2));
			}
			return sb.toString();
		}
	}
	
	public static BigDecimal formatCpuWithoutUnit(double value) {
		String data = null;
		
		String decimalValue = StringUtils.substringBefore(String.valueOf(value), ".");
		String decimalPoint = StringUtils.substringAfter(String.valueOf(value), ".");
		StringBuilder sb = new StringBuilder();
		sb.append(decimalValue);
		if(!StringUtils.isEmpty(decimalPoint) && !StringUtils.equals(decimalPoint, "0")) {
			sb.append(".");
			sb.append(StringUtils.substring(decimalPoint, 0, 2));
		}
		data = sb.toString();

		return new BigDecimal(data);
	}

	/**
	 * 1 = 1 byte
	 * 
	 * @param value
	 * @return memory size string with Gi or Mi e.g) 50Gi, 512Mi
	 */
	public static String formatMemory(double value) {
		if (value == 0)
			return StringUtils.substringBefore(String.valueOf(value), ".");

		double formattedValue = value / 1024 / 1024;
		if (formattedValue < 1024) {
			return StringUtils.substringBefore(String.valueOf(formattedValue), ".") + "Mi";
		} else {
			formattedValue = formattedValue / 1024;
			
			String decimalValue = StringUtils.substringBefore(String.valueOf(formattedValue), ".");
			String decimalPoint = StringUtils.substringAfter(String.valueOf(formattedValue), ".");
			StringBuilder sb = new StringBuilder();
			sb.append(decimalValue);
			if(!StringUtils.isEmpty(decimalPoint) && !StringUtils.equals(decimalPoint, "0")) {
				sb.append(".");
				sb.append(StringUtils.substring(decimalPoint, 0, 2));
			}
			sb.append("Gi");
			
			return sb.toString();
		}
	}
	
	public static BigDecimal formatMemoryWithoutUnit(double value) {
		String data = null;
		if (value == 0)
			data = StringUtils.substringBefore(String.valueOf(value), ".");

		double formattedValue = value / 1024 / 1024;
		if (formattedValue < 1024) {
			// shoud not be called this case
//			data = StringUtils.substringBefore(String.valueOf(formattedValue), ".");
			throw new IllegalArgumentException("The memory value is less than 1Gi(=1024Mi).");
		} else {
			formattedValue = formattedValue / 1024;
			
			String decimalValue = StringUtils.substringBefore(String.valueOf(formattedValue), ".");
			String decimalPoint = StringUtils.substringAfter(String.valueOf(formattedValue), ".");
			StringBuilder sb = new StringBuilder();
			sb.append(decimalValue);
			if(!StringUtils.isEmpty(decimalPoint) && !StringUtils.equals(decimalPoint, "0")) {
				sb.append(".");
				sb.append(StringUtils.substring(decimalPoint, 0, 2));
			}
			
			data = sb.toString();
		}
		
		return new BigDecimal(data);
	}
	
	public static void main(String[] args) {
		BigDecimal b = new BigDecimal(NumberUtils.percentFormat(343, 93429));
		System.err.println("value = " + b);
		
		System.out.println(NumberUtils.formatCpuWithoutUnit(0.31242));
		System.out.println(NumberUtils.formatCpuWithoutUnit(0));
		System.out.println(NumberUtils.formatCpuWithoutUnit(12343.3523523));
	}
}
