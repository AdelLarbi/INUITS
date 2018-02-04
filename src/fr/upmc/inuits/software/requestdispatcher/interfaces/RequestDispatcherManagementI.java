package fr.upmc.inuits.software.requestdispatcher.interfaces;

/**
 * L' interface <code>RequestDispatcherManagementI</code> permet la gestion des port entre dispatcher et l'AVM. 
 *
 */
public interface RequestDispatcherManagementI {
	/**
	 * 
	 * Permet la creation des ports de soumission entre l'AVM et le dispatcher.  
	 * Mise a jour du nombre d'AVM disponible .
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre requestSubmissionOutboundPortUri != null && requestSubmissionOutboundPortUri.length() > 0
	 * pre requestNotificationIntboundPortUri != null && requestNotificationIntboundPortUri.length() > 0
	 * 
	 * post this.rsop != null && this.rsop.get(i) instanceof RequestSubmissionI
	 * post this.rnip != null && this.rnip.get(i) instanceof RequestNotificationI
	 * </pre>
	 * 
	 * 
	 */
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri, 
			String requestNotificationIntboundPortUri) throws Exception;
	
	/**
	 * Permet la destruction des ports de soumission entre l'AVM et le dispatcher.  
	 * Mise a jour du nombre d'AVM.
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre this.availableApplicationVm >= 3
	 * 
	 * post this.availableApplicationVm >= 2
	 * </pre>
	 * 
	 * 
	 */
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception;
}
