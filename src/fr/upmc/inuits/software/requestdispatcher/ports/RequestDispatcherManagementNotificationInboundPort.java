package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationHandlerI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

public class RequestDispatcherManagementNotificationInboundPort
	extends	AbstractInboundPort
	implements RequestDispatcherManagementNotificationI {

	private static final long serialVersionUID = 1L;

	/**
	 * Permet la creation du port RequestDispatcherManagementNotificationInboundPort. 
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post owner instanceof RequestDispatcherManagementNotificationHandlerI
	 */
	public RequestDispatcherManagementNotificationInboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementNotificationI.class, owner);

		assert owner instanceof RequestDispatcherManagementNotificationHandlerI;		
	}
	
	/**
	 * Permet la creation du port RequestDispatcherManagementNotificationInboundPort. 
	 * @param uri uri du port
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post uri != null && owner instanceof RequestDispatcherManagementNotificationHandlerI
	 */
	public RequestDispatcherManagementNotificationInboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, RequestDispatcherManagementNotificationI.class, owner);

		assert uri != null && owner instanceof RequestDispatcherManagementNotificationHandlerI;
	}
	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI#notifyCreateRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {
		
		final RequestDispatcherManagementNotificationHandlerI rdManagementNotificationHandlerI = 
				(RequestDispatcherManagementNotificationHandlerI) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rdManagementNotificationHandlerI.acceptCreateRequestSubmissionAndNotificationPorts(appUri, rdUri);
						return null;
					}
				});
	}
}
