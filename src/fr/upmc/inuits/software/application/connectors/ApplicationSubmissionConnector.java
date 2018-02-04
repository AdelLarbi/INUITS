package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
/**
 * la classe  <code>ApplicationSubmissionConnector</code> represente un 
 * connecteur qui permet de soumettre une requete d'acceptation de l'application a l'admissionController
 */
public class ApplicationSubmissionConnector
	extends AbstractConnector
	implements ApplicationSubmissionI {

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI#submitApplicationAndNotify(String, int)
	 */
	@Override
	public void submitApplicationAndNotify(String appUri, int mustHaveCores) throws Exception {
		
		((ApplicationSubmissionI)this.offering).submitApplicationAndNotify(appUri, mustHaveCores);		
	}
}
