package fr.upmc.inuits.software.application;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.interfaces.RequestGeneratorManagementI;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import fr.upmc.inuits.software.application.ports.ApplicationManagementInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationNotificationInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationServicesInboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationSubmissionOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class Application 
extends AbstractComponent
implements ApplicationManagementI, ApplicationServicesI, ApplicationNotificationHandlerI {
	
	public static int DEBUG_LEVEL = 1;

	protected final String appURI;
	protected final RequestGenerator requestGenerator;
	
	protected ApplicationManagementInboundPort amip;
	protected ApplicationServicesInboundPort asip;
	protected ApplicationSubmissionOutboundPort asop;
	protected ApplicationNotificationInboundPort anip;
	
	protected RequestGeneratorManagementOutboundPort rgmop;
	
	final String rgRequestSubmissionOutboundPortURI;
	final String rgRequestNotificationInboundPortURI;
	
	public Application(
			String appURI,			
			double meanInterArrivalTime,
			long meanNumberOfInstructions,
			String applicationManagementInboundPortURI,
			String applicationServicesInboundPortURI,
			String applicationSubmissionOutboundPortURI,
			String applicationNotificationInboundPortURI) throws Exception {
		
		super(1, 1);
				
		assert meanInterArrivalTime > 0.0;
		assert meanNumberOfInstructions > 0;		
		assert applicationManagementInboundPortURI != null;
		assert applicationServicesInboundPortURI != null;
		assert applicationSubmissionOutboundPortURI != null;
		assert applicationNotificationInboundPortURI != null;
		
		final String rgManagementOutboundPortURI = "rgm-op";
		
		final String rgURI = "rg-" + appURI; 		
		final String rgManagementInboundPortURI = appURI + "-rgm-ip";
		rgRequestSubmissionOutboundPortURI = appURI + "-rgrs-op";
		rgRequestNotificationInboundPortURI = appURI + "-rgrn-ip";
		
		this.appURI = appURI;
		
		this.addOfferedInterface(ApplicationManagementI.class);
		this.amip = new ApplicationManagementInboundPort(applicationManagementInboundPortURI, this);
		this.addPort(this.amip);
		this.amip.publishPort();
		
		this.addOfferedInterface(ApplicationServicesI.class);
		this.asip = new ApplicationServicesInboundPort(applicationServicesInboundPortURI, this);
		this.addPort(this.asip);
		this.asip.publishPort();
		
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
		
		this.rgmop.doConnection(
				rgManagementInboundPortURI, 
				RequestGeneratorManagementConnector.class.getCanonicalName());
		
		assert this.appURI != null && this.appURI.length() > 0;
		assert this.amip != null && this.amip instanceof ApplicationManagementI;
		assert this.asip != null && this.asip instanceof ApplicationServicesI;
		assert this.asop != null && this.asop instanceof ApplicationSubmissionI;
		assert this.anip != null && this.anip instanceof ApplicationNotificationI;		
		assert this.rgmop != null && this.rgmop instanceof RequestGeneratorManagementI;
		assert this.requestGenerator != null && this.requestGenerator instanceof RequestGenerator;		
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
			/* FIXME java.lang.NullPointerException
			if (this.requestGenerator.isPortConnected(rgRequestSubmissionOutboundPortURI)) {
				this.requestGenerator.doPortDisconnection(rgRequestSubmissionOutboundPortURI);
			}*/			
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}
	
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) throws Exception {
		
		requestGenerator.doPortConnection(
				rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				RequestSubmissionConnector.class.getCanonicalName());
	}
	
	@Override
	public void doConnectionWithDispatcherForNotification(RequestDispatcher requestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {
		
		requestDispatcher.doPortConnection(
				dispatcherRequestNotificationOutboundPortUri,
				rgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
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
		
		if (isAccepted) {			
			this.rgmop.startGeneration();
			Thread.sleep(20000L);		
			this.rgmop.stopGeneration();
		}
	}
}
