package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.IIndicators.AppliedPrice;

/**
 * Example use to cast the output
 * <pre>
* {@code 
	*Object[] objs = Indicating.calculateMultiDimension(instrument, Period.ONE_MIN, srBean, 1);
	*double[][] sto = new double[2][];
	*sto[0] = (double[])objs[0];		// Fast %K values
	*sto[1] = (double[])objs[1];		// Fast %D values}
* </pre>
 * @author plam
 *
 */
public class StochasticRelativeStrengthIndex extends AbstractIndicatorBean {
	public StochasticRelativeStrengthIndex() {
		this.functionName = "STOCHRSI";	// must do this

		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{14, 5, 3, IIndicators.MaType.SMA.ordinal()};
		this.inputTypeArray = new AppliedPrice[] {IIndicators.AppliedPrice.CLOSE};
	}
	
	/**
	 * @param width	width of RSI (default: 14)
	 * @return
	 */
	public StochasticRelativeStrengthIndex setRSIWidth(int width) {
		this.optParams[0] = width;
		return this;
	}
	
	/**
	 * @param width	width of the fast %K line (default: 5)
	 * @return
	 */
	public StochasticRelativeStrengthIndex setFastKWidth(int width) {
		this.optParams[1] = width;
		return this;
	}
	
	/**
	 * @param width	width of the fast %D line (default: 3)
	 * @return
	 */
	public StochasticRelativeStrengthIndex setFastDWidth(int width) {
		this.optParams[2] = width;
		return this;
	}
	
	/**
	 * @param mt	type of moving average to use for slow %D line (default: SMA)
	 * @return
	 */
	public StochasticRelativeStrengthIndex setSlowDMAType(IIndicators.MaType mt) {
		optParams[3] = mt.ordinal();
		return this;
	}
	
	@Override
	public String toString() {
		return "Stochastic-Relative Strength Index";
	}
}