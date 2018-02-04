package fr.upmc.inuits.software.autonomiccontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerReceiverI;

public class AutonomicControllerReceiverConnector
	extends AbstractConnector
	implements AutonomicControllerReceiverI {

	@Override
	public void notifyReceivedData(String atcUri, ArrayList<String> availableAVMs) throws Exception {
		
		((AutonomicControllerReceiverI)this.offering).notifyReceivedData(atcUri, availableAVMs);
	}
}
