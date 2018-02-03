package fr.upmc.inuits.software.requestdispatcher.connector;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

public class RequestDispatcherManagementNotificationConnector
	extends AbstractConnector
	implements RequestDispatcherManagementNotificationI {

	@Override
	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {

		((RequestDispatcherManagementNotificationI)this.offering).
			notifyCreateRequestSubmissionAndNotificationPorts(appUri, rdUri);
	}
}
