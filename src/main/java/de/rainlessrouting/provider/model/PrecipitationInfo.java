package de.rainlessrouting.provider.model;

public class PrecipitationInfo {
	
	private long[] timeArray;
	
	public PrecipitationInfo(long[] timeArray) {
		this.timeArray = timeArray;
	}

	public long[] getTimeArray() {
		return timeArray;
	}

	public void setTimeArray(long[] timeArray) {
		this.timeArray = timeArray;
	} 
}
