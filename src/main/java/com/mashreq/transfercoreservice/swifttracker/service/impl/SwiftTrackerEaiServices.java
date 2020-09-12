package com.mashreq.transfercoreservice.swifttracker.service.impl;
/**
 * @author SURESH PASUPULETI
 */
import com.mashreq.esbcore.bindings.accountservices.mbcdm.accountdetails.EAIServices;

public class SwiftTrackerEaiServices extends EAIServices {

	public SwiftTrackerEaiServices() {
		super();
	}
	
	/**
     * Constructor helper for SOAP webservice is returning Object type
     */
	public SwiftTrackerEaiServices(Object o) {
		super();
		if (!(o instanceof EAIServices)) {
			throw new RuntimeException("Passing wrong object type to SwiftTrackerEaiServices constructor. "
					+ "Must be mbcdm.account.EAIServices");
		}
		EAIServices eaiServices = (EAIServices) o;
		this.body = eaiServices.getBody();
		this.header = eaiServices.getHeader();
	}
}
