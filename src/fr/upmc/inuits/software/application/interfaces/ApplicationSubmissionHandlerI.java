package fr.upmc.inuits.software.application.interfaces;

/**
 * L' interface <code>ApplicationSubmissionHandlerI</code> permet d'acceder a l'admissionController pour lui soumettre une requete d'acceptation de l'application a l'admissionController
 */
public interface ApplicationSubmissionHandlerI {
	
	/**
	 * 
	 * @param appUri uri de l'application qui soumet la requete
	 * @param mustHaveCores nombre de core que l'application a besoin pour s'executer
	 * @throws Exception
	 */
	public void	acceptApplicationSubmissionAndNotify(String appUri, int mustHaveCores) throws Exception;
}
