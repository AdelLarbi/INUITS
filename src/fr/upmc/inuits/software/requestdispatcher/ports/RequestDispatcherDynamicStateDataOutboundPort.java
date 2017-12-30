package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.ports.AbstractControlledDataOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;

public class RequestDispatcherDynamicStateDataOutboundPort 
	extends AbstractControlledDataOutboundPort {

	private static final long serialVersionUID = 1L;
	protected String rdURI;
	
	public RequestDispatcherDynamicStateDataOutboundPort(ComponentI owner, String rdURI) throws Exception {
		
		super(owner);
		this.rdURI = rdURI;

		assert owner instanceof RequestDispatcherStateDataConsumerI;
	}

	public RequestDispatcherDynamicStateDataOutboundPort(String uri, ComponentI owner, String rdURI) 
			throws Exception {
			
		super(uri, owner);
		this.rdURI = rdURI;

		assert owner instanceof ComputerStateDataConsumerI;
	}
		
	@Override
	public void receive(DataRequiredI.DataI d) throws Exception {
	
		((RequestDispatcherStateDataConsumerI)this.owner).
			acceptRequestDispatcherDynamicData(this.rdURI, (RequestDispatcherDynamicStateI) d);
	}
}
