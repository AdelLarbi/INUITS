package fr.upmc.inuits.software.requestdispatcher.interfaces;

/**
 * L' interface <code>RequestDispatcherManagementNotificationHandlerI</code> permet la gestion des port entre dispatcher et l'AVM. 
 */
public interface RequestDispatcherManagementNotificationHandlerI {
	
	/**
	 * Permet d'associer le dispatcher aux avm associé 
	 * @param appUri Uri de l'app TODO a verifier.
	 * @param rdUri Uri du RequestDispatcher
	 * @throws Exception
	 */
	public void	acceptCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception;
}
