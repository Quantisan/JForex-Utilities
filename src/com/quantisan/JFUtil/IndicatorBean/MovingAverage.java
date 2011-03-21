package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.IIndicators.AppliedPrice;

public class MovingAverage extends AbstractIndicatorBean {	
	MovingAverage() {
		MovingAverage.functionName = "MA";	// must do this

		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{20, IIndicators.MaType.SMA.ordinal()};
		this.inputTypeArray = new AppliedPrice[] {IIndicators.AppliedPrice.CLOSE};
	}
	
	/**
	 * @param mt	type of moving average implementation to use (default: SMA)
	 * @return
	 */
	public MovingAverage setMAType(IIndicators.MaType mt) {
		optParams[1] = mt.ordinal();
		return this;
	}
	
	/**
	 * @param width	number of price bars to used for calculation (default: 20)
	 * @return
	 */
	public MovingAverage setWidth(int width) {
		optParams[0] = Integer.valueOf(width);
		return this;
	}

	/**
	 * @param ap	which price to use for calculation (default: CLOSE)
	 * @return
	 */
	public MovingAverage setAppliedPrice(AppliedPrice ap) {
		inputTypeArray[0] = ap;
		return this;
	}

	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation (default: BID)
	 * @return
	 */
	public MovingAverage setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}

	@Override
	public String toString() {
		return "Moving Average";
	}
}