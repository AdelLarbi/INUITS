package fr.upmc.inuits.software.application;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import fr.upmc.inuits.software.application.ports.ApplicationManagementInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationNotificationInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationSubmissionOutboundPort;

public class Application 
extends AbstractComponent
implements ApplicationManagementI, ApplicationNotificationHandlerI {
	
	public static int DEBUG_LEVEL = 1;

	protected final String appURI;
	protected ApplicationManagementInboundPort amip;
	protected ApplicationSubmissionOutboundPort asop;
	protected ApplicationNotificationInboundPort anip;
	
	public Application(
			String appURI,			
			String managementInboundPortURI,
			String applicationSubmissionOutboundPortURI,
			String applicationNotificationInboundPortURI) throws Exception {
		
		super(1, 1);
				
		assert managementInboundPortURI != null;
		assert applicationSubmissionOutboundPortURI != null;
		assert applicationNotificationInboundPortURI != null;
		
		this.appURI = appURI;
		
		this.addOfferedInterface(ApplicationManagementI.class);
		this.amip = new ApplicationManagementInboundPort(managementInboundPortURI, this);
		this.addPort(this.amip);
		this.amip.publishPort();
		
		this.addRequiredInterface(ApplicationSubmissionI.class) ;
		this.asop = new ApplicationSubmissionOutboundPort(applicationSubmissionOutboundPortURI, this);
		this.addPort(this.asop);
		this.asop.publishPort();
		
		this.addOfferedInterface(ApplicationNotificationI.class);
		this.anip = new ApplicationNotificationInboundPort(applicationNotificationInboundPortURI, this);
		this.addPort(this.anip);
		this.anip.publishPort();
		
		assert this.appURI != null && this.appURI.length() > 0;
		assert this.amip != null && this.amip instanceof ApplicationManagementI;
		assert this.asop != null && this.asop instanceof ApplicationSubmissionI;
		assert this.anip != null && this.anip instanceof ApplicationNotificationI;
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {	

		try {
			if (this.asop.connected()) {
				this.asop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}
	
	@Override
	public void	sendRequestForApplicationExecution() throws Exception {
		
		if (Application.DEBUG_LEVEL == 1) {	
			this.logMessage("Application " + this.appURI + " asking for execution permission.");
		}
		
		this.asop.submitApplicationAndNotify(this.appURI);
	}
	
	@Override
	public void acceptApplicationAdmissionNotification(boolean isAccepted) throws Exception {
		
		if (Application.DEBUG_LEVEL == 1) {			
			this.logMessage("Application " + this.appURI + " is notified that admission request "
					+ ((isAccepted)? "has" : "hasn't")
					+ " been accepted.");
		}		
	}
}
