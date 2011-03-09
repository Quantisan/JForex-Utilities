package com.quantisan.JFUtil;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.Instrument;

/**
 * Currency pair utilities
 * 
 * @author plam
 *
 */
public enum Pairer {
	INSTANCE;
	
	private HashMap<Currency, Instrument> pairs = new HashMap<Currency, Instrument>();
	private final Currency ACCOUNTCURRENCY;
	
	private Pairer() {
		ACCOUNTCURRENCY = JForexAccount.getAccountCurrency();
		initializeMajorPairs();
	}
	
	/**
	 * Initialize currency pairs for getting AUD, CAD, CHF, EUR, GBP
	 * JPY, NZD, and USD counters to account currency.
	 * 	
	 */	
	private void initializeMajorPairs() {
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
			if (!curr.equals(ACCOUNTCURRENCY)) {
				instrument = getPair(curr, ACCOUNTCURRENCY);	
				pairs.put(curr, instrument);
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
	private static Instrument getPair(Currency first, Currency second) {
		String pair; 
		pair = first.toString() + Instrument.getPairsSeparator() + 
				second.toString();
		if (Instrument.isInverted(pair))
			pair = second.toString() + Instrument.getPairsSeparator() + 
				first.toString();
		return Instrument.fromString(pair);
	}
	
	/**
	 * Subscribe to transitional instruments for converting profit/loss
	 * to account currency.  
	 * Must be called before use of {@link #convertPipToAccountCurrency(Instrument)}
	 * 
	@param instSet set of instruments to be traded
	 *
	**/
	public static void subscribeTransitionalInstruments(Set<Instrument> instSet) {
		Currency firstCurr, secondCurr;
		Set<Instrument> subscribeSet = 
			new HashSet<Instrument>(JForexContext.getContext().getSubscribedInstruments());
		
		for (Instrument instrument : instSet) {
			firstCurr = instrument.getPrimaryCurrency();
			secondCurr = instrument.getSecondaryCurrency();		
			if (!firstCurr.equals(INSTANCE.ACCOUNTCURRENCY) && 
					!secondCurr.equals(INSTANCE.ACCOUNTCURRENCY))
			{				
				// TODO dynamically build pairs list according to instSet
				subscribeSet.add(INSTANCE.pairs.get(secondCurr));		// transitional pair
			}
		}
		JForexContext.getContext().setSubscribedInstruments(subscribeSet);	
	}
	
	/**
	 * Calculate the equivalent amount in account currency for each +1 pip on
	 * a 1,000 position size of an instrument
	 * 
	@param instrument the instrument traded
	 *
	@return	the equivalent account currency amount for each +1 pip movement of instrument with a 1,000 position size
	**/
	public static double convertPipToAccountCurrency(Instrument instrument) {	 
		double onePipInitial = instrument.getPipValue();
		double output;
		
		if (instrument.getSecondaryCurrency().equals(INSTANCE.ACCOUNTCURRENCY)) {
			// If second currency in the instrument is account currency, 
			// then risk is equal amount difference 
			output = onePipInitial;
		} else  if (instrument.getPrimaryCurrency().equals(INSTANCE.ACCOUNTCURRENCY)) {
			output = onePipInitial / JForexContext.getPrice(instrument);
		} else {
			Instrument transitionalInstrument = INSTANCE.pairs.get(instrument.getSecondaryCurrency());			
			double transitionalPrice = JForexContext.getPrice(transitionalInstrument);
			if (transitionalInstrument.getSecondaryCurrency().equals(INSTANCE.ACCOUNTCURRENCY))
				output = onePipInitial * transitionalPrice;
			else				
				output = onePipInitial / transitionalPrice;				
		}
		
		return output * 1000;		// assume 1,000 units traded
	}

}
