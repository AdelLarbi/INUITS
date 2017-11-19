package fr.upmc.inuits.software.admissioncontroller;

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
	public static int ANALYSE_DATA_TIMER = 1000;
		
	final String[] AVM_MANAGEMENT_IN_PORT_URI;
	final String[] AVM_MANAGEMENT_OUT_PORT_URI;	
	final String[] AVM_REQUEST_SUBMISSION_IN_PORT_URI;
	final String[] AVM_REQUEST_NOTIFICATION_OUT_PORT_URI;
	
	final String[] RD_REQUEST_SUBMISSION_IN_PORT_URI;
	final String[] RD_REQUEST_SUBMISSION_OUT_PORT_URI;
	final String[] RD_REQUEST_NOTIFICATION_IN_PORT_URI;
	final String[] RD_REQUEST_NOTIFICATION_OUT_PORT_URI;
	
	protected final String APPLICATION_VM_JVM_URI = "";
	protected final String REQUEST_DISPATCHER_JVM_URI = "";
	
	protected ComputerServicesOutboundPort[] csop;
	protected ComputerStaticStateDataOutboundPort[] cssdop;
	protected ComputerDynamicStateDataOutboundPort[] cdsdop;
	protected ApplicationManagementOutboundPort[] amop;
	protected ApplicationSubmissionInboundPort[] asip;
	protected ApplicationNotificationOutboundPort[] anop;
	
	protected DynamicComponentCreationOutboundPort[] portToApplicationVMJVM;
	protected DynamicComponentCreationOutboundPort[] portToRequestDispatcherJVM;	
	protected ApplicationVMManagementOutboundPort[] avmOutPort;
	
	protected int shiftAVMIndex;
	protected int shiftRDIndex;	
	
	protected final int TOTAL_COMPUTERS_USED;
	protected final int TOTAL_APPLICATION_EXECUTION_REQUESTED;
	protected int totalAVMReserved;
	protected int totalApplicationAccepted;
	
	protected int numberOfProcessors;
	protected int numberOfCoresPerProcessor;	
	boolean[][] reservedCores;
		
	public AdmissionController(
			String[] computersURI,			
			String[] computerServicesOutboundPortURI,
			String[] computerStaticStateDataOutboundPortURI,
			String[] computerDynamicStateDataOutboundPortURI,
			String[] applicationManagementOutboundPortURI,
			String[] applicationSubmissionInboundPortURI,
			String[] applicationNotificationOutboundPortURI) throws Exception {
		
		super(1, 1);
		
		assert computersURI != null && computersURI.length > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.length > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.length > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.length > 0;		
		assert applicationManagementOutboundPortURI != null && applicationManagementOutboundPortURI.length > 0;
		assert applicationSubmissionInboundPortURI != null && applicationSubmissionInboundPortURI.length > 0;
		assert applicationNotificationOutboundPortURI != null && applicationNotificationOutboundPortURI.length > 0;
	
		this.shiftRDIndex = 0;
		this.shiftAVMIndex = 0;
			
		this.TOTAL_COMPUTERS_USED = computersURI.length;
		this.TOTAL_APPLICATION_EXECUTION_REQUESTED = applicationManagementOutboundPortURI.length;
		this.totalAVMReserved = 0;
		this.totalApplicationAccepted = 0;		
	
		this.AVM_MANAGEMENT_IN_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		this.AVM_MANAGEMENT_OUT_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];	
		this.AVM_REQUEST_SUBMISSION_IN_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		this.AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		
		this.RD_REQUEST_SUBMISSION_IN_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED];
		this.RD_REQUEST_SUBMISSION_OUT_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		this.RD_REQUEST_NOTIFICATION_IN_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		this.RD_REQUEST_NOTIFICATION_OUT_PORT_URI = new String[TOTAL_APPLICATION_EXECUTION_REQUESTED];		
		
		this.amop = new ApplicationManagementOutboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED];
		this.asip = new ApplicationSubmissionInboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED];
		this.anop = new ApplicationNotificationOutboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED];
		
		this.portToApplicationVMJVM = new DynamicComponentCreationOutboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		this.portToRequestDispatcherJVM = new DynamicComponentCreationOutboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED];
		
		this.csop = new ComputerServicesOutboundPort[TOTAL_COMPUTERS_USED];
		this.cssdop = new ComputerStaticStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		this.cdsdop = new ComputerDynamicStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		
		this.addRequiredInterface(ApplicationVMManagementI.class);
		this.avmOutPort = new ApplicationVMManagementOutboundPort[TOTAL_APPLICATION_EXECUTION_REQUESTED * 10];
		
		this.addRequiredInterface(ComputerServicesI.class);
		// this.addOfferedInterface(ComputerStaticStateDataI.class); or :
		this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.csop[i] = new ComputerServicesOutboundPort(computerServicesOutboundPortURI[i], this);
			this.addPort(this.csop[i]);
			this.csop[i].publishPort();			
					
			this.cssdop[i] = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI[i], this, computersURI[i]);
			this.addPort(this.cssdop[i]);
			this.cssdop[i].publishPort();
			
			this.cdsdop[i] = new ComputerDynamicStateDataOutboundPort(computerDynamicStateDataOutboundPortURI[i], this, computersURI[i]);
			this.addPort(this.cdsdop[i]);
			this.cdsdop[i].publishPort();	
		}		
				
		this.addRequiredInterface(ApplicationManagementI.class);
		this.addOfferedInterface(ApplicationSubmissionI.class);
		this.addRequiredInterface(ApplicationNotificationI.class);
		
		for (int i = 0; i < TOTAL_APPLICATION_EXECUTION_REQUESTED; i++) {
			this.amop[i] = new ApplicationManagementOutboundPort(applicationManagementOutboundPortURI[i], this);
			this.addPort(this.amop[i]);
			this.amop[i].publishPort();		
			
			this.asip[i] = new ApplicationSubmissionInboundPort(applicationSubmissionInboundPortURI[i], this);			
			this.addPort(this.asip[i]);
			this.asip[i].publishPort();		
			
			this.anop[i] = new ApplicationNotificationOutboundPort(applicationNotificationOutboundPortURI[i], this);
			this.addPort(this.anop[i]);
			this.anop[i].publishPort();	
		}													
		
		this.addRequiredInterface(DynamicComponentCreationI.class);			
		
		assert this.cssdop != null && this.cssdop[0] instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop[0] instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.amop != null && this.amop[0] instanceof ApplicationManagementI;
		assert this.asip != null && this.asip[0] instanceof ApplicationSubmissionI;
		assert this.anop != null && this.anop[0] instanceof ApplicationNotificationI;		
	}
	
	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
				
		try {									
			// start the pushing of dynamic state information from the computer;
			// here only one push of information is planned after one second.
			for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
				this.cdsdop[i].startUnlimitedPushing(ANALYSE_DATA_TIMER); 
			}
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
			for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
				if (this.csop[i].connected()) {
					this.csop[i].doDisconnection();
				}
				if (this.cssdop[i].connected()) {
					this.cssdop[i].doDisconnection();
				}
				if (this.cdsdop[i].connected()) {
					this.cdsdop[i].doDisconnection();
				}
			}			
			for (int i = 0; i < TOTAL_APPLICATION_EXECUTION_REQUESTED; i++) {
				if (this.amop[i].connected()) {
					this.amop[i].doDisconnection();
				}
				if (this.anop[i].connected()) {
					this.anop[i].doDisconnection();
				}
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
	public void acceptApplicationSubmissionAndNotify(String appUri, int appIndex, int mustHaveCores) throws Exception {
				
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller checking for available resources to execute " + appUri + ".");
		}
		
		synchronized (this) {			
			if (isResourcesAvailable(mustHaveCores)) {
				acceptApplication(appUri, appIndex, mustHaveCores);
				this.anop[appIndex].notifyApplicationAdmission(true);
				
			} else {
				rejectApplication(appUri);
				this.anop[appIndex].notifyApplicationAdmission(false);
			}
			
			// Necessary await time to update data when several applications runs simultaneously 
			wait(ANALYSE_DATA_TIMER);			
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

	public void acceptApplication(String appUri, int appIndex, int mustHaveCores) throws Exception {
		
		this.logMessage("Admission controller allow application " + appUri + " to be executed.");
		
		// This will change when we start part 2 and 3.
		final int AVM_TO_DEPLOY_COUNT = 3; 		
		deployComponents(appUri, appIndex, AVM_TO_DEPLOY_COUNT);		
		this.logMessage("Admission controller deployed " + AVM_TO_DEPLOY_COUNT + " AVMs for " + appUri);
		
		allocateCores(mustHaveCores, AVM_TO_DEPLOY_COUNT);		
		this.logMessage("Admission controller allocated " + mustHaveCores + " cores for " + appUri);
		
		totalApplicationAccepted++;
		totalAVMReserved += AVM_TO_DEPLOY_COUNT;					
	}
	
	// This is just the first version of cores allocation before starting part 2 and 3.
	public void allocateCores(int coresCount, int avmToDeploy) throws Exception {
				
		AllocatedCore[] ac = this.csop[0].allocateCores(coresCount);		
		
		for (int i = 0; i < avmToDeploy; i++) {
			int newAvmIndex = i + shiftAVMIndex;
			this.avmOutPort[newAvmIndex].allocateCores(ac);		
		}
		
		// Useful for next components deployment
		shiftAVMIndex += avmToDeploy;
		shiftRDIndex++;
	}
	
	public void rejectApplication(String appUri) {
		
		this.logMessage("Admission controller can't accept application " + appUri + " because of lack of resources.");		
	}		
	
	public void deployComponents(String appUri, int appIndex, int applicationVMCount) throws Exception {						 			
				
		prepareDeployment(applicationVMCount);		
						
		this.logMessage("Admission controller deploying components for " + appUri + "...");
		
		for (int i = 0; i < applicationVMCount; i++) {
			int newAvmIndex = i + shiftAVMIndex;
							
			final String AVM_URI = "avm-" + appIndex + "-" + newAvmIndex;
			
			this.portToApplicationVMJVM[newAvmIndex].createComponent(
					ApplicationVM.class.getCanonicalName(),
					new Object[] {
							AVM_URI,
							AVM_MANAGEMENT_IN_PORT_URI[newAvmIndex],
						    AVM_REQUEST_SUBMISSION_IN_PORT_URI[newAvmIndex],
						    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[newAvmIndex]
					});	
		}							
			
		final String RD_URI = "rd-" + appIndex + "-" + appUri;
		final String[] LOCAL_RD_REQUEST_SUBMISSION_OUT_PORT_URI = new String[applicationVMCount];
		final String[] LOCAL_RD_REQUEST_NOTIFICATION_IN_PORT_URI = new String[applicationVMCount];
		this.RD_REQUEST_SUBMISSION_IN_PORT_URI[appIndex] = "rrsip-" + appIndex;
		this.RD_REQUEST_NOTIFICATION_OUT_PORT_URI[appIndex] = "rrnop-" + appIndex;
		
		for (int i = 0; i < applicationVMCount; i++) {
			LOCAL_RD_REQUEST_SUBMISSION_OUT_PORT_URI[i] = RD_REQUEST_SUBMISSION_OUT_PORT_URI[i + shiftAVMIndex];
			LOCAL_RD_REQUEST_NOTIFICATION_IN_PORT_URI[i] = RD_REQUEST_NOTIFICATION_IN_PORT_URI[i + shiftAVMIndex];			
		}													
		
		this.portToRequestDispatcherJVM[shiftRDIndex].createComponent(
				RequestDispatcher.class.getCanonicalName(),
				new Object[] {
						RD_URI,							
						RD_REQUEST_SUBMISSION_IN_PORT_URI[appIndex],
						LOCAL_RD_REQUEST_SUBMISSION_OUT_PORT_URI,
						LOCAL_RD_REQUEST_NOTIFICATION_IN_PORT_URI,
						RD_REQUEST_NOTIFICATION_OUT_PORT_URI[appIndex]
				});
		
		// --------------------------------------------------------------------				
		for (int i = 0; i < applicationVMCount; i++) {		
			int newAvmIndex = i + shiftAVMIndex;
								
			this.avmOutPort[newAvmIndex] = new ApplicationVMManagementOutboundPort(AVM_MANAGEMENT_OUT_PORT_URI[newAvmIndex], this);			
			this.addPort(this.avmOutPort[newAvmIndex]);			
			this.avmOutPort[newAvmIndex].publishPort();			
			
			avmOutPort[newAvmIndex].doConnection(
					AVM_MANAGEMENT_IN_PORT_URI[newAvmIndex],
					ApplicationVMManagementConnector.class.getCanonicalName());			
		}
		
		// --------------------------------------------------------------------
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();					
		
		rop.doConnection(RD_URI, ReflectionConnector.class.getCanonicalName());		
		
		rop.toggleLogging();
		rop.toggleTracing();
		
		this.amop[appIndex].doDynamicConnectionWithDispatcherForSubmission(RD_REQUEST_SUBMISSION_IN_PORT_URI[appIndex]);
		this.amop[appIndex].doDynamicConnectionWithDispatcherForNotification(rop, RD_REQUEST_NOTIFICATION_OUT_PORT_URI[appIndex]);
				
		for (int i = 0; i < applicationVMCount; i++) {
			int newAvmIndex = i + shiftAVMIndex;
			
			rop.doPortConnection(
					RD_REQUEST_SUBMISSION_OUT_PORT_URI[newAvmIndex],
					AVM_REQUEST_SUBMISSION_IN_PORT_URI[newAvmIndex],
					Javassist.getRequestSubmissionConnectorClassName());					
		}		
		rop.doDisconnection();
		
		// --------------------------------------------------------------------
		for (int i = 0; i < applicationVMCount; i++) {
			int newAvmIndex = i + shiftAVMIndex;
			final String AVM_URI = "avm-" + appIndex + "-" + newAvmIndex;
			
			rop.doConnection(AVM_URI, ReflectionConnector.class.getCanonicalName());
	
			RequestDispatcher.DEBUG_LEVEL = 1;
			rop.toggleTracing();
			rop.toggleLogging();
			
			rop.doPortConnection(
					AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[newAvmIndex],
					RD_REQUEST_NOTIFICATION_IN_PORT_URI[newAvmIndex],
					Javassist.getRequestNotificationConnectorClassName());
			
			rop.doDisconnection();
		}			
	}
	
	public void prepareDeployment(int applicationVMCount) throws Exception {					
		for (int i = 0; i < applicationVMCount; i++) {
			int newAvmIndex = i + shiftAVMIndex;								
			
			this.portToApplicationVMJVM[newAvmIndex] = new DynamicComponentCreationOutboundPort(this);
			this.portToApplicationVMJVM[newAvmIndex].localPublishPort();
			this.addPort(this.portToApplicationVMJVM[newAvmIndex]);
			this.portToApplicationVMJVM[newAvmIndex].doConnection(					
					this.APPLICATION_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
			
			this.AVM_MANAGEMENT_IN_PORT_URI[newAvmIndex] = "amip-" + newAvmIndex;
			this.AVM_MANAGEMENT_OUT_PORT_URI[newAvmIndex] = "amop-" + newAvmIndex;	
			this.AVM_REQUEST_SUBMISSION_IN_PORT_URI[newAvmIndex] = "arsip-" + newAvmIndex;
			this.AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[newAvmIndex] = "arnop-" + newAvmIndex;						
			this.RD_REQUEST_SUBMISSION_OUT_PORT_URI[newAvmIndex] = "rrsop-" + newAvmIndex;
			this.RD_REQUEST_NOTIFICATION_IN_PORT_URI[newAvmIndex] = "rrnip-" + newAvmIndex;
		}
			
		this.portToRequestDispatcherJVM[shiftRDIndex] = new DynamicComponentCreationOutboundPort(this);
		this.portToRequestDispatcherJVM[shiftRDIndex].localPublishPort();
		this.addPort(this.portToRequestDispatcherJVM[shiftRDIndex]);
		this.portToRequestDispatcherJVM[shiftRDIndex].doConnection(					
				this.REQUEST_DISPATCHER_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
	}
}