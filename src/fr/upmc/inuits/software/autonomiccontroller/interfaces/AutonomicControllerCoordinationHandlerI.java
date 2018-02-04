package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

public interface AutonomicControllerCoordinationHandlerI {
	
	public void	acceptSentDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs) 
			throws Exception;
}
