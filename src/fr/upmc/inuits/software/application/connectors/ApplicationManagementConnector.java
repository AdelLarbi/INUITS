package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementConnector 
	extends AbstractConnector
	implements ApplicationManagementI {

	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception {
	
		((ApplicationManagementI)this.offering).
			doConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}

	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri)
			throws Exception {
		
		((ApplicationManagementI)this.offering).
			doDynamicConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);		
	}
	
	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doConnectionWithDispatcherForNotification(ropForRequestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}

	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doDynamicConnectionWithDispatcherForNotification(ropForRequestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}	
}
