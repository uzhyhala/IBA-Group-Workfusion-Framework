package com.ibagroup.workfusion.rpa.core.mis;

public enum LoggingPhase {
	ONSTART("onstart"), 
	ONCOMPLETION("oncompletion");

	private String phase;

	LoggingPhase(String phase) {
		this.phase = phase;
	}

	public String getPhase() {
		return phase;
	}
}
