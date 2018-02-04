package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerCoordinationI
	extends OfferedI, RequiredI {
	
	public void sendDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs) 
			throws Exception;
}
