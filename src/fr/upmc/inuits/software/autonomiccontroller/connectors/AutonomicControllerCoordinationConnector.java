package fr.upmc.inuits.software.autonomiccontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationI;

public class AutonomicControllerCoordinationConnector
	extends AbstractConnector
	implements AutonomicControllerCoordinationI {

	@Override
	public void sendDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs)
			throws Exception {
		
		((AutonomicControllerCoordinationI)this.offering).sendDataAndNotify(originSenderUri, thisSenderUri, availableAVMs);
	}
}
