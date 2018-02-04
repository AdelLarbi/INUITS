package fr.upmc.inuits.software.autonomiccontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerSenderI;

public class AutonomicControllerSenderConnector
	extends AbstractConnector
	implements AutonomicControllerSenderI {

	@Override
	public void sendDataAndNotify(String atcUri, ArrayList<String> availableAVMs) throws Exception {

		((AutonomicControllerSenderI)this.offering).sendDataAndNotify(atcUri, availableAVMs);
	}
}
