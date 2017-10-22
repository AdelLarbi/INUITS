package fr.upmc.inuits.software.application;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import fr.upmc.inuits.software.application.ports.ApplicationManagementInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationNotificationInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class Application 
extends AbstractComponent
implements ApplicationManagementI, ApplicationNotificationHandlerI {
	
	public static int DEBUG_LEVEL = 1;

	protected final String appURI;
	protected final RequestGenerator requestGenerator;
	
	protected ApplicationManagementInboundPort amip;
	protected ApplicationSubmissionOutboundPort asop;
	protected ApplicationNotificationInboundPort anip;
	
	protected RequestGeneratorManagementOutboundPort rgmop;			
	//protected RequestSubmissionInboundPort rsip;
	protected RequestNotificationOutboundPort rnop;
	
	final String rgRequestSubmissionOutboundPortURI;
	
	public Application(
			String appURI,			
			double meanInterArrivalTime,
			long meanNumberOfInstructions,
			String applicationManagementInboundPortURI,
			String applicationSubmissionOutboundPortURI,
			String applicationNotificationInboundPortURI) throws Exception {
		
		super(1, 1);
				
		assert meanInterArrivalTime > 0.0;
		assert meanNumberOfInstructions > 0;
		assert applicationManagementInboundPortURI != null;
		assert applicationSubmissionOutboundPortURI != null;
		assert applicationNotificationInboundPortURI != null;
		
		final String rgManagementOutboundPortURI = "rgm-op";
		
		final String rgURI = "rg-" + appURI; 		
		final String rgManagementInboundPortURI = appURI + "-rgm-ip";
		rgRequestSubmissionOutboundPortURI = appURI + "-rgrs-op";
		final String rgRequestNotificationInboundPortURI = appURI + "-rgrn-ip";
		
		this.appURI = appURI;
		
		this.addOfferedInterface(ApplicationManagementI.class);
		this.amip = new ApplicationManagementInboundPort(applicationManagementInboundPortURI, this);
		this.addPort(this.amip);
		this.amip.publishPort();
		
		this.addRequiredInterface(ApplicationSubmissionI.class);
		this.asop = new ApplicationSubmissionOutboundPort(applicationSubmissionOutboundPortURI, this);
		this.addPort(this.asop);
		this.asop.publishPort();
		
		this.addOfferedInterface(ApplicationNotificationI.class);
		this.anip = new ApplicationNotificationInboundPort(applicationNotificationInboundPortURI, this);
		this.addPort(this.anip);
		this.anip.publishPort();
				
		this.addRequiredInterface(RequestGeneratorManagementI.class);
		this.rgmop = new RequestGeneratorManagementOutboundPort(rgManagementOutboundPortURI, this);
		this.addPort(this.rgmop);
		this.rgmop.publishPort();									
		
		this.requestGenerator = new RequestGenerator(				
				rgURI,
				meanInterArrivalTime,
				meanNumberOfInstructions,
				rgManagementInboundPortURI,
				rgRequestSubmissionOutboundPortURI,
				rgRequestNotificationInboundPortURI);
		
		AbstractCVM.theCVM.addDeployedComponent(requestGenerator);		
				
		RequestGenerator.DEBUG_LEVEL = 2;
		this.requestGenerator.toggleTracing();
		this.requestGenerator.toggleLogging();
		
		/*this.rsip = new RequestSubmissionInboundPort(rgURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		this.rsip.doConnection(
				rgRequestSubmissionOutboundPortURI, 
				RequestSubmissionConnector.class.getCanonicalName());*/
		
		this.rgmop.doConnection(
				rgManagementInboundPortURI, 
				RequestGeneratorManagementConnector.class.getCanonicalName());
		
		this.rnop = new RequestNotificationOutboundPort(rgURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		this.rnop.doConnection(
				rgRequestNotificationInboundPortURI, 
				RequestNotificationConnector.class.getCanonicalName());
		
		//foo();
		
		assert this.appURI != null && this.appURI.length() > 0;
		assert this.amip != null && this.amip instanceof ApplicationManagementI;
		assert this.asop != null && this.asop instanceof ApplicationSubmissionI;
		assert this.anip != null && this.anip instanceof ApplicationNotificationI;		
		assert this.rgmop != null && this.rgmop instanceof RequestGeneratorManagementI;		
		//assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {	

		try {
			if (this.asop.connected()) {
				this.asop.doDisconnection();
			}
			if (this.rgmop.connected()) {
				this.rgmop.doDisconnection();
			}			
			if (this.rnop.connected()) {
				this.rnop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}
	
	/*@Override
	public void	toggleLogging() {
		
		this.requestGenerator.toggleLogging();		
		super.toggleLogging();
	}
	
	@Override
	public void toggleTracing() {
		
		this.requestGenerator.toggleTracing();		
		super.toggleTracing();
	}*/
	
	@Override
	public void	sendRequestForApplicationExecution() throws Exception {
		
		if (Application.DEBUG_LEVEL == 1) {	
			this.logMessage("Application " + this.appURI + " asking for execution permission.");
		}
		
		this.asop.submitApplicationAndNotify(this.appURI);
	}
	
	@Override
	public void doConnectionWithDispatcher(String dispatcherRequestSubmissionInboundPortUri) throws Exception {
		System.out.println("BEGIN -> Application");
		requestGenerator.doPortConnection(
				rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				RequestSubmissionConnector.class.getCanonicalName());
		System.out.println("END -> Application");
	}
	
	@Override
	public void acceptApplicationAdmissionNotification(boolean isAccepted) throws Exception {
		
		if (Application.DEBUG_LEVEL == 1) {			
			this.logMessage("Application " + this.appURI + " is notified that admission request "
					+ ((isAccepted)? "has" : "hasn't")
					+ " been accepted.");
		}
		
		if (isAccepted) {			
			this.rgmop.startGeneration();
			Thread.sleep(20000L);		
			this.rgmop.stopGeneration();
		}
	}
	
	private void foo() throws Exception {
		final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrs-ip";
		final String RD_REQUEST_SUBMISSION_OUT_PORT_URI = "rdrs-op";
		final String RD_REQUEST_NOTIFICATION_IN_PORT_URI = "rdrn-ip";
		final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrn-op";
		
		RequestDispatcher requestDispatcher;
		
		requestDispatcher = new RequestDispatcher(				
				"rd0",							
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		AbstractCVM.theCVM.addDeployedComponent(requestDispatcher);
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		requestDispatcher.toggleTracing();
		requestDispatcher.toggleLogging();
		
		requestGenerator.doPortConnection(
				rgRequestSubmissionOutboundPortURI,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
	}
}
