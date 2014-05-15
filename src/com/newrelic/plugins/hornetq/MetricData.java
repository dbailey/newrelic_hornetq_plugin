package com.newrelic.plugins.hornetq;

public class MetricData {
	public String name;
	public long metricValue;
	public long metricCount;
	public long minValue;
	public long maxValue;
	public long sumOfSquares;
	
	public MetricData(String name, long metricValue, long metricCount, long minValue, long maxValue, long sumOfSquares) {
		this.name = name;
		this.metricValue = metricValue;
		this.metricCount = metricCount;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.sumOfSquares = sumOfSquares;
	}

}
