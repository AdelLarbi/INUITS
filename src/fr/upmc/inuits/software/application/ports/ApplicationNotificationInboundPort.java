package fr.upmc.inuits.software.application.ports;

import fr.upmc.components.ComponentI;
import fr.upmc.components.ports.AbstractInboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;

public class ApplicationNotificationInboundPort 
extends	AbstractInboundPort
implements ApplicationNotificationI {

	private static final long serialVersionUID = 1L;

	public ApplicationNotificationInboundPort(ComponentI owner) throws Exception {
		
		super(ApplicationNotificationI.class, owner);

		assert owner instanceof ApplicationNotificationHandlerI;		
	}
	
	public ApplicationNotificationInboundPort(String uri, ComponentI owner) throws Exception {
			
		super(uri, ApplicationNotificationI.class, owner);

		assert uri != null && owner instanceof ApplicationNotificationHandlerI;
	}
	
	@Override
	public void notifyApplicationAdmission() throws Exception {
		
		final ApplicationNotificationHandlerI appNotificationHandlerI = (ApplicationNotificationHandlerI) this.owner;
		
		this.owner.handleRequestAsync(
				new ComponentI.ComponentService<Void>() {
					@Override
					public Void call() throws Exception {
						appNotificationHandlerI.acceptApplicationAdmissionNotification();
						return null;
					}
				});		
	}
}
