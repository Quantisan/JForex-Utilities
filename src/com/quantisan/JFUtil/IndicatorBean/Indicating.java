package com.quantisan.JFUtil.IndicatorBean;

import java.io.File;
import java.util.*;

import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.indicators.IIndicator;
import com.quantisan.JFUtil.JForexContext;
import com.quantisan.JFUtil.Logging;
import com.quantisan.JFUtil.Printer;

/**
 * Accessing technical analysis indicators
 * 
 */
public class Indicating {
	private static final Indicating INSTANCE = new Indicating();
	private Filter filter = Filter.NO_FILTER;
	private Set<String> singleArrayOutputs = new HashSet<String>();
	//private Set<String> doubleArrayOutputs = new HashSet<String>();
	
	private Indicating() {
		singleArrayOutputs.add("ATR");
		singleArrayOutputs.add("ULTOSC");
		singleArrayOutputs.add("RSI");
		singleArrayOutputs.add("MA");
	};
	
	/**
	 * Register a custom indicator function name as outputting a single array. 
	 * Call immediately after {@link Indicating#registerCustomIndicator(File)}
	 * 
	 * @param functionName
	 */
	public static void registerSingleArrayOutputs(String functionName) {
		INSTANCE.singleArrayOutputs.add(functionName);
	}
	
	public static double[] calculate(Instrument instrument, Period period,
			AbstractIndicatorBean indicatorBean, int dataPoints) throws JFException
	{
		// check if this is the function returns a 1-D array
		if (!INSTANCE.singleArrayOutputs.contains(indicatorBean.getFunctionName())) 
		{
			throw new IllegalArgumentException(indicatorBean.getFunctionName() 
											+ " does not return a 1-dimensional array");
		}
		
		IBar bar = JForexContext.getHistory().getBar(instrument, period, OfferSide.BID, 1);
		Object[] objs = JForexContext.getIndicators()
							.calculateIndicator(instrument, 
												period, 
												indicatorBean.getOfferSide(), 
												indicatorBean.getFunctionName(),  
												indicatorBean.getInputTypes(), 
												indicatorBean.getParams(),
												getFilter(),
												dataPoints,
												bar.getTime(),
												0);
		return (double[])objs[0];
	}
	
	public static double calculate(Instrument instrument, Period period,
			AbstractIndicatorBean indicatorBean) throws JFException {
		return calculate(instrument, period, indicatorBean, 1)[0];
	}
	
	/**
	 * Register a custom indicator in the system before using it. Preferably
	 * run this in onStart for all your custom indicators.
	 * 
	@param	file full path to the indicator .jfx file or just the filename
	 * if file is inside directory of {@link IContext#getFilesDir()}
	 *
	 */
	public static void registerCustomIndicator(File file) throws JFException {
		JForexContext.getIndicators().registerCustomIndicator(file);			
	}
	
	public static void registerCustomIndicator(Class<? extends IIndicator> indicatorClass)
		throws JFException
	{
		JForexContext.getIndicators().registerCustomIndicator(indicatorClass);
	}

	/**
	 * Set the type of chart filter to use.
	 * 
	@param	filter	type of chart filter.
	 *
	 */
	public static void setFilter(Filter filter) {
		INSTANCE.filter = filter;
	}

	/**
	 * Get the type of chart filter being used.
	 * 
	@return	type of chart filter
	 *
	 */
	public static Filter getFilter() {
		return INSTANCE.filter;		
	}

	
	@Deprecated private IIndicators indicators;
	@Deprecated private IHistory history;
	@Deprecated private OfferSide offerSide;
	@Deprecated private IContext context;
	
	
	/**
	 * Constructor
	 * 
	@param	context	IContext for accessing JForex API functions
	 *
	 */
	@Deprecated public Indicating(IContext context) {
		this.context = context;
		this.indicators = context.getIndicators();
		this.history = context.getHistory();
		this.offerSide = OfferSide.BID;
		this.filter = Filter.WEEKENDS;
	}

	@Deprecated protected IContext getContext() {
		return context;
	}

	/**
	 * Get the latest indicator results using default parameters
	 * 
	 * 	                          
	@param  instrument the instrument
	 *  
	@param  period the period to calculate the indicator
	 * 
	@param  functionName name of the indicator
	 *
	@param  dataPoints number of data points to get
	 * 

	@return an array of indicator results
	 */
	@Deprecated public double[] getIndicatorResult(Instrument instrument, Period period,
			String functionName, int dataPoints) 
	{
		AppliedPrice[] inputTypeArray;
		OfferSide[] offerSides;
		
		Object[] optParams;	
		
		// set default parameters
		if (functionName.equals("ULTOSC")) {
			offerSides = new OfferSide[] {OfferSide.BID};
			inputTypeArray = null;
			optParams = new Integer[]{7, 14, 28};			
		}
		else if (functionName.equals("ATR")) {
			offerSides = new OfferSide[] {OfferSide.BID};
			inputTypeArray = null;
			optParams = new Integer[]{14};
		}
		else {
			offerSides = new OfferSide[] {OfferSide.BID};
			inputTypeArray = new AppliedPrice[] {IIndicators.AppliedPrice.CLOSE};
			optParams = new Integer[]{20};
		}

		// note for multi value indicator, .e.g with 4 outputs
		// http://www.dukascopy.com/swiss/english/forex/jforex/forum/viewtopic.php?f=5&t=24358
//		double aRsi = ((double[])aArr[0])[0];
//		double aMfi = ((double[])aArr[1])[0];
//		double aStoch = ((double[])aArr[2])[0];
//		double aAvg = ((double[])aArr[3])[0];
		
		return getIndicatorResult(instrument, period, functionName, offerSides, 
				inputTypeArray, optParams, dataPoints);
	}
	
