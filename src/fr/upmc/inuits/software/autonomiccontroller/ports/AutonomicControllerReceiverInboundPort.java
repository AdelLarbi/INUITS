package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerReceiverHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerReceiverI;

public class AutonomicControllerReceiverInboundPort
	extends	AbstractInboundPort
	implements AutonomicControllerReceiverI {

	private static final long serialVersionUID = 1L;

	public AutonomicControllerReceiverInboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerReceiverI.class, owner);

		assert owner instanceof AutonomicControllerReceiverHandlerI;		
	}
	
	public AutonomicControllerReceiverInboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, AutonomicControllerReceiverI.class, owner);

		assert uri != null && owner instanceof AutonomicControllerReceiverHandlerI;
	}
	
	@Override
	public void notifyReceivedData(String atcUri, ArrayList<String> availableAVMs) throws Exception {

		final AutonomicControllerReceiverHandlerI atcReceiverHandlerI = (AutonomicControllerReceiverHandlerI) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atcReceiverHandlerI.acceptReceivedDataNotification(atcUri, availableAVMs);
						return null;
					}
				});		
	}
}
