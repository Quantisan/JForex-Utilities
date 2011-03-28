package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.OfferSide;

public class UltimateOscillator extends AbstractIndicatorBean {
	UltimateOscillator() {
		this.functionName = "ULTOSC";
		
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{7, 14, 28};
		this.inputTypeArray = null;
	}
	
	/**
	 * Set number of price bars to used for calculation
	 * 
	 * @param firstWidth (default: 7)
	 * @param secondWidth (default: 14)
	 * @param thirdWidth (default: 28)
	 * @return
	 */
	public UltimateOscillator setWidths(int firstWidth, int secondWidth, int thirdWidth) {
		optParams[0] = Integer.valueOf(firstWidth);
		optParams[1] = Integer.valueOf(secondWidth);
		optParams[2] = Integer.valueOf(thirdWidth);
		return this;
	}

	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation (default: BID)
	 * @return
	 */
	public UltimateOscillator setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}
	
	@Override
	public String toString() {
		return "Ultimate Oscillator";
	}
}
