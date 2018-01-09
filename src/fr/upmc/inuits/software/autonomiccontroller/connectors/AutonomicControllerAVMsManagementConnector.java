package fr.upmc.inuits.software.autonomiccontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;

public class AutonomicControllerAVMsManagementConnector
	extends AbstractConnector
	implements AutonomicControllerAVMsManagementI {

	@Override
	public void doRequestAddAVM(String appUri) throws Exception {
		
		((AutonomicControllerAVMsManagementI)this.offering).doRequestAddAVM(appUri);
	}

	@Override
	public void doRequestRemoveAVM(String appUri) throws Exception {

		((AutonomicControllerAVMsManagementI)this.offering).doRequestRemoveAVM(appUri);
	}
}
