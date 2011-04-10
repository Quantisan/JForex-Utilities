package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.IIndicators.AppliedPrice;

/**
 * Example use to cast the output
 * <pre>
* {@code 
	*double[][] macd = new double[3][];
	*macd[0] = (double[])objs[0];		// macd values
	*macd[1] = (double[])objs[1];		// signal values
	*macd[2] = (double[])objs[2];		// histogram values}
* </pre>
 * @author plam
 *
 */
public class MovingAverageConvergenceDivergence extends AbstractIndicatorBean {
	// [0] = MACD, [1] = signal, [2] = Macd Hist data
	
	public MovingAverageConvergenceDivergence() {

		this.functionName = "MACD";	// must do this
	
		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{12, 26, 9};
		this.inputTypeArray = new IIndicators.AppliedPrice[] {AppliedPrice.CLOSE};
	}
	
	/**
	 * @param width	width of the fast line (default: 12)
	 * @return
	 */
	public MovingAverageConvergenceDivergence setFastWidth(int width) {
		this.optParams[0] = width;
		return this;
	}
	
	/**
	 * @param width	width of the slow line (default: 26)
	 * @return
	 */
	public MovingAverageConvergenceDivergence setSlowWidth(int width) {
		this.optParams[1] = width;
		return this;
	}
	
	/**
	 * @param ap	which price to use for calculation (default: CLOSE)
	 * @return
	 */
	public MovingAverageConvergenceDivergence setAppliedPrice(AppliedPrice ap) {
		inputTypeArray[0] = ap;
		return this;
	}
	
	/**
	 * @param width	width of the signal line (default: 9)
	 * @return
	 */
	public MovingAverageConvergenceDivergence setSignalWidth(int width) {
		this.optParams[2] = width;
		return this;
	}
	
	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation (default: BID)
	 * @return
	 */
	public MovingAverageConvergenceDivergence setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}
	
	@Override
	public String toString() {
		return "Moving Average Convergence-Divergence";
	}

}
