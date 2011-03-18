package com.quantisan.JFUtil.IndicatorBean;

import com.dukascopy.api.IIndicators.AppliedPrice;
import com.dukascopy.api.OfferSide;

public abstract class AbstractIndicatorBean {
	String functionName;
	OfferSide[] offerSides;
	Integer[] optParams;
	AppliedPrice[] inputTypeArray;
	
	protected OfferSide[] getOfferSide() { return offerSides; }

	protected String getFunctionName() { return functionName; }

	protected AppliedPrice[] getInputTypes() { return inputTypeArray; }

	protected Object[] getParams() { return optParams; }
}
