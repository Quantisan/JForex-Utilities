package com.quantisan.JFUtil;

import java.util.Calendar;
import java.util.TimeZone;

import com.dukascopy.api.*;

/**
 * Provides singleton access to JForex context objects
 *
 */
public enum JForexContext {
	INSTANCE;
	
	private IContext context;
	private IEngine engine;
	private IConsole console;
	private IHistory history;
	private IIndicators indicators;
	
	/**
	 * Set JForex context objects.  Must initialize before use of strategy.
	 * 
	 * @param context
	 */
	public static void setContext(IContext context) {
		INSTANCE.context = context;
		INSTANCE.engine = context.getEngine();
		INSTANCE.console = context.getConsole();
		INSTANCE.history = context.getHistory();		
		INSTANCE.indicators = context.getIndicators();
	}

	/**
	 * @return the context
	 */
	public static IContext getContext() {
		return INSTANCE.context;
	}

	/**
	 * @return the engine
	 */
	public static IEngine getEngine() {
		return INSTANCE.engine;
	}

	/**
	 * @return the console
	 */
	public static IConsole getConsole() {
		return INSTANCE.console;
	}

	/**
	 * @return the history
	 */
	public static IHistory getHistory() {
		return INSTANCE.history;
	}

	/**
	 * Access to JForex IIndicators
	 * 
	 * @return the indicators
	 */
	public static IIndicators getIndicators() {
		return INSTANCE.indicators;
	}
	
	/**
	 * Get the latest bid price of an instrument
	 * 
	@param instrument the instrument to lookup
	 *
	@return latest tick bid price
	 *
	**/
	public static double getPrice(Instrument instrument) {
		double price;
		try {
			price = getHistory().getLastTick(instrument).getBid();
		} catch (JFException ex) {
			price = Double.NaN;
			Printer.printErr("Cannot get price.", ex);			
		}
		return price;
	}
	
	/**
	 * @param instrument
	 * @return time of last tick if available; if not, time of system in GMT
	 */
	public static long getTime(Instrument instrument) {
		try {
			return getHistory().getTimeOfLastTick(instrument);
		} catch (JFException ex) {
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			return calendar.getTimeInMillis();
		}
	}
}
