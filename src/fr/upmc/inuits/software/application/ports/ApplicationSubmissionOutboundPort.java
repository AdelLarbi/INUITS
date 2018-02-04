package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;

/**
 * La classe <code> ApplicationSubmissionOutboundPort </code> implémente le
 * port sortant à travers lequel l'application soumet une requete d'acceptation de l'application a l'admissionController.
 */
public class ApplicationSubmissionOutboundPort
	extends AbstractOutboundPort
	implements ApplicationSubmissionI {

	/**
	 * Permet la creation du port ApplicationSubmissionOutboundPort.
	 * @param owner composant auquel on veut accéder .
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post aucune postcondition.
	 * 
	 */
	public ApplicationSubmissionOutboundPort(ComponentI owner) throws Exception {
	
		super(ApplicationSubmissionI.class, owner);
	}
	
	/**
	 * Permet la creation du port ApplicationSubmissionOutboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post uri != null
	 * 
	 */
	public ApplicationSubmissionOutboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationSubmissionI.class, owner);

		assert uri != null;
	}

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI#submitApplicationAndNotify(String, int)
	 */
	@Override
	public void submitApplicationAndNotify(String appUri, int mustHaveCores) throws Exception {
		
		((ApplicationSubmissionI)this.connector).submitApplicationAndNotify(appUri, mustHaveCores);		
	}
}
