package fr.upmc.inuits.software.application.interfaces;

/**
 * L' interface <code>ApplicationNotificationHandlerI</code> permet de soumettre une requete d'acceptation de l'application a l'admissionController
 */
public interface ApplicationNotificationHandlerI {

	/**
	 * Méthode qui récupère la notification pour savoir s'il y a assez de ressources pour l'application on lance la génération.
	 * @param isAccepted booleen qui permet de savoir si l'application est accepté par l'admissionController
	 */
	public void	acceptApplicationAdmissionNotification(boolean isAccepted) throws Exception;
}
