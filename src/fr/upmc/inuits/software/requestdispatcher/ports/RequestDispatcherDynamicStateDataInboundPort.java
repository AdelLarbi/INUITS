package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataOfferedI.DataI;
import fr.upmc.datacenter.ports.AbstractControlledDataInboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;
/**
 * La classe <code> ApplicationNotificationInboundPort </code> implémente le
 * port entrant via lequel on peut acceder au composant RequestDispatcherDynamicState.
 */
public class RequestDispatcherDynamicStateDataInboundPort 
	extends AbstractControlledDataInboundPort {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Contructeur creant le port de type RequestDispatcherDynamicStateDataInboundPort
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post owner instanceof RequestDispatcher
	 */
	public RequestDispatcherDynamicStateDataInboundPort(ComponentI owner) throws Exception {
		
		super(owner);
		
		assert owner instanceof RequestDispatcher;
	}


	/**
	 * Contructeur creant le port de type RequestDispatcherDynamicStateDataInboundPort avec sont l'URI.
	 * @param uri du port
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * 
	 * pre aucune preCondition
	 * post owner instanceof RequestDispatcher
	 */
	public RequestDispatcherDynamicStateDataInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, owner);
		
		assert owner instanceof RequestDispatcher;
	}
	
	/**
	 * Permet d'atteindre les services offer du composant 
	 * @see fr.upmc.components.interfaces.DataOfferedI#get()
	 * @return l'objet dynamique RequestDispatcherDynamicState  
	 * @throws Exception
	 */
	@Override
	public DataI get() throws Exception {
		
		final RequestDispatcher rd = (RequestDispatcher) this.owner;
		
		return rd.handleRequestSync(
					new ComponentI.ComponentService<DataOfferedI.DataI>() {
						@Override
						public DataOfferedI.DataI call() throws Exception {
							return rd.getDynamicState();
						}
					});
	}
}
