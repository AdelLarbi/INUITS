package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationHandlerI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

public class RequestDispatcherManagementNotificationInboundPort
	extends	AbstractInboundPort
	implements RequestDispatcherManagementNotificationI {

	private static final long serialVersionUID = 1L;

	public RequestDispatcherManagementNotificationInboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementNotificationI.class, owner);

		assert owner instanceof RequestDispatcherManagementNotificationHandlerI;		
	}
	
	public RequestDispatcherManagementNotificationInboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, RequestDispatcherManagementNotificationI.class, owner);

		assert uri != null && owner instanceof RequestDispatcherManagementNotificationHandlerI;
	}
	
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
