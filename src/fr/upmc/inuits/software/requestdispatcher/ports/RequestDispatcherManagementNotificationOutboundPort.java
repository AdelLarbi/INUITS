package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

public class RequestDispatcherManagementNotificationOutboundPort 
	extends AbstractOutboundPort
	implements RequestDispatcherManagementNotificationI {

	/**
	 * Permet la creation du port RequestDispatcherManagementNotificationOutboundPort. 
	 *
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post aucune postCondition
	 */
	public RequestDispatcherManagementNotificationOutboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementNotificationI.class, owner);
	}
	
	/**
	 * Permet la creation du port RequestDispatcherManagementNotificationOutboundPort. 
	 * @param uri uri du port
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post uri != null
	 */
	public RequestDispatcherManagementNotificationOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, RequestDispatcherManagementNotificationI.class, owner);

		assert uri != null;
	}
	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI#notifyCreateRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {
		
		((RequestDispatcherManagementNotificationI)this.connector).
			notifyCreateRequestSubmissionAndNotificationPorts(appUri, rdUri);
	}
}
