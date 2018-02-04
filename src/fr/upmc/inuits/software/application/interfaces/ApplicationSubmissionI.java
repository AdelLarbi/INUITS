package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L' interface <code>ApplicationSubmissionI</code> permet de soumettre une requete d'acceptation de l'application a l'admissionController
 */
public interface ApplicationSubmissionI 
	extends OfferedI, RequiredI {
	
	/**
	 * Permet de demandé a l'admisssionController.
	 * @param appUri uri de l'application
	 * @param mustHaveCores nombre de core que l'application a besoin pour s'executer
	 * @throws Exception
	 */
	public void submitApplicationAndNotify(String appUri, int mustHaveCores) throws Exception;
}
