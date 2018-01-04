package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;

public class AutonomicControllerManagementOutboundPort
	extends AbstractOutboundPort
	implements AutonomicControllerManagementI {

	public AutonomicControllerManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerManagementI.class, owner);
			
		assert owner != null;
	}

	public AutonomicControllerManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AutonomicControllerManagementI.class, owner);

		assert uri != null && owner != null;
	}
	
	@Override
	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) throws Exception {
		
		((AutonomicControllerManagementI)this.connector).
			doConnectionWithComputerForServices(computerServicesInboundPortUri);
		
	}

	@Override
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) throws Exception {
		
		((AutonomicControllerManagementI)this.connector).
			doConnectionWithComputerForStaticState(computerStaticStateInboundPortUri);
	}

	@Override
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		((AutonomicControllerManagementI)this.connector).
			doConnectionWithComputerForDynamicState(computerDynamicStateInboundPortUri, isStartPushing);	
	}
}
