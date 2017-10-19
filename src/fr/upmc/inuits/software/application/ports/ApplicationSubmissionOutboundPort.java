package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;

public class ApplicationSubmissionOutboundPort
	extends AbstractOutboundPort
	implements ApplicationSubmissionI {

	public ApplicationSubmissionOutboundPort(ComponentI owner) throws Exception {
	
		super(ApplicationSubmissionI.class, owner);
	}
	
	public ApplicationSubmissionOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationSubmissionI.class, owner);

		assert uri != null;
	}

	@Override
	public void submitApplicationAndNotify() throws Exception {
		
		((ApplicationSubmissionI)this.connector).submitApplicationAndNotify();		
	}
}
