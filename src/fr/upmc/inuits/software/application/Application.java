package fr.upmc.inuits.software.application;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.pre.reflection.connectors.ReflectionConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
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
import fr.upmc.inuits.utils.Javassist;

public class Application 
extends AbstractComponent
implements ApplicationManagementI, ApplicationServicesI, ApplicationNotificationHandlerI {
	
	public static int DEBUG_LEVEL = 1;

	protected final String REQUEST_GENERATOR_JVM_URI = "";
	
	protected final String appURI;		
	protected ReflectionOutboundPort rop;
	
	protected ApplicationManagementInboundPort amip;
	protected ApplicationServicesInboundPort asip;
	protected ApplicationSubmissionOutboundPort asop;
	protected ApplicationNotificationInboundPort anip;
	
	protected RequestGeneratorManagementOutboundPort rgmop;
	protected DynamicComponentCreationOutboundPort portToRequestGeneratorJVM;

	protected final Double meanInterArrivalTime;
	protected final Long meanNumberOfInstructions;
	protected final String rgManagementInboundPortURI;
	
	protected final String rgURI;	
	protected final String rgRequestSubmissionOutboundPortURI;
	protected final String rgRequestNotificationInboundPortURI;
	
	public Application(
			String appURI,
			Double meanInterArrivalTime,
			Long meanNumberOfInstructions,
			String applicationManagementInboundPortURI,
			String applicationServicesInboundPortURI,
			String applicationSubmissionOutboundPortURI,
			String applicationNotificationInboundPortURI) throws Exception {
		
		super(appURI, 1, 1);
								
		assert meanInterArrivalTime > 0.0;
		assert meanNumberOfInstructions > 0;		
		assert applicationManagementInboundPortURI != null;
		assert applicationServicesInboundPortURI != null;
		assert applicationSubmissionOutboundPortURI != null;
		assert applicationNotificationInboundPortURI != null;
				
		this.meanInterArrivalTime = meanInterArrivalTime;
		this.meanNumberOfInstructions = meanNumberOfInstructions;
		
		this.rgURI = "rg-" + appURI; 		
		this.rgManagementInboundPortURI = appURI + "-rgm-ip";
		this.rgRequestSubmissionOutboundPortURI = appURI + "-rgrs-op";
		this.rgRequestNotificationInboundPortURI = appURI + "-rgrn-ip";
		
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
		this.rgmop = new RequestGeneratorManagementOutboundPort(this);
		this.addPort(this.rgmop);
		this.rgmop.publishPort();
		
		this.addRequiredInterface(DynamicComponentCreationI.class);
						
		assert this.appURI != null && this.appURI.length() > 0;
		assert this.amip != null && this.amip instanceof ApplicationManagementI;
		assert this.asip != null && this.asip instanceof ApplicationServicesI;
		assert this.asop != null && this.asop instanceof ApplicationSubmissionI;
		assert this.anip != null && this.anip instanceof ApplicationNotificationI;		
		assert this.rgmop != null && this.rgmop instanceof RequestGeneratorManagementI;		
	}
	
	@Override
	public void start() throws ComponentStartException {
		
		try {
			this.portToRequestGeneratorJVM = new DynamicComponentCreationOutboundPort(this);
			this.portToRequestGeneratorJVM.localPublishPort();
			this.addPort(this.portToRequestGeneratorJVM);
			this.portToRequestGeneratorJVM.doConnection(					
					this.REQUEST_GENERATOR_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());								
			
		} catch (Exception e) {
			throw new ComponentStartException(e);
		}

		super.start();
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
			if (this.portToRequestGeneratorJVM.connected()) {
				this.portToRequestGeneratorJVM.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
		}

		super.shutdown();
	}	
	
	public void dynamicRequestGeneratorDeploy() throws Exception {
							 								
		this.portToRequestGeneratorJVM.createComponent(
				RequestGenerator.class.getCanonicalName(),
				new Object[] {
						this.rgURI,
						this.meanInterArrivalTime,
						this.meanNumberOfInstructions,
						this.rgManagementInboundPortURI,
						this.rgRequestSubmissionOutboundPortURI,
						this.rgRequestNotificationInboundPortURI
				});							
		
		rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();
	
		rop.doConnection(this.rgURI, ReflectionConnector.class.getCanonicalName());
		
		RequestGenerator.DEBUG_LEVEL = 2;
		rop.toggleLogging();
		rop.toggleTracing();
	}
	
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) throws Exception {		
		
		rop.doPortConnection(
				this.rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				RequestSubmissionConnector.class.getCanonicalName());				
	}
	
	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) throws Exception {					
				
		this.rop.doPortConnection(
				this.rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				Javassist.getRequestSubmissionConnectorClassName());		
	}	
	
	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {			
		
		ropForRequestDispatcher.doPortConnection(
				dispatcherRequestNotificationOutboundPortUri,
				rgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
	}
	
	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {			
		
		ropForRequestDispatcher.doPortConnection(
				dispatcherRequestNotificationOutboundPortUri,
				rgRequestNotificationInboundPortURI,
				Javassist.getRequestNotificationConnectorClassName());
	}
	
	@Override
	public void	sendRequestForApplicationExecution(int coresToReserve) throws Exception {
						
		if (Application.DEBUG_LEVEL == 1) {	
			this.logMessage("Application " + this.appURI + " asking for execution permission.");
		}
		
		dynamicRequestGeneratorDeploy();
		
		this.asop.submitApplicationAndNotify(this.appURI, coresToReserve);
	}
	
	@Override
	public void acceptApplicationAdmissionNotification(boolean isAccepted) throws Exception {
		
		if (Application.DEBUG_LEVEL == 1) {			
			this.logMessage("Application " + this.appURI + " is notified that admission request "
					+ ((isAccepted)? "has" : "hasn't")
					+ " been accepted.");
		}
		
		if (isAccepted) {				
			launch();					
		}
	}
	
	public void	launch() throws Exception {						
		this.rgmop.doConnection(
				this.rgManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName());			
									
		this.rgmop.startGeneration();
		//Thread.sleep(20000L);		
		Thread.sleep(5000L);
		this.rgmop.stopGeneration();
	}
}
