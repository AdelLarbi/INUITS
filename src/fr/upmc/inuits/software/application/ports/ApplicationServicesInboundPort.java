package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.application.Application;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;

/**
 * La classe <code> ApplicationServicesInboundPort </code> implémente le
 * port entrant via lequel les méthodes permettant d'acceder aux services de l'application 
 * qui permettent la demande a l'admissionController 
 *
 */
public class ApplicationServicesInboundPort
	extends AbstractInboundPort
	implements ApplicationServicesI {

	private static final long serialVersionUID = 1L;

	/**
	 * Permet la creation du port ApplicationServicesInboundPort.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post owner != null && owner instanceof Application;
	 * 
	 */
	public ApplicationServicesInboundPort(ComponentI owner) throws Exception {
			
		super(ApplicationServicesI.class, owner);

		assert owner != null && owner instanceof Application;
	}

	/**
	 * Permet la creation du port ApplicationServicesInboundPort.
	 * @param uri uri du port.
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune precondition.
	 * post owner != null && owner instanceof Application;
	 * 
	 */
	public ApplicationServicesInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationServicesI.class, owner);

		assert	owner != null && owner instanceof Application;
	}
		
	/**
	* @see fr.upmc.inuits.software.application.interfaces.ApplicationServicesI#sendRequestForApplicationExecution(int)
	*/
	@Override
	public void sendRequestForApplicationExecution(int coresToReserve) throws Exception {		
		
		final Application app = (Application) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						app.sendRequestForApplicationExecution(coresToReserve);
						return null;
					}
				});		
	}
}
