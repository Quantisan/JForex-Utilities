package com.quantisan.JFUtil;

import com.dukascopy.api.Instrument;

/**
 * Methods for rounding numbers for JForex compliance
 * 
 */
public final class Rounding {
	/**
	 * Rounding a pip value to 1/10 of a pip
	 *                      
	@param  instrument the instrument
	 *  
	@param  value	a pip value
	 * 	                       
	@return a pip value rounded to 0.1 pips
	 */
	public static double pip(Instrument instrument, double value) {
		int scale = instrument.getPipScale();
		value = Math.round(value * java.lang.Math.pow(10, scale + 1));		
		value /= java.lang.Math.pow(10, scale + 1);
		return value;
	}
	
	/**
	 * Rounding by floor a double number to the thousands
	 *                         
	@param  lot lot size in
	 *  
	@return a floored number to the thousands
	 */
	public static double lot(double lot)
	{	
		lot = (int)(lot * 1000) / (1000d);		// 1000 units mininum
		return lot;
	}
}
