package fr.upmc.inuits.software.requestdispatcher.connector;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
/**
 * 
 * La classe <code>RequestDispatcherManagementConnector</code> implemente un
 * connecteur pour la gestion de la repartition de requêtes .
 *
 * @author evergreen
 *
 */
public class RequestDispatcherManagementConnector
	extends AbstractConnector
	implements RequestDispatcherManagementI {

	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#createRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		((RequestDispatcherManagementI)this.offering).
		createRequestSubmissionAndNotificationPorts(requestSubmissionOutboundPortUri, requestNotificationIntboundPortUri);	
	}
	
	/**
	 *  @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#destroyRequestSubmissionAndNotificationPorts()
	 */
	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {

		((RequestDispatcherManagementI)this.offering).destroyRequestSubmissionAndNotificationPorts();
	}
}
