package fr.upmc.inuits.software.autonomiccontroller.ports;

import java.util.ArrayList;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.AutonomicController;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;

public class AutonomicControllerManagementInboundPort 
	extends AbstractInboundPort
	implements AutonomicControllerManagementI {

	private static final long serialVersionUID = 1L;
	
	public AutonomicControllerManagementInboundPort(ComponentI owner) throws Exception {
		
		super(AutonomicControllerManagementI.class, owner);

		assert owner != null && owner instanceof AutonomicController;
	}

	public AutonomicControllerManagementInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, AutonomicControllerManagementI.class, owner);

		assert	owner != null && owner instanceof AutonomicController;
	}

	@Override
	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) throws Exception {
		
		final AutonomicController atc = (AutonomicController) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atc.doConnectionWithComputerForServices(computerServicesInboundPortUri);
						return null;
					}
				});
	}

	@Override
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) 
			throws Exception {
		
		final AutonomicController atc = (AutonomicController) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atc.doConnectionWithComputerForStaticState(computerStaticStateInboundPortUri);
						return null;
					}
				});
	}

	@Override
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		final AutonomicController atc = (AutonomicController) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atc.doConnectionWithComputerForDynamicState(computerDynamicStateInboundPortUri, isStartPushing);
						return null;
					}
				});
	}

	@Override
	public void doConnectionWithRequestDispatcherForDynamicState(String requestDispatcherDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		final AutonomicController atc = (AutonomicController) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atc.doConnectionWithRequestDispatcherForDynamicState(requestDispatcherDynamicStateInboundPortUri,
								isStartPushing);
						return null;
					}
				});
	}

	@Override
	public void doConnectionWithAdmissionControllerForAVMsManagement(
			String admissionControllerAtCAVMsManagementInboundPortUri) throws Exception {
		
		final AutonomicController atc = (AutonomicController) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						atc.doConnectionWithAdmissionControllerForAVMsManagement(
								admissionControllerAtCAVMsManagementInboundPortUri);
						return null;
					}
				});
	}
}
