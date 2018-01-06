package fr.upmc.inuits.software.autonomiccontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;

public class AutonomicControllerManagementConnector
	extends AbstractConnector
	implements AutonomicControllerManagementI {

	@Override
	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) throws Exception {
		
		((AutonomicControllerManagementI)this.offering).
			doConnectionWithComputerForServices(computerServicesInboundPortUri);		
	}

	@Override
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) 
			throws Exception {
		
		((AutonomicControllerManagementI)this.offering).
			doConnectionWithComputerForStaticState(computerStaticStateInboundPortUri);
	}

	@Override
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
	
		((AutonomicControllerManagementI)this.offering).
			doConnectionWithComputerForDynamicState(computerDynamicStateInboundPortUri, isStartPushing);
	}

	@Override
	public void doConnectionWithRequestDispatcherForDynamicState(String requestDispatcherDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		((AutonomicControllerManagementI)this.offering).
			doConnectionWithRequestDispatcherForDynamicState(requestDispatcherDynamicStateInboundPortUri, isStartPushing);		
	}
}
