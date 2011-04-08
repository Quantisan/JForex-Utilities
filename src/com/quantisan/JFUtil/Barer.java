package com.quantisan.JFUtil;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IIndicators.AppliedPrice;

/**
 * Bar data manipulation utility functions
 * 
 * @author Paul Lam
 *
 */
public class Barer {
	private Barer() {};
	
	/**
	 * Calculates the AppliedPrice
	 * @param bar the bar to calculate
	 * @param ap HIGH, LOW, MEDIAN_PRICE, TYPICAL_PRICE, CLOSE, OPEN, or WEIGHTED_CLOSE
	 * @return	the corresponding AppliedPrice, except TIMESTAMP and VOLUME, 
	 * 			which will return CLOSE instead
	 */
	public static double calcAppliedPrice(IBar bar, AppliedPrice ap) {
		double price;
		switch (ap) {
			case HIGH: 	price = bar.getHigh();	break;
			case LOW: 	price = bar.getLow();		break;
			case MEDIAN_PRICE: 
				price = (bar.getLow() + bar.getHigh()) / 2d;		
				break;
			case TYPICAL_PRICE:
				price = (bar.getLow() + bar.getHigh() + bar.getClose()) / 3d;		
				break;
			case CLOSE:	price = bar.getClose();		break;
			case OPEN:	price = bar.getOpen();		break;
			case WEIGHTED_CLOSE:
				price = (bar.getLow() + bar.getHigh() + (bar.getClose() * 2d)) / 4d;		
				break;	
			default: price = bar.getClose();	break;
		}
		return price;
	}
}
