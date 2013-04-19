package com.nisovin.brucesgym;

public enum StatisticType {

	XP("xp"),
	TOTAL("tot"),
	MAX("max"),
	MIN("min");
	
	private String code;
	
	StatisticType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
}
