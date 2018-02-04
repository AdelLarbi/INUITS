package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import java.util.ArrayList;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerReceiverI
	extends OfferedI, RequiredI {
	
	public void notifyReceivedData(String atcUri, ArrayList<String> availableAVMs) throws Exception;
}
