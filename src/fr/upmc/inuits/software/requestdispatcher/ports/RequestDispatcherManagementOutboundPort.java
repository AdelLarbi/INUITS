package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

public class RequestDispatcherManagementOutboundPort 
	extends AbstractOutboundPort
	implements RequestDispatcherManagementI {

	public RequestDispatcherManagementOutboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementI.class, owner);
			
		assert owner != null;
	}

	public RequestDispatcherManagementOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, RequestDispatcherManagementI.class, owner);

		assert uri != null && owner != null;
	}
	
	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		((RequestDispatcherManagementI)this.connector).
		createRequestSubmissionAndNotificationPorts(requestSubmissionOutboundPortUri, requestNotificationIntboundPortUri);
	}

	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {
		
		((RequestDispatcherManagementI)this.connector).destroyRequestSubmissionAndNotificationPorts();
	}
}
