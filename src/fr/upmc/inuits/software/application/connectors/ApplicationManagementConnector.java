package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementConnector 
	extends AbstractConnector
	implements ApplicationManagementI {

	@Override
	public void doConnectionWithDispatcher(String dispatcherRequestSubmissionInboundPortUri) throws Exception {
	
		((ApplicationManagementI)this.offering).doConnectionWithDispatcher(dispatcherRequestSubmissionInboundPortUri);
	}
}
