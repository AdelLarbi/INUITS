package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;

/**
 * La classe <code> ApplicationNotificationOutboundPort </code> implémente le
 * port sortant à travers lequel on appelle les méthodes permettant d'acceder au service de l'application 
 * pour la notifier de la reponse du controller concernant son acceptation au sein du centre de calcul.
 * @author evergreen
 *
 */
public class ApplicationNotificationOutboundPort 
	extends AbstractOutboundPort
	implements ApplicationNotificationI {

	/**
	 * Permet la creation du port ApplicationNotificationOutboundPort.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post aucune postcondition
	 * 
	 */
	public ApplicationNotificationOutboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationNotificationI.class, owner);
	}
	
	/**
	 * Permet la creation du port ApplicationNotificationOutboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post uri != null;
	 * 
	 */
	public ApplicationNotificationOutboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, ApplicationNotificationI.class, owner);

		assert uri != null;
	}
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI#notifyApplicationAdmission(boolean)
	 */
	@Override
	public void notifyApplicationAdmission(boolean isAccepted) throws Exception {
	
		((ApplicationNotificationI)this.connector).notifyApplicationAdmission(isAccepted);		
	}
}
