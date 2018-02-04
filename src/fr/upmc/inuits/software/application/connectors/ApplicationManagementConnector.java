package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

/**
 * La classe <code>ApplicationManagementConnector</code> represente un
 * connecteur qui permet a l'application de pouvoir établir des connection entre le Composant RequestDispatcher et le composant requestGenerator
 */
public class ApplicationManagementConnector 
	extends AbstractConnector
	implements ApplicationManagementI {

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception {
	
		((ApplicationManagementI)this.offering).
			doConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri)
			throws Exception {
		
		((ApplicationManagementI)this.offering).
			doDynamicConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);		
	}
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doConnectionWithDispatcherForNotification(ropForRequestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.offering).doDynamicConnectionWithDispatcherForNotification(ropForRequestDispatcher, 
				dispatcherRequestNotificationOutboundPortUri);
	}	
}
