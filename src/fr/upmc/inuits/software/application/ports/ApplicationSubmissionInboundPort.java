package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;

public class ApplicationSubmissionInboundPort
	extends AbstractInboundPort
	implements ApplicationSubmissionI {

	private static final long serialVersionUID = 1L;
	
	public ApplicationSubmissionInboundPort(ComponentI owner) throws Exception {
			
		super(ApplicationSubmissionI.class, owner);
		
		assert owner instanceof ApplicationSubmissionHandlerI;
	}

	public ApplicationSubmissionInboundPort(String uri, ComponentI owner ) throws Exception {
		
		super(uri, RequestSubmissionI.class, owner);

		assert uri != null && owner instanceof ApplicationSubmissionHandlerI;
	}

	@Override
	public void submitApplicationAndNotify() throws Exception {
		
		final ApplicationSubmissionHandlerI appSubmissionHandler = (ApplicationSubmissionHandlerI) this.owner;

		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						appSubmissionHandler.acceptApplicationSubmissionAndNotify();
						return null;
					}
				});		
	}
}
