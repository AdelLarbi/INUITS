package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerReceiverI;

public class AutonomicControllerReceiverOutboundPort
	extends AbstractOutboundPort
	implements AutonomicControllerReceiverI {

	public AutonomicControllerReceiverOutboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerReceiverI.class, owner);
	}
	
	public AutonomicControllerReceiverOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, AutonomicControllerReceiverI.class, owner);

		assert uri != null;
	}
	
	@Override
	public void notifyReceivedData(String atcUri, ArrayList<String> availableAVMs) throws Exception {

		((AutonomicControllerReceiverI)this.connector).notifyReceivedData(atcUri, availableAVMs);
	}
}
