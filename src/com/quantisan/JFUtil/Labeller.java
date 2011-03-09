package com.quantisan.JFUtil;

/**
 * Work in progress
 * 
 * @author plam
 *
 */
public class Labeller {
	private static final Labeller INSTANCE = new Labeller();
	private int counter;
	
	private Labeller() {
		counter = 0;
	};
	
	public static int getCounter() {
		return INSTANCE.counter;
	}

	public static void setCounter(int counter) {
		INSTANCE.counter = counter;
	}
	
	public static void incrementCounter() {
		INSTANCE.counter++;
	}

	public static String getLabel() {
		String label;
		
		return label;
	}
}
