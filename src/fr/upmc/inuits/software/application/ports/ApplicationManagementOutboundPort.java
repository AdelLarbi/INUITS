package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementOutboundPort
	extends AbstractOutboundPort
	implements ApplicationManagementI {

	public ApplicationManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationManagementI.class, owner);
			
		assert owner != null;
	}

	public ApplicationManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationManagementI.class, owner);

		assert uri != null && owner != null;
	}
	
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception {
		
		((ApplicationManagementI)this.connector).
			doConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}
	
	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri)
			throws Exception {
		
		((ApplicationManagementI)this.connector).
			doDynamicConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}

	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.connector).
			doConnectionWithDispatcherForNotification(ropForRequestDispatcher, dispatcherRequestNotificationOutboundPortUri);
	}

	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.connector).
			doDynamicConnectionWithDispatcherForNotification(ropForRequestDispatcher, dispatcherRequestNotificationOutboundPortUri);
	}
}
