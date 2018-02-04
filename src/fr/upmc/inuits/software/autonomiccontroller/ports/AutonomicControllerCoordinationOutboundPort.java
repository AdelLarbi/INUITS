package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationI;

public class AutonomicControllerCoordinationOutboundPort
	extends AbstractOutboundPort
	implements AutonomicControllerCoordinationI {

	public AutonomicControllerCoordinationOutboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerCoordinationI.class, owner);
	}
	
	public AutonomicControllerCoordinationOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AutonomicControllerCoordinationI.class, owner);

		assert uri != null;
	}

	@Override
	public void sendDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs)
			throws Exception {

		((AutonomicControllerCoordinationI)this.connector).sendDataAndNotify(originSenderUri, thisSenderUri, availableAVMs);
	}
}
