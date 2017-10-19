package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.application.Application;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;

public class ApplicationManagementInboundPort
	extends AbstractInboundPort
	implements ApplicationManagementI {

	private static final long serialVersionUID = 1L;

	public ApplicationManagementInboundPort(ComponentI owner) throws Exception {
			
		super(ApplicationManagementI.class, owner);

		assert owner != null && owner instanceof Application;
	}

	public ApplicationManagementInboundPort(String uri, ComponentI owner) throws Exception {
		
		super(uri, ApplicationManagementI.class, owner);

		assert	owner != null && owner instanceof Application;
	}
		
	@Override
	public void sendRequestForApplicationExecution() throws Exception {		
		
		final Application app = (Application) this.owner;
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						app.sendRequestForApplicationExecution();
						return null;
					}
				});
		
	}
}
