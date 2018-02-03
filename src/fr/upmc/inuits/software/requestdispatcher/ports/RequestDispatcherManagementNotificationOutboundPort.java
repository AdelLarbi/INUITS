package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

public class RequestDispatcherManagementNotificationOutboundPort 
	extends AbstractOutboundPort
	implements RequestDispatcherManagementNotificationI {

	public RequestDispatcherManagementNotificationOutboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementNotificationI.class, owner);
	}
	
	public RequestDispatcherManagementNotificationOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, RequestDispatcherManagementNotificationI.class, owner);

		assert uri != null;
	}
	
	@Override
	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {
		
		((RequestDispatcherManagementNotificationI)this.connector).
			notifyCreateRequestSubmissionAndNotificationPorts(appUri, rdUri);
	}
}
