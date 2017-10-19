package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ApplicationManagementI 
	extends OfferedI, RequiredI {
		
	public void sendRequestForApplicationExecution() throws Exception;
}
