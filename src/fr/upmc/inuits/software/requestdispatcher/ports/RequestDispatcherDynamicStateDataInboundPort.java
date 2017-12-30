package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataOfferedI.DataI;
import fr.upmc.datacenter.ports.AbstractControlledDataInboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class RequestDispatcherDynamicStateDataInboundPort 
	extends AbstractControlledDataInboundPort {
	
	private static final long serialVersionUID = 1L;

	public RequestDispatcherDynamicStateDataInboundPort(ComponentI owner) throws Exception {
		
		super(owner);
		
		assert owner instanceof RequestDispatcher;
	}

	public RequestDispatcherDynamicStateDataInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, owner);
		
		assert owner instanceof RequestDispatcher;
	}
		
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
