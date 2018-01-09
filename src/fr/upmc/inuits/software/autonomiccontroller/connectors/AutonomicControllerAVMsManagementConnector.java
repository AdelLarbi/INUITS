package fr.upmc.inuits.software.autonomiccontroller.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;

public class AutonomicControllerAVMsManagementConnector
	extends AbstractConnector
	implements AutonomicControllerAVMsManagementI {

	@Override
	public void doRequestAddAVM(String atcUri) throws Exception {
		
		((AutonomicControllerAVMsManagementI)this.offering).doRequestAddAVM(atcUri);
	}

	@Override
	public void doRequestRemoveAVM(String atcUri) throws Exception {

		((AutonomicControllerAVMsManagementI)this.offering).doRequestRemoveAVM(atcUri);
	}
}
