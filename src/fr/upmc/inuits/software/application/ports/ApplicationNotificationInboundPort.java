package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;

/**
 * La classe <code> ApplicationNotificationInboundPort </code> implémente le
 * port entrant via lequel les méthodes permettant d'acceder au service de l'application 
 * pour notifier l'application de la reponse du controller concernant son acceptation au sein du centre de calcul. 
 *
 */
public class ApplicationNotificationInboundPort 
	extends	AbstractInboundPort
	implements ApplicationNotificationI {

	private static final long serialVersionUID = 1L;

	/**
	 * Permet la creation du port ApplicationNotificationInboundPort.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post owner instanceof ApplicationNotificationHandlerI
	 * 
	 */
	public ApplicationNotificationInboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationNotificationI.class, owner);

		assert owner instanceof ApplicationNotificationHandlerI;		
	}
	
	/**
	 * Permet la creation du port ApplicationNotificationInboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post uri != null && owner instanceof ApplicationNotificationHandlerI
	 * 
	 */
	public ApplicationNotificationInboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, ApplicationNotificationI.class, owner);

		assert uri != null && owner instanceof ApplicationNotificationHandlerI;
	}
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI#notifyApplicationAdmission(boolean)
	 */
	@Override
	public void notifyApplicationAdmission(boolean isAccepted) throws Exception {
		
		final ApplicationNotificationHandlerI appNotificationHandlerI = (ApplicationNotificationHandlerI) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						appNotificationHandlerI.acceptApplicationAdmissionNotification(isAccepted);
						return null;
					}
				});		
	}
}
