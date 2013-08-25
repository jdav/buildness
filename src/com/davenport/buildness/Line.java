package com.davenport.buildness;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.davenport.buildness.Image.Coordinate;


public class Line extends HashSet<Coordinate> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1003992317735022882L;

	public Set<Coordinate> findEndPoints() {
		return null;
	}
	
	public boolean isClosed() {
		return findEndPoints().isEmpty();
	}
	
	public Line union(Line line) {
		Line union = new Line();
		
		for (Coordinate coordinate : this) {
			union.add(coordinate);
		}
		
		for (Coordinate coordinate : line) {
			union.add(coordinate);
		}
		
		return union;
	}
	
}
