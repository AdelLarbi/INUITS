package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;

public class ApplicationNotificationConnector 
extends AbstractConnector
implements ApplicationNotificationI {

	@Override
	public void notifyApplicationAdmission() throws Exception {
		
		((ApplicationNotificationI)this.offering).notifyApplicationAdmission();		
	}
}
