package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface AutonomicControllerAVMsManagementHandlerI {
	
	public void	acceptRequestAddAVM(String appUri, ArrayList<AllocatedCore[]> allocatedCores) throws Exception;
	
	public void	acceptRequestRemoveAVM(String appUri, String rdUri) throws Exception;
}
