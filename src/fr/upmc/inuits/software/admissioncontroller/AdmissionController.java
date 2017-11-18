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
import fr.upmc.datacenter.software.applicationvm.interfaces.ApplicationVMManagementI;
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
	
	final String[] AVM_MANAGEMENT_IN_PORT_URI = {"a1m-ip","a2m-ip","a3m-ip"}; //= "am-ip";
	final String[] AVM_MANAGEMENT_OUT_PORT_URI = {"a1m-op","a2m-op","a3m-op"}; //= "am-op";	
	final String[] AVM_REQUEST_SUBMISSION_IN_PORT_URI = {"a1rs-ip","a2rs-ip","a3rs-ip"}; //= "ars-ip";
	final String[] AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = {"a1rn-op","a2rn-op","a3rn-op"}; //= "arn-op";
	
	final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrs-ip";
	final String[] RD_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd1rs-op","rd2rs-op","rd3rs-op"}; //= "rdrs-op";
	final String[] RD_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd1rn-ip","rd2rn-ip","rd3rn-ip"}; //= "rdrn-ip";
	final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrn-op";		
	
	protected ComputerServicesOutboundPort csop;
	protected ComputerStaticStateDataOutboundPort cssdop;
	protected ComputerDynamicStateDataOutboundPort cdsdop;
	protected ApplicationManagementOutboundPort amop;
	protected ApplicationSubmissionInboundPort asip;
	protected ApplicationNotificationOutboundPort anop;
	
	protected DynamicComponentCreationOutboundPort[] portToApplicationVMJVM;
	protected DynamicComponentCreationOutboundPort[] portToRequestDispatcherJVM;
	
	protected ApplicationVMManagementOutboundPort[] avmOutPort;
	
	protected int totalAVMReserved;
	protected int totalApplicationAccepted;
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
	
		this.totalAVMReserved = 0;
		this.totalApplicationAccepted = 0;		
		
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
			for (int i = 0; i < this.totalAVMReserved; i++) {
				if (this.portToApplicationVMJVM[i].connected()) {
					this.portToApplicationVMJVM[i].doDisconnection();
				}
			}
			for (int i = 0; i < this.totalApplicationAccepted; i++) {
				if (this.portToRequestDispatcherJVM[i].connected()) {
					this.portToRequestDispatcherJVM[i].doDisconnection();
				}
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
	public void acceptApplicationSubmissionAndNotify(String appUri, int mustHaveCores) throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller checking for available resources to execute " + appUri + ".");
		}
		
		if (isResourcesAvailable(mustHaveCores)) {
			acceptApplication(appUri, mustHaveCores);
			this.anop.notifyApplicationAdmission(true);
			
		} else {
			rejectApplication(appUri);
			this.anop.notifyApplicationAdmission(false);
		}		
	}
	
	public boolean isResourcesAvailable(int mustHaveCores) {		
		int availableCores = 0;
		
		for (int p = 0; p < reservedCores.length; p++) {
			for (int c = 0; c < reservedCores[0].length; c++) {
				
				if (!this.reservedCores[p][c]) {
					availableCores++;
					
					if (availableCores == mustHaveCores) {
						return true;	
					}					
				}
			}
		}
			
		return false;
	}

	public void acceptApplication(String appUri, int mustHaveCores) throws Exception {
		
		this.logMessage("Admission controller allow application " + appUri + " to be executed.");
		
		final int AVM_TO_DEPLOY_COUNT = 3; 		
		deployComponents(appUri, AVM_TO_DEPLOY_COUNT);		
		this.logMessage("Admission controller deployed " + AVM_TO_DEPLOY_COUNT + " AVMs for " + appUri);
		
		allocateCores(mustHaveCores / AVM_TO_DEPLOY_COUNT, AVM_TO_DEPLOY_COUNT);	
		this.logMessage("Admission controller allocated " + mustHaveCores + " cores for " + appUri);
		
		totalApplicationAccepted++;
		totalAVMReserved += AVM_TO_DEPLOY_COUNT;
		
		//FIXME
		//assert mustHaveCores <= this.numberOfProcessors * this.numberOfCoresPerProcessor;				
	}
	
	public void allocateCores(int coresCount, int avmToDeploy) throws Exception {
		
		AllocatedCore[] ac = this.csop.allocateCores(coresCount);
		for (int i = 0; i < avmToDeploy; i++) {
			this.avmOutPort[i].allocateCores(ac);			
		}		
	}
	
	public void rejectApplication(String appUri) {
		
		this.logMessage("Admission controller can't accept application " + appUri + " because of lack of resources.");		
	}		
	
	public void deployComponents(String appUri, int applicationVMCount) throws Exception {						 			
		
		prepareDeployment();
		
		final String RD_URI = "rd-" + appUri;
		
		this.logMessage("Admission controller deploying components for " + appUri + "...");
		
		for (int i = 0; i < applicationVMCount; i++) {
			this.portToApplicationVMJVM[i].createComponent(
					ApplicationVM.class.getCanonicalName(),
					new Object[] {
							"vm" + i,
							AVM_MANAGEMENT_IN_PORT_URI[i],
						    AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
						    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i]
					});	
		}					
									
		this.portToRequestDispatcherJVM[0].createComponent( //FIXME NOT 0 !!!!
				RequestDispatcher.class.getCanonicalName(),
				new Object[] {
						RD_URI,							
						RD_REQUEST_SUBMISSION_IN_PORT_URI,
						RD_REQUEST_SUBMISSION_OUT_PORT_URI,
						RD_REQUEST_NOTIFICATION_IN_PORT_URI,
						RD_REQUEST_NOTIFICATION_OUT_PORT_URI
				});
				
		// --------------------------------------------------------------------
		this.addRequiredInterface(ApplicationVMManagementI.class);
		this.avmOutPort = new ApplicationVMManagementOutboundPort[applicationVMCount];
		
		for (int i = 0; i < applicationVMCount; i++) {						
			this.avmOutPort[i] = new ApplicationVMManagementOutboundPort(AVM_MANAGEMENT_OUT_PORT_URI[i], this);
			this.addPort(this.avmOutPort[i]);
			this.avmOutPort[i].publishPort();
			
			avmOutPort[i].doConnection(
					AVM_MANAGEMENT_IN_PORT_URI[i],
					ApplicationVMManagementConnector.class.getCanonicalName());			
		}		
		// --------------------------------------------------------------------
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();					
		
		rop.doConnection(RD_URI, ReflectionConnector.class.getCanonicalName());		
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		rop.toggleLogging();
		rop.toggleTracing();
		
		this.amop.doDynamicConnectionWithDispatcherForSubmission(RD_REQUEST_SUBMISSION_IN_PORT_URI);
		this.amop.doDynamicConnectionWithDispatcherForNotification(rop, RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
				
		for (int i = 0; i < applicationVMCount; i++) {
			rop.doPortConnection(
					RD_REQUEST_SUBMISSION_OUT_PORT_URI[i],
					AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
					Javassist.getRequestSubmissionConnectorClassName());					
		}		
		rop.doDisconnection();
		
		// --------------------------------------------------------------------
		for (int i = 0; i < applicationVMCount; i++) {
			rop.doConnection("vm" + i, ReflectionConnector.class.getCanonicalName());
	
			RequestDispatcher.DEBUG_LEVEL = 1;
			rop.toggleTracing();
			rop.toggleLogging();
			
			rop.doPortConnection(
					AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
					RD_REQUEST_NOTIFICATION_IN_PORT_URI[i],
					Javassist.getRequestNotificationConnectorClassName());
			
			rop.doDisconnection();
		}
	}
	
	public void prepareDeployment() throws Exception {
		//FIXME not 3 nor 1 !!!!
		this.portToApplicationVMJVM = new DynamicComponentCreationOutboundPort[3];
		this.portToRequestDispatcherJVM = new DynamicComponentCreationOutboundPort[1];
		
		for (int i = 0; i < 3; i++) {
			this.portToApplicationVMJVM[i] = new DynamicComponentCreationOutboundPort(this);
			this.portToApplicationVMJVM[i].localPublishPort();
			this.addPort(this.portToApplicationVMJVM[i]);
			this.portToApplicationVMJVM[i].doConnection(					
					this.APPLICATION_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
		}
		
		for (int i = 0; i < 1; i++) {
			this.portToRequestDispatcherJVM[i] = new DynamicComponentCreationOutboundPort(this);
			this.portToRequestDispatcherJVM[i].localPublishPort();
			this.addPort(this.portToRequestDispatcherJVM[i]);
			this.portToRequestDispatcherJVM[i].doConnection(					
					this.REQUEST_DISPATCHER_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
		}
	}
}
