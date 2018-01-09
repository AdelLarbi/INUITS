package fr.upmc.inuits.software.autonomiccontroller.interfaces;

public interface AutonomicControllerAVMsManagementHandlerI {
	
	public void	acceptRequestAddAVM(String atcUri) throws Exception;
	
	public void	acceptRequestRemoveAVM(String atcUri) throws Exception;
}
