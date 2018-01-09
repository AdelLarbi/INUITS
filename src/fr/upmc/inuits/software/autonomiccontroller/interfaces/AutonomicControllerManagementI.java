package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

public interface AutonomicControllerManagementI {

	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) 
			throws Exception;	
	
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) 
			throws Exception;	
	
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception;	
	
	public void doConnectionWithRequestDispatcherForDynamicState(String requestDispatcherDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception;	
	
	public void doConnectionWithAdmissionControllerForAVMsManagement(
			String admissionControllerAtCAVMsManagementInboundPortUri) throws Exception;
}
