package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;

/**
 * La classe <code> RequestDispatcherDynamicStateDataOutboundPort </code> implémente le
 * port sortant à travers lequel on peut acceder au composant RequestDispatcherDynamicStateData.
 */
public class RequestDispatcherDynamicStateDataOutboundPort 
	extends AbstractControlledDataOutboundPort {

	private static final long serialVersionUID = 1L;
	protected String rdURI;
	
	/**
	 * Permet la creation du port RequestDispatcherDynamicStateDataOutboundPort. 
	 * @param rdUri uri du requestDispatcher
	 * @param owner composant auquel on veut accéder.
	 * @throws Exception
	 * 
	 * pre aucune preCondition
	 * post owner instanceof RequestDispatcherStateDataConsumerI;
	 */
	public RequestDispatcherDynamicStateDataOutboundPort(ComponentI owner, String rdURI) throws Exception {
		
		super(owner);
		this.rdURI = rdURI;

		assert owner instanceof RequestDispatcherStateDataConsumerI;
	}

	/**
	 * Permet la creation du port RequestDispatcherDynamicStateDataOutboundPort. 
	 * @param owner composant composant auquel on veut accéder.
	 * @param uri uri du port
	 * @param rdURI uri du dispatcher
	 * @throws Exception
	 * 
	 * pre aucune precondition
	 * post owner instanceof ComputerStateDataConsumerI;
	 */
	public RequestDispatcherDynamicStateDataOutboundPort(String uri, ComponentI owner, String rdURI) 
			throws Exception {
			
		super(uri, owner);
		this.rdURI = rdURI;

		assert owner instanceof ComputerStateDataConsumerI;
	}
		
	/**
	 * @see fr.upmc.components.interfaces.DataRequiredI#receive()
	 * @return l'objet dynamique RequestDispatcherDynamicState  
	 * @throws Exception
	 */
	@Override
	public void receive(DataRequiredI.DataI d) throws Exception {
	
		((RequestDispatcherStateDataConsumerI)this.owner).
			acceptRequestDispatcherDynamicData(this.rdURI, (RequestDispatcherDynamicStateI) d);
	}
}
