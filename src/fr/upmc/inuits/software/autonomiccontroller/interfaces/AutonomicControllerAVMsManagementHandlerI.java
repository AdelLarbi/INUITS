package fr.upmc.inuits.software.autonomiccontroller.interfaces;

public interface AutonomicControllerAVMsManagementHandlerI {
	
	public void	acceptRequestAddAVM(String appUri) throws Exception;
	
	public void	acceptRequestRemoveAVM(String appUri) throws Exception;
}
