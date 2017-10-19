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
}
