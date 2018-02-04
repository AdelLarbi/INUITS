package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationI;

public class AutonomicControllerCoordinationInboundPort
	extends AbstractInboundPort
	implements AutonomicControllerCoordinationI {
	
	private static final long serialVersionUID = 1L;

	public AutonomicControllerCoordinationInboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerCoordinationI.class, owner);
		
		assert owner instanceof AutonomicControllerCoordinationHandlerI;
	}

	public AutonomicControllerCoordinationInboundPort(String uri, ComponentI owner ) throws Exception {
		
		super(uri, AutonomicControllerCoordinationI.class, owner);

		assert uri != null && owner instanceof AutonomicControllerCoordinationHandlerI;
	}

	@Override
	public void sendDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs)
			throws Exception {

		final AutonomicControllerCoordinationHandlerI atcSenderHandlerI = (AutonomicControllerCoordinationHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atcSenderHandlerI.acceptSentDataAndNotify(originSenderUri, thisSenderUri, availableAVMs);
						return null;
					}
				});
	}
}
