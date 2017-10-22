package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementConnector 
	extends AbstractConnector
	implements ApplicationManagementI {

	@Override
	public void sendRequestForApplicationExecution() throws Exception {
		
		((ApplicationManagementI)this.offering).sendRequestForApplicationExecution();
	}

	@Override
	public void doConnectionWithDispatcher(String dispatcherRequestSubmissionInboundPortUri) throws Exception {
		System.out.println("BEGIN -> Connector");
		((ApplicationManagementI)this.offering).doConnectionWithDispatcher(dispatcherRequestSubmissionInboundPortUri);
		System.out.println("END -> Connector");
	}
}
