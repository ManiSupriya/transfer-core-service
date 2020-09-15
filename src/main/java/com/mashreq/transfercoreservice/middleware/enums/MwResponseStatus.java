package com.mashreq.transfercoreservice.middleware.enums;

public enum MwResponseStatus {
    S("SUCCESS"), // Success
    F("FAIL"), // Failure
    P("PROCESSING");  // Processin
    
    private final String name;
	
	MwResponseStatus(String name) {
        this.name =  name;
    }
	
	public String getName() {
        return name;
    }
}
