package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
/**
 * la classe  <code>ApplicationNotificationConnector</code> represente un
 * connecteur qui permet a l'application de recevoir l'information concernant sont acceptaton par l'admissionController 
 * Permet la Connection entre ApplicationNotificationOutboundPort et ApplicationNotificationInboundPort
 */
public class ApplicationNotificationConnector 
	extends AbstractConnector
	implements ApplicationNotificationI {

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI#notifyApplicationAdmission(boolean)
	 */
	@Override
	public void notifyApplicationAdmission(boolean isAccepted) throws Exception {
		
		((ApplicationNotificationI)this.offering).notifyApplicationAdmission(isAccepted);		
	}
}
