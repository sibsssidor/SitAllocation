package org.pdg.sitallocation;

public class Solution {
	
	private final double[] variables;
	private final double score;
	
	public double[] getVariables() {
		return variables;
	}

	public double getScore() {
		return score;
	}

	public Solution(double[] variables, double score) {
		super();
		this.variables = variables;
		this.score = score;
	}

}
