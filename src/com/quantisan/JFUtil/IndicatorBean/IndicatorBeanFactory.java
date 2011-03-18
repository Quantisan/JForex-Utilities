package com.quantisan.JFUtil.IndicatorBean;

public class IndicatorBeanFactory {
	private IndicatorBeanFactory() {};
	
	/**
	 * Average True Range (ATR)
	 * 
	 * @return ATR indicator bean
	 */
	public static AverageTrueRange getAverageTrueRange() { return new AverageTrueRange(); }
	
	/**
	 * Moving Average (MA), includes different implementations
	 * 
	 * @return moving average bean
	 */
	public static MovingAverage getMovingAverage() { return new MovingAverage(); }
}
