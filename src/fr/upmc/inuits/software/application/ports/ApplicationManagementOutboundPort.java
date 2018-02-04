package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

/**
 * La classe <code> ApplicationManagementOutboundPort </code> implémente le
 * port sortant à travers lequel on appelle les méthodes de gestion du composant application.
 */
public class ApplicationManagementOutboundPort
	extends AbstractOutboundPort
	implements ApplicationManagementI {

	/**
	 * Permet la creation du port ApplicationManagementOutboundPort. 
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition
	 * post owner != null
	 */
	public ApplicationManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationManagementI.class, owner);
			
		assert owner != null;
	}

	/**
	 * Permet la creation du port ApplicationManagementOutboundPort. 
	 * @param owner composant composant auquel on veut accéder.
	 * @param uri uri du port
	 * @throws Exception
	 * 
	 * pre aucune precondition
	 * 
	 * post uri != null && owner != null;
	 */
	public ApplicationManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationManagementI.class, owner);

		assert uri != null && owner != null;
	}
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception {
		
		((ApplicationManagementI)this.connector).
			doConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri)
			throws Exception {
		
		((ApplicationManagementI)this.connector).
			doDynamicConnectionWithDispatcherForSubmission(dispatcherRequestSubmissionInboundPortUri);
	}

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.connector).
			doConnectionWithDispatcherForNotification(ropForRequestDispatcher, dispatcherRequestNotificationOutboundPortUri);
	}

	/** 
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		((ApplicationManagementI)this.connector).
			doDynamicConnectionWithDispatcherForNotification(ropForRequestDispatcher, dispatcherRequestNotificationOutboundPortUri);
	}
}
