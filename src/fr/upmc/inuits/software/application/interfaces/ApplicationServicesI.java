package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

/**
 * L' interface <code>ApplicationServicesI</code> permet d'acceder aux services de l'application qui permettent la demande a l'admissionController.
 */
public interface ApplicationServicesI 
	extends OfferedI, RequiredI {
		
	/**
	 * Deploiement du requestGenerateur, et demande de l'application a l'admissionController s'il y a assez de ressource pour executer ses requetes.
	 * @param coresToReserve nombre de core que veux reserver l'application pour son éxécution.
	 */
	public void sendRequestForApplicationExecution(int coresToReserve) throws Exception;
}
