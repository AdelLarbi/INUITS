package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

public interface AutonomicControllerSenderHandlerI {
	
	public void	acceptSentDataAndNotify(String atcUri, ArrayList<String> availableAVMs) throws Exception;
}
