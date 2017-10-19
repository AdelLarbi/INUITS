package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;

public class ApplicationNotificationOutboundPort 
extends AbstractOutboundPort
implements ApplicationNotificationI {

	public ApplicationNotificationOutboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationNotificationI.class, owner);
	}
	
	public ApplicationNotificationOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, ApplicationNotificationI.class, owner);

		assert uri != null;
	}
	
	@Override
	public void notifyApplicationAdmission() throws Exception {
	
		((ApplicationNotificationI)this.connector).notifyApplicationAdmission();		
	}
}
