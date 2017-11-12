package fr.upmc.inuits.software.admissioncontroller;

import java.util.ArrayList;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.pre.dcc.connectors.DynamicComponentCreationConnector;
import fr.upmc.components.cvm.pre.dcc.interfaces.DynamicComponentCreationI;
import fr.upmc.components.cvm.pre.dcc.ports.DynamicComponentCreationOutboundPort;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.components.pre.reflection.connectors.ReflectionConnector;
import fr.upmc.components.pre.reflection.ports.ReflectionOutboundPort;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionHandlerI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import fr.upmc.inuits.software.application.ports.ApplicationManagementOutboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationNotificationOutboundPort;
import fr.upmc.inuits.software.application.ports.ApplicationSubmissionInboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;
import fr.upmc.inuits.utils.Javassist;

public class AdmissionController 
	extends AbstractComponent 
	implements ComputerStateDataConsumerI, ApplicationSubmissionHandlerI {
	
	public static int DEBUG_LEVEL = 1;	
	
	protected final String APPLICATION_VM_JVM_URI = "";
	protected final String REQUEST_DISPATCHER_JVM_URI = "";
	
	final String AVM_MANAGEMENT_IN_PORT_URI = "am-ip";
	final String AVM_MANAGEMENT_OUT_PORT_URI = "am-op";	
	final String AVM_REQUEST_SUBMISSION_IN_PORT_URI = "ars-ip";
	final String AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = "arn-op";
	
	final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrs-ip";
	final String RD_REQUEST_SUBMISSION_OUT_PORT_URI = "rdrs-op";
	final String RD_REQUEST_NOTIFICATION_IN_PORT_URI = "rdrn-ip";
	final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrn-op";		
	
	protected ComputerServicesOutboundPort csop;
	protected ComputerStaticStateDataOutboundPort cssdop;
	protected ComputerDynamicStateDataOutboundPort cdsdop;
	protected ApplicationManagementOutboundPort amop;
	protected ApplicationSubmissionInboundPort asip;
	protected ApplicationNotificationOutboundPort anop;
	
	protected DynamicComponentCreationOutboundPort portToApplicationVMJVM;
	protected DynamicComponentCreationOutboundPort portToRequestDispatcherJVM;
	
	protected ApplicationVMManagementOutboundPort avmOutPort;
	
	protected int numberOfProcessors;
	protected int numberOfCoresPerProcessor;	
	boolean[][] reservedCores;
		
	public AdmissionController(
			ArrayList<String> computersURI,			
			String computerServicesOutboundPortURI,
			String computerStaticStateDataOutboundPortURI,
			String computerDynamicStateDataOutboundPortURI,
			String applicationManagementOutboundPortURI,
			String applicationSubmissionInboundPortURI,
			String applicationNotificationOutboundPortURI) throws Exception {
		
		super(1, 1);
		
		assert computersURI != null && computersURI.size() > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.length() > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.length() > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.length() > 0;		
		assert applicationManagementOutboundPortURI != null && applicationManagementOutboundPortURI.length() > 0;
		assert applicationSubmissionInboundPortURI != null && applicationSubmissionInboundPortURI.length() > 0;
		assert applicationNotificationOutboundPortURI != null && applicationNotificationOutboundPortURI.length() > 0;
	
		this.addRequiredInterface(ComputerServicesI.class);
		this.csop = new ComputerServicesOutboundPort(computerServicesOutboundPortURI, this);
		this.addPort(this.csop);
		this.csop.publishPort();
		
		// this.addOfferedInterface(ComputerStaticStateDataI.class);
		// or :
		this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		
		this.cssdop = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI, this, computersURI.get(0));
		this.addPort(this.cssdop);
		this.cssdop.publishPort();

		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		this.cdsdop = new ComputerDynamicStateDataOutboundPort(computerDynamicStateDataOutboundPortURI, this, computersURI.get(0));
		this.addPort(this.cdsdop);
		this.cdsdop.publishPort();
				
		this.addRequiredInterface(ApplicationManagementI.class);
		this.amop = new ApplicationManagementOutboundPort(applicationManagementOutboundPortURI, this);
		this.addPort(this.amop);
		this.amop.publishPort();
		
		this.addOfferedInterface(ApplicationSubmissionI.class);
		this.asip = new ApplicationSubmissionInboundPort(applicationSubmissionInboundPortURI, this);
		this.addPort(this.asip);
		this.asip.publishPort();
		
		this.addRequiredInterface(ApplicationNotificationI.class);
		this.anop = new ApplicationNotificationOutboundPort(applicationNotificationOutboundPortURI, this);
		this.addPort(this.anop);
		this.anop.publishPort();											
		
		this.addRequiredInterface(DynamicComponentCreationI.class);
		
		assert this.cssdop != null && this.cssdop instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.amop != null && this.amop instanceof ApplicationManagementI;
		assert this.asip != null && this.asip instanceof ApplicationSubmissionI;
		assert this.anop != null && this.anop instanceof ApplicationNotificationI;		
	}
	
	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
				
		try {	
			this.portToApplicationVMJVM = new DynamicComponentCreationOutboundPort(this);
			this.portToApplicationVMJVM.localPublishPort();
			this.addPort(this.portToApplicationVMJVM);
			this.portToApplicationVMJVM.doConnection(					
					this.APPLICATION_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
			
			this.portToRequestDispatcherJVM = new DynamicComponentCreationOutboundPort(this);
			this.portToRequestDispatcherJVM.localPublishPort();
			this.addPort(this.portToRequestDispatcherJVM);
			this.portToRequestDispatcherJVM.doConnection(					
					this.REQUEST_DISPATCHER_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
		
			// start the pushing of dynamic state information from the computer;
			// here only one push of information is planned after one second.
			this.cdsdop.startUnlimitedPushing(1000);			
			//this.cdsdop.startLimitedPushing(1000, 25);			
													
		} catch (Exception e) {
			throw new ComponentStartException("Unable to start pushing dynamic data from the computer component.", e);
		}
		
		this.reservedCores = new boolean[this.numberOfProcessors][this.numberOfCoresPerProcessor];
		for (int np = 0; np < this.numberOfProcessors; np++) {
			for(int nc = 0; nc < this.numberOfCoresPerProcessor; nc++) {
				this.reservedCores[np][nc] = false;
			}
		}
		
		assert this.reservedCores != null;
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {			
			if (this.csop.connected()) {
				this.csop.doDisconnection();
			}
			if (this.cssdop.connected()) {
				this.cssdop.doDisconnection();
			}
			if (this.cdsdop.connected()) {
				this.cdsdop.doDisconnection();
			}
			if (this.amop.connected()) {
				this.amop.doDisconnection();
			}
			if (this.anop.connected()) {
				this.anop.doDisconnection();
			}			
			if (this.portToApplicationVMJVM.connected()) {
				this.portToApplicationVMJVM.doDisconnection();
			}
			if (this.portToRequestDispatcherJVM.connected()) {
				this.portToRequestDispatcherJVM.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}

	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
		
		this.numberOfProcessors = staticState.getNumberOfProcessors();
		this.numberOfCoresPerProcessor = staticState.getNumberOfCoresPerProcessor();			
		
		if (AdmissionController.DEBUG_LEVEL == 2) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Admission controller accepting static data from " + computerURI + "\n");
			sb.append("  timestamp                     : " + staticState.getTimeStamp() + "\n");		   							
			sb.append("  timestamper id                : " + staticState.getTimeStamperId() + "\n");									
			sb.append("  number of processors          : " + staticState.getNumberOfProcessors() + "\n");			
			sb.append("  number of cores per processor : " + staticState.getNumberOfCoresPerProcessor() + "\n");									
			
			for (int p = 0; p < staticState.getNumberOfProcessors(); p++) {
				if (p == 0) {
					sb.append("  processor URIs                 : ");
					
				} else {
					sb.append("                                 : ");
				}
				sb.append(p + "  " + staticState.getProcessorURIs().get(p) + "\n");
			}
			
			sb.append("  processor port URIs            : " + "\n");
			sb.append(Computer.printProcessorsInboundPortURI(10, staticState.getNumberOfProcessors(), 
					staticState.getProcessorURIs(), staticState.getProcessorPortMap()));
			
			this.logMessage(sb.toString());
		}
	}

	@Override
	public void acceptComputerDynamicData(String computerURI, ComputerDynamicStateI currentDynamicState)
			throws Exception {
		
		this.reservedCores = currentDynamicState.getCurrentCoreReservations();
		
		if (AdmissionController.DEBUG_LEVEL == 2) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Admission controller accepting dynamic data from " + computerURI + "\n");
			sb.append("  timestamp                : " + currentDynamicState.getTimeStamp() + "\n");
			sb.append("  timestamper id           : " + currentDynamicState.getTimeStamperId() + "\n");
			
			boolean[][] reservedCores = currentDynamicState.getCurrentCoreReservations();
			for (int p = 0; p < reservedCores.length; p++) {
				if (p == 0) {
					sb.append("  reserved cores           : ");
					
				} else {
					sb.append("                             ");
				}								
						
				for (int c = 0; c < reservedCores[p].length; c++) {										 			
					if (reservedCores[p][c]) {
						sb.append("T ");
						
					} else {
						sb.append("F ");
					}
				}
			}
			
			this.logMessage(sb.toString());
		}			
	}

	@Override
	public void acceptApplicationSubmissionAndNotify(String appUri) throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller checking for available resources to execute " + appUri + ".");
		}
		
		if (isResourcesAvailable()) {
			acceptApplication(appUri);
			this.anop.notifyApplicationAdmission(true);
			
		} else {
			rejectApplication(appUri);
			this.anop.notifyApplicationAdmission(false);
		}		
	}
	
	public boolean isResourcesAvailable() {		
		
		for (int p = 0; p < reservedCores.length; p++) {
			for (int c = 0; c < reservedCores[0].length; c++) {
				if (!this.reservedCores[p][c]) {					
					return true;
				}
			}
		}
			
		return false;
	}

	public void acceptApplication(String appUri) throws Exception {
		
		this.logMessage("Admission controller allow application " + appUri + " to be executed.");
		
		deployComponents(appUri);
		
		int coresToAllocateCount = 4;
			
		//FIXME
		//assert coresToAllocateCount <= this.numberOfProcessors * this.numberOfCoresPerProcessor;
		
		AllocatedCore[] ac = this.csop.allocateCores(coresToAllocateCount);
		this.avmOutPort.allocateCores(ac);
	}
	
	public void rejectApplication(String appUri) {
		
		this.logMessage("Admission controller can't accept application " + appUri + " because of lack of resources.");		
	}
	
	public void deployComponents(String appUri) throws Exception {						 			
				
		this.logMessage("Admission controller deploying components for " + appUri + ".");
		
		this.portToApplicationVMJVM.createComponent(
				ApplicationVM.class.getCanonicalName(),
				new Object[] {
						"vm0",
						AVM_MANAGEMENT_IN_PORT_URI,
					    AVM_REQUEST_SUBMISSION_IN_PORT_URI,
					    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI
				});			
		
		this.portToRequestDispatcherJVM.createComponent(
				RequestDispatcher.class.getCanonicalName(),
				new Object[] {
						"rd0",							
						RD_REQUEST_SUBMISSION_IN_PORT_URI,
						RD_REQUEST_SUBMISSION_OUT_PORT_URI,
						RD_REQUEST_NOTIFICATION_IN_PORT_URI,
						RD_REQUEST_NOTIFICATION_OUT_PORT_URI
				});					
		// --------------------------------------------------------------------
		this.avmOutPort = new ApplicationVMManagementOutboundPort(
						AVM_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avmOutPort.publishPort();
		this.avmOutPort.doConnection(
				AVM_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());			
		// --------------------------------------------------------------------
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();			
		
		rop.doConnection("rd0", ReflectionConnector.class.getCanonicalName());
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		rop.toggleLogging();
		rop.toggleTracing();
						
		this.amop.doDynamicConnectionWithDispatcherForSubmission(RD_REQUEST_SUBMISSION_IN_PORT_URI);
		this.amop.doDynamicConnectionWithDispatcherForNotification(rop, RD_REQUEST_NOTIFICATION_OUT_PORT_URI);		
		rop.doPortConnection(
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				AVM_REQUEST_SUBMISSION_IN_PORT_URI,
				Javassist.getRequestSubmissionConnectorClassName());
		
		rop.doDisconnection();														
		// --------------------------------------------------------------------
		rop.doConnection("vm0", ReflectionConnector.class.getCanonicalName());

		RequestDispatcher.DEBUG_LEVEL = 1;
		rop.toggleTracing();
		rop.toggleLogging();
		
		rop.doPortConnection(
				AVM_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				Javassist.getRequestNotificationConnectorClassName());
		
		rop.doDisconnection();
	}
}
