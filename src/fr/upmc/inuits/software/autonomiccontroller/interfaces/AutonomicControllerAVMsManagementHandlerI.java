package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;

public interface AutonomicControllerAVMsManagementHandlerI {
	
	public void	acceptRequestAddAVM(String appUri, HashMap<Integer,ArrayList<AllocatedCore[]>> allocatedCores) 
			throws Exception;
	
	public void	acceptRequestRemoveAVM(String appUri, String rdUri) throws Exception;
	
	public void	acceptRequestAddCores(String appUri, AllocatedCore[] allocatedCore, int availableAVMsCount)  
			throws Exception;
}
