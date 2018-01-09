package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface AutonomicControllerAVMsManagementI 
	extends OfferedI, RequiredI {

	public void doRequestAddAVM(String appUri, ArrayList<AllocatedCore[]> allocatedCores) throws Exception;
	
	public void doRequestRemoveAVM(String appUri) throws Exception;
}
