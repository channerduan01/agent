package com.channer;

public class UcsLevelCost {
	private double level;
	private double cost;
	
	// constructors
	public UcsLevelCost(double level, double cost) {
		this.level = level;
		this.cost = cost ;
	}

	public double getLevel() {
		return level;
	}

	public void setLevel(double level) {
		this.level = level;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}
	
	public String toString() {
		return String.valueOf(level) + " " + String.valueOf(cost);
		
	}
}
