package fr.upmc.inuits.software.autonomiccontroller.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;

public class AutonomicControllerAVMsManagementInboundPort
	extends AbstractInboundPort
	implements AutonomicControllerAVMsManagementI {

	private static final long serialVersionUID = 1L;

	public AutonomicControllerAVMsManagementInboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerAVMsManagementI.class, owner);
		
		assert owner instanceof AutonomicControllerAVMsManagementHandlerI;
	}

	public AutonomicControllerAVMsManagementInboundPort(String uri, ComponentI owner ) throws Exception {
		
		super(uri, AutonomicControllerAVMsManagementI.class, owner);

		assert uri != null && owner instanceof AutonomicControllerAVMsManagementHandlerI;
	}
	
	@Override
	public void doRequestAddAVM(String atcUri) throws Exception {
		
		final AutonomicControllerAVMsManagementHandlerI acAVMsManagementHandler = (AutonomicControllerAVMsManagementHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						acAVMsManagementHandler.acceptRequestAddAVM(atcUri);
						return null;
					}
				});
	}

	@Override
	public void doRequestRemoveAVM(String atcUri) throws Exception {

		final AutonomicControllerAVMsManagementHandlerI acAVMsManagementHandler = (AutonomicControllerAVMsManagementHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						acAVMsManagementHandler.acceptRequestRemoveAVM(atcUri);
						return null;
					}
				});
	}
}
