package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;

public class ApplicationServicesOutboundPort 
	extends AbstractOutboundPort
	implements ApplicationServicesI {

	public ApplicationServicesOutboundPort(ComponentI owner) throws Exception {
		super(ApplicationServicesI.class, owner);
			
		assert owner != null;
	}

	public ApplicationServicesOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ApplicationServicesI.class, owner);

		assert uri != null && owner != null;
	}
		
	@Override
	public void sendRequestForApplicationExecution() throws Exception {
		
		((ApplicationServicesI)this.connector).sendRequestForApplicationExecution();		
	}	
}
