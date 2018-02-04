package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;
/**
 * la classe  <code>ApplicationServicesConnector</code> represente un
 * connecteur qui permet d'acceder à l'application pour la lancer (preparation de creation de composants et demande au controleur d'acceptation s'il y a assez de ressources)
 */
public class ApplicationServicesConnector 
	extends AbstractConnector
	implements ApplicationServicesI {

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationServicesI#sendRequestForApplicationExecution(int)
	 */
	@Override
	public void sendRequestForApplicationExecution(int coresToReserve) throws Exception {
		
		((ApplicationServicesI)this.offering).sendRequestForApplicationExecution(coresToReserve);
	}
}
