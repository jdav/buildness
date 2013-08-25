package com.davenport.buildness.formula;

import java.util.ArrayList;
import java.util.Collection;


public class Term {
	
	private Double coefficient = 1.0;
	private Collection<Factor> multipliers = new ArrayList<Factor>();
	private Collection<Factor> divisors = new ArrayList<Factor>();
	
	public Double getCoefficient() {
		return coefficient;
	}
	
	public void setCoefficient(Double coefficient) {
		this.coefficient = coefficient;
	}
	
	public Collection<Factor> getMultipliers() {
		return multipliers;
	}
	
	public void setMultipliers(Collection<Factor> multipliers) {
		this.multipliers = multipliers;
	}
	
	public Collection<Factor> getDivisors() {
		return divisors;
	}
	
	public void setDivisors(Collection<Factor> divisors) {
		this.divisors = divisors;
	}
}
