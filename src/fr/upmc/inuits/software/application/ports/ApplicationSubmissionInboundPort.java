package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;

/**
 * La classe <code> ApplicationSubmissionInboundPort </code> implémente le
 * port entrant via lequel l'application soumet une requete d'acceptation de l'application a l'admissionController.
 */
public class ApplicationSubmissionInboundPort
	extends AbstractInboundPort
	implements ApplicationSubmissionI {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Permet la creation du port ApplicationSubmissionInboundPort.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post aucune postcondition.
	 * 
	 */
	public ApplicationSubmissionInboundPort(ComponentI owner) throws Exception {
			
		super(ApplicationSubmissionI.class, owner);
		
		assert owner instanceof ApplicationSubmissionHandlerI;
	}

	/**
	 * Permet la creation du port ApplicationSubmissionInboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post aucune postcondition
	 * 
	 */
	public ApplicationSubmissionInboundPort(String uri, ComponentI owner ) throws Exception {
		
		super(uri, ApplicationSubmissionI.class, owner);

		assert uri != null && owner instanceof ApplicationSubmissionHandlerI;
	}

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI#submitApplicationAndNotify(String, int)
	 */
	@Override
	public void submitApplicationAndNotify(String appUri, int mustHaveCores) throws Exception {
		
		final ApplicationSubmissionHandlerI appSubmissionHandler = (ApplicationSubmissionHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						appSubmissionHandler.acceptApplicationSubmissionAndNotify(appUri, mustHaveCores);
						return null;
					}
				});		
	}
}
