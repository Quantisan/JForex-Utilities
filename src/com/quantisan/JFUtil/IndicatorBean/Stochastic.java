package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.OfferSide;

/**
 * Example use to cast the output
 * <pre>
* {@code 
	*Object[] objs = Indicating.calculateMultiDimension(instrument, Period.ONE_MIN, stochBean, 1);
	*double[][] sto = new double[2][];
	*sto[0] = (double[])objs[0];		// %K values
	*sto[1] = (double[])objs[1];		// %D values}
* </pre>
 * @author plam
 *
 */
public class Stochastic extends AbstractIndicatorBean {
	public Stochastic() {
		this.functionName = "STOCH";	// must do this
	
		// setting default parameters
		this.offerSides = new OfferSide[] {OfferSide.BID};
		this.optParams = new Integer[]{5, 3, IIndicators.MaType.SMA.ordinal()
										,3, IIndicators.MaType.SMA.ordinal()};
		this.inputTypeArray = new IIndicators.AppliedPrice[] {AppliedPrice.CLOSE};
	}

	/**
	 * @param width	width of the fast %K line (default: 5)
	 * @return
	 */
	public Stochastic setFastKWidth(int width) {
		this.optParams[0] = width;
		return this;
	}
	
	/**
	 * @param width	width of the slow %K line (default: 3)
	 * @return
	 */
	public Stochastic setSlowKWidth(int width) {
		this.optParams[1] = width;
		return this;
	}
	
	/**
	 * @param mt	type of moving average use for slow %K line (default: SMA)
	 * @return
	 */
	public Stochastic setSlowKMAType(IIndicators.MaType mt) {
		optParams[2] = mt.ordinal();
		return this;
	}
	
	/**
	 * @param width	width of the slow %D line (default: 3)
	 * @return
	 */
	public Stochastic setSlowDWidth(int width) {
		this.optParams[3] = width;
		return this;
	}
	
	/**
	 * @param mt	type of moving average to use for slow %D line (default: SMA)
	 * @return
	 */
	public Stochastic setSlowDMAType(IIndicators.MaType mt) {
		optParams[4] = mt.ordinal();
		return this;
	}
	
	/**
	 * @param os	side (i.e. bid or ask) of price to use for calculation (default: BID)
	 * @return
	 */
	public Stochastic setOfferSide(OfferSide os) {
		offerSides[0] = os;
		return this;
	}
	
	@Override
	public String toString() {
		return "Stochastic";
	}

}
