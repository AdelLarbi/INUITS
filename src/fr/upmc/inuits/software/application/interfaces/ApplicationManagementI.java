package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;

/**
 * L' interface <code>ApplicationManagementI</code> permet de faire la connexion entre le requestDispatcher et le requestGenerator de manier static et dynammique. 
 *
 */
public interface ApplicationManagementI 
	extends OfferedI, RequiredI {

	/**
	 * Connection static entre le requestGenerator et le requestGenerator pour la soumission
	 * @param dispatcherRequestSubmissionInboundPortUri Uri pour connecter le requestDispatcher et requestGenerater
	 */
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception;
	
	/**
	 * Connection dynamique entre le requestGenerator et le requestGenerator pour la soumission
	 * @param dispatcherRequestSubmissionInboundPortUri Uri pour connecter le requestDispatcher et requestGenerater
	 */
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) 
			throws Exception;
	
	/**
	 * Connection static entre le requestGenerator et le requestGenerator pour la notification
	 * @param ropForRequestDispatcher objet(port) qui permet de connecter le le requestDispatcher et requestGenerater
	 * @param dispatcherRequestNotificationOutboundPortUri Uri pour connecter le requestDispatcher et requestGenerater
	 */
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher, 
			String dispatcherRequestNotificationOutboundPortUri) throws Exception;
	
	/**
	 * Connection dynamique entre le requestGenerator et le requestGenerator pour la notification
	 * @param ropForRequestDispatcher objet(port) qui permet de connecter le le requestDispatcher et requestGenerater
	 * @param dispatcherRequestNotificationOutboundPortUri Uri pour connecter le requestDispatcher et requestGenerater
	 */
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher, 
			String dispatcherRequestNotificationOutboundPortUri) throws Exception;	
}
