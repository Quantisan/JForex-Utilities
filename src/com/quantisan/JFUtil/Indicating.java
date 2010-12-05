package com.quantisan.JFUtil;

import java.io.File;

import com.dukascopy.api.*;
import com.dukascopy.api.IIndicators.AppliedPrice;

/**
 * Accessing technical analysis indicators
 * 
 */
public class Indicating {
	private IIndicators indicators;
	private IHistory history;
	private OfferSide offerSide;
	private IContext context;
	private Filter filter;
	
	/**
	 * Constructor
	 * 
	@param	context	IContext for accessing JForex API functions
	 *
	 */
	public Indicating(IContext context) {
		this.context = context;
		this.indicators = context.getIndicators();
		this.history = context.getHistory();
		this.offerSide = OfferSide.BID;
		this.filter = Filter.WEEKENDS;
	}

	protected IContext getContext() {
		return context;
	}

	/**
	 * Register a custom indicator in the system
	 * 
	@param	file full path to the indicator .jfx file or just the filename
	 * if file is inside directory of {@link Indicating#getFilesDir()}
	 *
	 */
	public void registerCustomIndicator(File file) {
		try {
			this.indicators.registerCustomIndicator(file);			
		} catch (JFException ex) {
			Logging logger = new Logging(getContext().getConsole());			
			logger.printErr("Cannot register " + file.toString(), ex);
		}
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
	public double[] getIndicatorResult(Instrument instrument, Period period,
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
		// TODO add more common indicators
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
	public double[] getIndicatorResult(Instrument instrument, Period period, 
			String functionName, OfferSide[] offerSides, 
			AppliedPrice[] inputTypes, Object[] params,	int dataPoints) 
	{
		IBar bar;
		try {
			bar = this.history.getBar(instrument, period, this.offerSide, 1);
		} catch (JFException ex) {
			Logging logger = new Logging(getContext().getConsole());			
			logger.printErr("Cannot get bar history", ex);
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
			Logging logger = new Logging(getContext().getConsole());			
			logger.printErr("Cannot calculate indicator", ex);
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
	public IBar getLastBar(Instrument instrument, Period period, OfferSide offerSide) {
		IBar bar;
		
		try {
			bar = this.history.getBar(instrument, period, offerSide, 1);
		} catch (JFException ex) {
			Logging logger = new Logging(getContext().getConsole());			
			logger.printErr("Cannot load bar history", ex);		
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
	@see IHistory#getLastTick(Instrument)
	**/
	public double getLastPrice(Instrument instrument, OfferSide offerSide) {
		double price;
		try {
			if (offerSide == OfferSide.BID)
				price = this.history.getLastTick(instrument).getBid();
			else
				price = this.history.getLastTick(instrument).getAsk();
		}
		catch (JFException ex) {
			price = Double.NaN;
			Logging logger = new Logging(getContext().getConsole());
			logger.printErr("Cannot get price.", ex);			
		}
		return price;
	}

	/**
	 * Get the strategy write-accessible JForex files directory
	 * 
	@return directory path
	 *
	@see IContext#getFilesDir()
	 */
	public String getFilesDir() {
		return this.context.getFilesDir().toString();
	}

	/**
	 * Set the offer side used.
	 * 
	@param	ofs OfferSide.BID or OfferSide.ASK
	 *
	 */
	public void setOfferSide(OfferSide ofs) {
		this.offerSide = ofs;
	}

	/**
	 * Get the offer side being used.
	 * 
	@return	offer side, bid or ask
	 *
	 */
	public OfferSide getOfferSide() {
		return offerSide;
	}

	/**
	 * Set the type of chart filter to use.
	 * 
	@param	filter	type of chart filter.
	 *
	 */
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	/**
	 * Get the type of chart filter being used.
	 * 
	@return	type of chart filter
	 *
	 */
	public Filter getFilter() {
		return filter;		
	}
}
