package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.OfferSide;

public abstract class AbstractIndicatorBean {
	protected static String functionName;
	protected OfferSide[] offerSides;
	protected Integer[] optParams;
	protected AppliedPrice[] inputTypeArray;
	
	protected OfferSide[] getOfferSide() { return offerSides; }

	protected String getFunctionName() { return functionName; }

	protected AppliedPrice[] getInputTypes() { return inputTypeArray; }

	protected Object[] getParams() { return optParams; }
	
	@Override public abstract String toString();
}
