package com.quantisan.JFUtil;

import com.dukascopy.api.Instrument;

/**
 * Makes unique order labels
 * 
 * @author Paul Lam
 */
public class LabelMaker {
	private LabelMaker() {}
	
	/**
	 * Returns an unique order label numbered by a timestamp of the last tick
	 * 
	 * @param instrument instrument traded
	 * @return an unique order label with instrument name + timestamp in millisecond
	 */
	public static String getLabel (Instrument instrument) 
	{
		String iname = instrument.toString();
		iname = iname.substring(0, 3) + iname.substring(4, 7);
		String label = iname;

		label += JForexContext.getTime(instrument);
	
		label.toLowerCase();
		return label;
	}
}
