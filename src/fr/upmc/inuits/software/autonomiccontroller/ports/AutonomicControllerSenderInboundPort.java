package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerSenderHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerSenderI;

public class AutonomicControllerSenderInboundPort
	extends AbstractInboundPort
	implements AutonomicControllerSenderI {
	
	private static final long serialVersionUID = 1L;

	public AutonomicControllerSenderInboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerSenderI.class, owner);
		
		assert owner instanceof AutonomicControllerSenderHandlerI;
	}

	public AutonomicControllerSenderInboundPort(String uri, ComponentI owner ) throws Exception {
		
		super(uri, AutonomicControllerSenderI.class, owner);

		assert uri != null && owner instanceof AutonomicControllerSenderHandlerI;
	}
	
	@Override
	public void sendDataAndNotify(String atcUri, ArrayList<String> availableAVMs) throws Exception {

		final AutonomicControllerSenderHandlerI atcSenderHandlerI = (AutonomicControllerSenderHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atcSenderHandlerI.acceptSentDataAndNotify(atcUri, availableAVMs);
						return null;
					}
				});
	}
}
