package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L' interface <code>ApplicationNotificationI</code> permet a l'application de savoir si elle est acceptée par l'admissionControler
 */
public interface ApplicationNotificationI
	extends OfferedI, RequiredI {
	
	/**
	 * Permet au admissionController de notifier de sa reponse a l'application concernant son acceptation au sein du centre de calcul.
	 * @param isAccepted booleen qui si oui ou non l'applicationest acceptée.
	 * @throws Exception
	 */
	public void notifyApplicationAdmission(boolean isAccepted) throws Exception;
}
