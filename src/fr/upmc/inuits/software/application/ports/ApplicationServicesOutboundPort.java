package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;

/**
 * La classe <code> ApplicationServicesOutboundPort </code> implémente le
 * port sortant à travers lequel on appelle les méthodes permettant d'acceder 
 * aux services de l'application qui permettent la demande a l'admissionController
 *
 */
public class ApplicationServicesOutboundPort 
	extends AbstractOutboundPort
	implements ApplicationServicesI {

	/**
	 * Permet la creation du port ApplicationServicesOutboundPort.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post owner != null;
	 * 
	 */
	public ApplicationServicesOutboundPort(ComponentI owner) throws Exception {
		super(ApplicationServicesI.class, owner);
			
		assert owner != null;
	}

	/**
	 * Permet la creation du port ApplicationServicesOutboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post uri != null && owner != null;
	 * 
	 */
	public ApplicationServicesOutboundPort(String uri, ComponentI owner) throws Exception {
		super(uri, ApplicationServicesI.class, owner);

		assert uri != null && owner != null;
	}
	
	/**
	* @see fr.upmc.inuits.software.application.interfaces.ApplicationServicesI#sendRequestForApplicationExecution(int)
	*/
	@Override
	public void sendRequestForApplicationExecution(int coresToReserve) throws Exception {
		
		((ApplicationServicesI)this.connector).sendRequestForApplicationExecution(coresToReserve);		
	}	
}
