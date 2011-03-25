package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.OfferSide;

public class AverageTrueRange extends AbstractIndicatorBean {
	AverageTrueRange() {
		this.functionName = "ATR";	// must do this
		this.inputTypeArray = null;		// must be null for no appliedprice
		
		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{14};
	}
	
	/**
	 * @param width	number of price bars to used for calculation
	 * @return
	 */
	public AverageTrueRange setWidth(int width) {
		optParams[0] = Integer.valueOf(width);
		return this;
	}
	
	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation
	 * @return
	 */
	public AverageTrueRange setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}

	@Override
	public String toString() {
		return "Average True Range";
	}
}
