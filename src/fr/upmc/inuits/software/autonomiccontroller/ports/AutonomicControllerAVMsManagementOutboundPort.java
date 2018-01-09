package fr.upmc.inuits.software.autonomiccontroller.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;

public class AutonomicControllerAVMsManagementOutboundPort
	extends AbstractOutboundPort	
	implements AutonomicControllerAVMsManagementI {

	public AutonomicControllerAVMsManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerAVMsManagementI.class, owner);
	}
	
	public AutonomicControllerAVMsManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AutonomicControllerAVMsManagementI.class, owner);

		assert uri != null;
	}
	
	@Override
	public void doRequestAddAVM(String appUri) throws Exception {

		if (((AutonomicControllerAVMsManagementI)this.connector) != null) {
			((AutonomicControllerAVMsManagementI)this.connector).doRequestAddAVM(appUri);	
		} else {
			System.out.println("/!\\ No Client Connected with " + appUri + " for AutonomicControllerAVMsManagementI /!\\");
		}		
	}

	@Override
	public void doRequestRemoveAVM(String appUri) throws Exception {
		
		if (((AutonomicControllerAVMsManagementI)this.connector) != null) {
			((AutonomicControllerAVMsManagementI)this.connector).doRequestRemoveAVM(appUri);	
		} else {
			System.out.println("/!\\ No Client Connected with " + appUri + " for AutonomicControllerAVMsManagementI /!\\");
		}
	}
}
