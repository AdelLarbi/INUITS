package fr.upmc.inuits.software.requestdispatcher.connector;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;

/**
 * 
 * La classe <code>RequestDispatcherManagementNotificationConnector</code> implemente un
 * connecteur pour la gestion de la notification de requêtes.
 *
 */
public class RequestDispatcherManagementNotificationConnector
	extends AbstractConnector
	implements RequestDispatcherManagementNotificationI {

	/**
	 *  @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI#notifyCreateRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {

		((RequestDispatcherManagementNotificationI)this.offering).
			notifyCreateRequestSubmissionAndNotificationPorts(appUri, rdUri);
	}
}
