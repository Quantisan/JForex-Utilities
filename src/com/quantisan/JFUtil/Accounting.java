package com.quantisan.JFUtil;

import java.util.*;

import com.dukascopy.api.*;

/**
 * Accounting class
 *
**/
public class Accounting {
	private IAccount account;
	private IHistory history;
	private IContext context;
	private final Currency ACCOUNTCURRENCY;
	
	private HashMap<Currency, Instrument> pairs = new HashMap<Currency, Instrument>();

	/**
	 * Constructor
	 * 
	@param context context of the strategy
	 *
	 */
	public Accounting(IContext context) {
		this.context = context;
		this.account = context.getAccount();
		this.history = context.getHistory();
		this.ACCOUNTCURRENCY = account.getCurrency();
		initializeCurrencyPairs();
	}
	
	/**
	 * Initialize currency pairs for getting all major counter 
	 * to account currency.
	 * 
	 * Called in constructor.
	 * 	
	 */	
	private void initializeCurrencyPairs() {
		Set<Currency> curSet = new HashSet<Currency>();
		// add all major currencies
		curSet.add(Currency.getInstance("AUD"));
		curSet.add(Currency.getInstance("CAD"));
		curSet.add(Currency.getInstance("CHF"));
		curSet.add(Currency.getInstance("EUR"));
		curSet.add(Currency.getInstance("GBP"));
		curSet.add(Currency.getInstance("JPY"));
		curSet.add(Currency.getInstance("NZD"));
		curSet.add(Currency.getInstance("USD"));
		Instrument instrument;
		for (Currency curr : curSet) {
			if (!curr.equals(this.ACCOUNTCURRENCY)) {
				instrument = getPair(curr, this.ACCOUNTCURRENCY);	
				this.pairs.put(curr, instrument);
			}
		}
	}
	
	/**
	 * Get the Instrument given two Currencies
	 * 
	@param first a currency in a pair
	 *
	@param second the other currency in a pair
	 *
	@return an Instrument with the correct base/counter currencies order
	 *
	**/
	private Instrument getPair(Currency first, Currency second) {
		String pair; 
		pair = first.toString() + Instrument.getPairsSeparator() + 
				second.toString();
		if (Instrument.isInverted(pair))
			pair = second.toString() + Instrument.getPairsSeparator() + 
				first.toString();
		return Instrument.fromString(pair);
	}

	/**
	 * Get the latest bid price of an instrument
	 * 
	@param instrument the instrument to lookup
	 *
	@return latest tick bid price
	 *
	**/
	private double getPrice(Instrument instrument) {
		double price;
		try {
			price = this.history.getLastTick(instrument).getBid();
		}
		catch (JFException ex) {
			price = Double.NaN;
			Logging logger = new Logging(this.context.getConsole());
			logger.printErr("Cannot get price.", ex);			
		}
		return price;
	}
	
	/**
	 * Subscribe to transitional instruments for converting profit/loss
	 * to account currency
	 * 
	@param instSet set of instruments to be traded
	 *
	**/
	public void subscribeTransitionalInstruments(Set<Instrument> instSet) {
		Currency firstCurr, secondCurr;
		Set<Instrument> subscribeSet = new HashSet<Instrument>(this.context.getSubscribedInstruments());
		for (Instrument instrument : instSet) {
			firstCurr = instrument.getPrimaryCurrency();
			secondCurr = instrument.getSecondaryCurrency();		
			if (!firstCurr.equals(this.ACCOUNTCURRENCY) && 
					!secondCurr.equals(this.ACCOUNTCURRENCY))
			{				
				subscribeSet.add(pairs.get(secondCurr));		// transitional pair
			}
		}
		this.context.setSubscribedInstruments(subscribeSet);	
	}
	
	/**
	 * Calculate the risked amount in home currency per unit traded of an instrument
	 * 
	@param instrument the instrument traded
	 *
	@param stopSize	the stop size to use for calculation
	 *
	@return	the risked amount per unit traded with given stop size
	**/
	private double getAccountRiskPerUnit(Instrument instrument, double stopSize) {
		double transitionalPrice;
		Instrument transitionalInstrument;
		double riskInitialCurrency = stopSize;
		
		if (instrument.getSecondaryCurrency().equals(this.ACCOUNTCURRENCY)) {
			// If second currency in the instrument is account currency, 
			// then risk is equal amount difference 
			return riskInitialCurrency;
		} else  if (instrument.getPrimaryCurrency().equals(this.ACCOUNTCURRENCY)) {
			return riskInitialCurrency / getPrice(instrument);
		} else {
			transitionalInstrument = pairs.get(instrument.getSecondaryCurrency());			
			transitionalPrice = getPrice(transitionalInstrument);
			if (transitionalInstrument.getSecondaryCurrency().equals(this.ACCOUNTCURRENCY))
				return riskInitialCurrency * transitionalPrice;
			else				
				return riskInitialCurrency / transitionalPrice;				
		}
	}

	/**
	 * Update account information object, call in onAccount().
	 * 
	**/
	public void update(IAccount account) {
		this.account = account;
	}
	
	/**
	 * Get lot size given size of stop and preferred risk percentage
	 * 
	@param instrument the instrument traded
	 *
	@param stopSize	the stop size to use for calculation
	 *
	@param riskPct	a risk percentage in range of (0, 1]
	 *
	@return	a suggested lot size in millions
	**/
	public double getLot(Instrument instrument, 
			double stopSize, double riskPct) 
	{
		return getPartLot(instrument, stopSize, riskPct, 1);
	}
	
	/**
	 * Get lot size divided by number of parts 
	 * given size of stop and preferred risk percentage.
	 * For use with splitting a position into multiple parts, such that
	 * the total risk is less than the given risk percentage.
	 * 
	@param instrument the instrument traded
	 *
	@param stopSize	the stop size to use for calculation
	 *
	@param riskPct	a risk percentage in range of (0, 1]
	 *
	@param parts	number of parts of the position
	 *
	@return	a suggested partitioned lot size in millions
	**/
	public double getPartLot(Instrument instrument, 
			double stopSize, double riskPct, int parts) 
	{
		double riskAmount, lotSize;
		double equity = this.account.getEquity();
		riskAmount = equity * riskPct;
		lotSize = riskAmount / getAccountRiskPerUnit(instrument, stopSize);
		lotSize /= 1e6;		// in millions for JForex API
		
		lotSize /= parts;
		
		return Rounding.lot(lotSize);
	}
	
}
