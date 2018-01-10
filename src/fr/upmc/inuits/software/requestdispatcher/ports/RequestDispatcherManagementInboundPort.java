package fr.upmc.inuits.software.requestdispatcher.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;

public class RequestDispatcherManagementInboundPort
	extends AbstractInboundPort
	implements RequestDispatcherManagementI {

	private static final long serialVersionUID = 1L;

	public RequestDispatcherManagementInboundPort(ComponentI owner) throws Exception {
		
		super(RequestDispatcherManagementI.class, owner);

		assert owner != null && owner instanceof RequestDispatcher;
	}

	public RequestDispatcherManagementInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, RequestDispatcherManagementI.class, owner);

		assert	owner != null && owner instanceof RequestDispatcher;
	}
	
	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		final RequestDispatcher rd = (RequestDispatcher) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rd.createRequestSubmissionAndNotificationPorts(requestSubmissionOutboundPortUri, 
								requestNotificationIntboundPortUri);
						return null;
					}
				});
	}

	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {

		final RequestDispatcher rd = (RequestDispatcher) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						rd.destroyRequestSubmissionAndNotificationPorts();
						return null;
					}
				});
	}
}
