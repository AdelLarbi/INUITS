package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

public interface AutonomicControllerReceiverHandlerI {
	
	public void	acceptReceivedDataNotification(String atcUri, ArrayList<String> availableAVMs) throws Exception;
}
