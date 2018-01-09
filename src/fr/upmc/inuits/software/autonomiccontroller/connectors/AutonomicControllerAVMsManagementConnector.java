package fr.upmc.inuits.software.autonomiccontroller.connectors;

import java.util.ArrayList;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;

public class AutonomicControllerAVMsManagementConnector
	extends AbstractConnector
	implements AutonomicControllerAVMsManagementI {

	@Override
	public void doRequestAddAVM(String appUri, ArrayList<AllocatedCore[]> allocatedCores) throws Exception {
		
		((AutonomicControllerAVMsManagementI)this.offering).doRequestAddAVM(appUri, allocatedCores);
	}

	@Override
	public void doRequestRemoveAVM(String appUri) throws Exception {

		((AutonomicControllerAVMsManagementI)this.offering).doRequestRemoveAVM(appUri);
	}
}
