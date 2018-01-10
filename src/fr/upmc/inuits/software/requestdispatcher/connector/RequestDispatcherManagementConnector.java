package fr.upmc.inuits.software.requestdispatcher.connector;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

public class RequestDispatcherManagementConnector
	extends AbstractConnector
	implements RequestDispatcherManagementI {

	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		((RequestDispatcherManagementI)this.offering).
		createRequestSubmissionAndNotificationPorts(requestSubmissionOutboundPortUri, requestNotificationIntboundPortUri);	
	}

	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {

		((RequestDispatcherManagementI)this.offering).destroyRequestSubmissionAndNotificationPorts();
	}
}
