package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

public class RequestDispatcherManagementOutboundPort 
	extends AbstractOutboundPort
	implements RequestDispatcherManagementI {

	/**
	 * Contructeur creant le port de type RequestDispatcherManagementOutboundPort
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition
	 * post owner != null
	 */
	public RequestDispatcherManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementI.class, owner);
			
		assert owner != null;
	}

	/**
	 * Contructeur creant le port de type RequestDispatcherManagementOutboundPort
	 * @param uri uri du port
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition
	 * post uri != null && owner != null
	 */
	public RequestDispatcherManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, RequestDispatcherManagementI.class, owner);

		assert uri != null && owner != null;
	}
	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#createRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		((RequestDispatcherManagementI)this.connector).
		createRequestSubmissionAndNotificationPorts(requestSubmissionOutboundPortUri, requestNotificationIntboundPortUri);
	}

	/**
	 *  @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#destroyRequestSubmissionAndNotificationPorts()
	 */
	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {
		
		((RequestDispatcherManagementI)this.connector).destroyRequestSubmissionAndNotificationPorts();
	}
}
