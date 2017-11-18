package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;

public class ApplicationServicesConnector 
	extends AbstractConnector
	implements ApplicationServicesI {

	@Override
	public void sendRequestForApplicationExecution(int coresToReserve) throws Exception {
		
		((ApplicationServicesI)this.offering).sendRequestForApplicationExecution(coresToReserve);
	}
}
