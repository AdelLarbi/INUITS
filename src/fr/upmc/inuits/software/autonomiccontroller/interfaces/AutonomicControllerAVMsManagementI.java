package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerAVMsManagementI 
	extends OfferedI, RequiredI {

	public void doRequestAddAVM(String appUri) throws Exception;
	
	public void doRequestRemoveAVM(String appUri) throws Exception;
}