	/**
	 * Get the latest indicator results using custom parameters
	 * 
	 * 	                          
	@param  instrument the instrument
	 *  
	@param  period the period to calculate the indicator
	 * 
	@param  functionName name of the indicator
	 *
	@param	inputTypes	an array of AppliedPrice setting price data
	 *
	@param	params	an array of Objects setting the parameters to the indicator
	 *
	@param  dataPoints number of data points to get
	 * 
	@return an array of indicator results
	 *
	@see IIndicators#calculateIndicator(Instrument, Period, OfferSide[], String, AppliedPrice[], Object[], Filter, int, long, int)
	 */
	@Deprecated public double[] getIndicatorResult(Instrument instrument, Period period, 
			String functionName, OfferSide[] offerSides, 
			AppliedPrice[] inputTypes, Object[] params,	int dataPoints) 
	{
		IBar bar;
		try {
			bar = this.history.getBar(instrument, period, this.offerSide, 1);
		} catch (JFException ex) {		
			Logging.printErr(getContext().getConsole(), "Cannot get bar history", ex);
			return null;
		}
		
		Object[] objs;
		try {
			objs = this.indicators.calculateIndicator(instrument, 
					period, 
					offerSides, 
					functionName,  
					inputTypes, 
					params,
					filter,
					dataPoints,
					bar.getTime(),
					0);
		} catch (JFException ex) {			
			Logging.printErr(getContext().getConsole(), "Cannot calculate indicator", ex);
			return null;
		}
		
		return (double[])objs[0];
	}
	
	/**
	 * Get the most recent completed bar
	 * 
	@param instrument the instrument
	 *
	@param period the period
	 *
	@param offerSide bid or ask bar using OfferSide.BID or OfferSide.ASK
	 *
	@return the last completed bid/ask bar
	 *
	@see IHistory#getBar(Instrument, Period, OfferSide, int)
	 */	
	@Deprecated public IBar getLastBar(Instrument instrument, Period period, OfferSide offerSide) {
		IBar bar;
		
		try {
			bar = this.history.getBar(instrument, period, offerSide, 1);
		} catch (JFException ex) {		
			Logging.printErr(getContext().getConsole(), "Cannot load bar history", ex);		
			return null;
		}
		return bar;
	}
	
	/**
	 * Get the last tick price of an instrument
	 * 
	@param instrument the instrument to lookup
	 *
	@param offerSide bid or ask
	 *
	@return latest tick price
	 *
	**/
	@Deprecated public double getLastPrice(Instrument instrument, OfferSide offerSide) {
		double price;
		try {
			if (offerSide == OfferSide.BID)
				price = this.history.getLastTick(instrument).getBid();
			else
				price = this.history.getLastTick(instrument).getAsk();
		}
		catch (JFException ex) {
			price = Double.NaN;
			Logging.printErr(getContext().getConsole(), "Cannot get price.", ex);			
		}
		return price;
	}
	
	/**
	 * Get the last Tick of an instrument
	 * 
	@param instrument the instrument to lookup
	 *
	@return latest tick
	 *
	@see IHistory#getLastTick(Instrument)
	**/
	@Deprecated public ITick getLastTick(Instrument instrument) throws JFException {
		ITick tick;
		try {
			tick = JForexContext.getHistory().getLastTick(instrument);
		}
		catch (JFException ex) {	
			tick = null;
			Printer.printErr("Cannot get price.", ex);			
		}
		return tick;
	}

	/**
	 * Get the strategy write-accessible JForex files directory
	 * 
	@return directory path
	 *
	@see IContext#getFilesDir()
	 */
	@Deprecated public String getFilesDir() {
		return this.context.getFilesDir().toString();
	}

	/**
	 * Set the offer side used.
	 * 
	@param	ofs OfferSide.BID or OfferSide.ASK
	 *
	 */
	@Deprecated public void setOfferSide(OfferSide ofs) {
		this.offerSide = ofs;
	}

	/**
	 * Get the offer side being used.
	 * 
	@return	offer side, bid or ask
	 *
	 */
	@Deprecated public OfferSide getOfferSide() {
		return offerSide;
	}
}
