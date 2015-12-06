package com.channer;

/*
 * Created By Haoyang
 */

public class FuzzyFunction {
	private double start = 0.0d;
	private double flatStartPoint = 0.0d;
	private double flatEndPoint = 0.0d;
	private double end = 0.0d;
	private double value = 0.0d;
	// True indicates left high right low
	private boolean dir = true;

	public FuzzyFunction(double start, double flatSP, double flatEP, double end, double value) {
		this.start = start;
		this.flatStartPoint = flatSP;
		this.flatEndPoint = flatEP;
		this.end = end;
		this.value = value;
	}

	public FuzzyFunction(double start, double end, double value) {
		this.start = start;
		this.end = end;
		this.value = value;
	}

	public double getFuzzyValue(double input)
	{
		if (input < start || input > end)
			return 0d;
		
		double FuzzyValue = 0.0d;
		if (flatStartPoint == 0.0d && flatEndPoint == 0.0d){
			if (dir)
			FuzzyValue = value*(end -input)/(end-start);
			else
			FuzzyValue = value*(input-start)/(end-start);
		}else{
			if(input<= flatStartPoint)
			{
				FuzzyValue = value*(input-start)/(flatStartPoint-start);
			}else if(input > flatStartPoint && input <= flatEndPoint)
			{
				FuzzyValue = value;
			}else if (input > flatEndPoint)
			{
				FuzzyValue = value*(end-input)/(end-flatEndPoint);
			}
		}
		return FuzzyValue;
	}

	//True: Left High right low, false: Left low right High
	public void setDir(boolean dir)
	{
		this.dir = dir;
	}
}
