package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerAVMsManagementI 
	extends OfferedI, RequiredI {

	public void doRequestAddAVM(String atcUri) throws Exception;
	
	public void doRequestRemoveAVM(String atcUri) throws Exception;
}
