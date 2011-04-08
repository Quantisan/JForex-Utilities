package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.IIndicators.AppliedPrice;

public class FastStochastic extends AbstractIndicatorBean {
	public FastStochastic() {
		this.functionName = "STOCHF";	// must do this
		
		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{5, 3, IIndicators.MaType.SMA.ordinal()};
		this.inputTypeArray = new IIndicators.AppliedPrice[] {AppliedPrice.CLOSE};
	}

	/**
	 * @param width	width of the fast %K line (default: 5)
	 * @return
	 */
	public FastStochastic setFastKWidth(int width) {
		this.optParams[0] = width;
		return this;
	}
	
	/**
	 * @param width	width of the fast %D line (default: 3)
	 * @return
	 */
	public FastStochastic setFastDWidth(int width) {
		this.optParams[1] = width;
		return this;
	}
	
	/**
	 * @param mt	type of moving average use for fast %D line (default: SMA)
	 * @return
	 */
	public FastStochastic setFastDMAType(IIndicators.MaType mt) {
		optParams[2] = mt.ordinal();
		return this;
	}
	
	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation (default: BID)
	 * @return
	 */
	public FastStochastic setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Fast Stochastic Oscillator";
	}

}
