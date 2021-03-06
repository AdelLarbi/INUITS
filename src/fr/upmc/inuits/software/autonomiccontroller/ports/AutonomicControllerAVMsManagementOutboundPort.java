package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;
import java.util.HashMap;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
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
	public void doRequestAddAVM(String appUri, HashMap<Integer,ArrayList<AllocatedCore[]>> allocatedCores) 
			throws Exception {

		if (((AutonomicControllerAVMsManagementI)this.connector) != null) {
			((AutonomicControllerAVMsManagementI)this.connector).doRequestAddAVM(appUri, allocatedCores);	
		} else {
			System.out.println("/!\\ No Client Connected with " + appUri + " for AutonomicControllerAVMsManagementI /!\\");
		}		
	}

	@Override
	public void doRequestRemoveAVM(String appUri, String rdUri) throws Exception {
		
		if (((AutonomicControllerAVMsManagementI)this.connector) != null) {
			((AutonomicControllerAVMsManagementI)this.connector).doRequestRemoveAVM(appUri, rdUri);	
		} else {
			System.out.println("/!\\ No Client Connected with " + appUri + " for AutonomicControllerAVMsManagementI /!\\");
		}
	}

	@Override
	public void doRequestAddCores(String appUri, AllocatedCore[] allocatedCore, int availableAVMsCount)
			throws Exception {
		
		if (((AutonomicControllerAVMsManagementI)this.connector) != null) {
			((AutonomicControllerAVMsManagementI)this.connector).doRequestAddCores(appUri, allocatedCore, availableAVMsCount);	
		} else {
			System.out.println("/!\\ No Client Connected with " + appUri + " for AutonomicControllerAVMsManagementI /!\\");
		}
	}
}
