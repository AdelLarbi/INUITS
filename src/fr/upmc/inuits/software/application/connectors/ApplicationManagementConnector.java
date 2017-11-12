package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

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
	public void doConnectionWithDispatcherForNotification(RequestDispatcher requestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doConnectionWithDispatcherForNotification(requestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}

	@Override
	public void doDynamicConnectionWithDispatcherForNotification(RequestDispatcher requestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doDynamicConnectionWithDispatcherForNotification(requestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}	
}
