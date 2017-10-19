package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementOutboundPort 
	extends AbstractOutboundPort
	implements ApplicationManagementI {

	public ApplicationManagementOutboundPort(ComponentI owner) throws Exception {
		super(ApplicationManagementI.class, owner);
			
		assert owner != null;
	}

	public ApplicationManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ApplicationManagementI.class, owner);

		assert uri != null && owner != null;
	}
		
	@Override
	public void sendRequestForApplicationExecution() throws Exception {
		
		((ApplicationManagementI)this.connector).sendRequestForApplicationExecution();		
	}
}
