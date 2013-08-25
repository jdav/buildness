package com.davenport.buildness.formula;


public class Factor {
	private Double exponent = 1.0;
	private Unknown unknown = null;
	
	public Double getExponent() {
		return exponent;
	}
	
	public void setExponent(Double exponent) {
		this.exponent = exponent;
	}
	
	public Unknown getUnknown() {
		return unknown;
	}
	
	public void setUnknown(Unknown unknown) {
		this.unknown = unknown;
	}
}
