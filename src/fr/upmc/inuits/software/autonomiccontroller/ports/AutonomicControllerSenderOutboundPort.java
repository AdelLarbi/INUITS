package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerSenderI;

public class AutonomicControllerSenderOutboundPort
	extends AbstractOutboundPort
	implements AutonomicControllerSenderI {

	public AutonomicControllerSenderOutboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerSenderI.class, owner);
	}
	
	public AutonomicControllerSenderOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AutonomicControllerSenderI.class, owner);

		assert uri != null;
	}
	
	@Override
	public void sendDataAndNotify(String atcUri, ArrayList<String> availableAVMs) throws Exception {
		
		((AutonomicControllerSenderI)this.connector).sendDataAndNotify(atcUri, availableAVMs);
	}
}
