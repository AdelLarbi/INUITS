package fr.upmc.inuits.software.admissioncontroller;

import java.util.ArrayList;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
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
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenterclient.utils.TimeProcessing;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class AdmissionController 
	extends AbstractComponent 
	implements ComputerStateDataConsumerI, RequestSubmissionHandlerI {
	
	public static int DEBUG_LEVEL = 1;	
	
	protected ComputerServicesOutboundPort csop;
	protected ComputerStaticStateDataOutboundPort cssdop;
	protected ComputerDynamicStateDataOutboundPort cdsdop;
	protected RequestSubmissionInboundPort rsip;
	protected RequestNotificationOutboundPort rnop;
	
	protected ApplicationVMManagementOutboundPort avmOutPort;
	
	protected String acRequestSubmissionOutPortUri;
	protected String acRequestNotificationInPortUri;
	
	boolean[][] reservedCores;
	
	public AdmissionController(
			ArrayList<String> computersURI,			
			String computerServicesOutboundPortURI,
			String computerStaticStateDataOutboundPortURI,
			String computerDynamicStateDataOutboundPortURI,
			String requestSubmissionInboundPortURI,
			String requestNotificationOutboundPortURI) throws Exception {
		
		super(1, 1);

		assert computersURI != null && computersURI.size() > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.length() > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.length() > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.length() > 0;
		assert requestSubmissionInboundPortURI != null && requestSubmissionInboundPortURI.length() > 0;
		assert requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0;				
		
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
		
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(requestSubmissionInboundPortURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		
		this.acRequestSubmissionOutPortUri = computersURI + "-acrs-op";
		this.acRequestNotificationInPortUri = computersURI + "-acrn-ip";
		
		this.reservedCores = new boolean[0][0];
		
		assert this.cssdop != null && this.cssdop instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;
		assert this.acRequestSubmissionOutPortUri != null;
		assert this.acRequestNotificationInPortUri != null;
		assert this.reservedCores != null;
	}
	
	@Override
	public void start() throws ComponentStartException {
		
		super.start();

		// start the pushing of dynamic state information from the computer;
		// here only one push of information is planned after one second.
		try {
			this.cdsdop.startLimitedPushing(1000, 25);
			//this.cdsdop.startUnlimitedPushing(1000);
			
		} catch (Exception e) {
			throw new ComponentStartException("Unable to start pushing dynamic data from the computer component.", e);
		}
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
			if (this.rnop.connected()) {
				this.rnop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}

	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
		
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
		
		this.reservedCores = currentDynamicState.getCurrentCoreReservations();
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		/*if (AdmissionController.DEBUG_LEVEL == 1) {			
			this.logMessage(				
					"Admission controller submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}*/					
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Admission controller submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		if (isResourcesAvailable()) {			
			acceptRequest(r);
			
		} else {
			rejectRequest(r);
		}
		
		//this.rnop.notifyRequestTermination(r);		
	}
	
	public boolean isResourcesAvailable() {
		
		for (int p = 0; p < reservedCores.length; p++) {
			for (int c = 0; c < reservedCores[0].length; c++) {
				if (this.reservedCores[p][c]) {
					//TODO
				}
			}
		}
		
		return true;
	}
	
	public void acceptRequest(RequestI r) throws Exception {
		
		deploy();
		
		AllocatedCore[] ac = this.csop.allocateCores(4);
		this.avmOutPort.allocateCores(ac);
	}
	
	public void deploy() throws Exception {
		
		final String AVM_MANAGEMENT_IN_PORT_URI = "am-ip";
		final String AVM_MANAGEMENT_OUT_PORT_URI = "am-op";	
		final String AVM_REQUEST_SUBMISSION_IN_PORT_URI = "ars-ip";
		final String AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = "arn-op";
		
		final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrs-ip";
		final String RD_REQUEST_SUBMISSION_OUT_PORT_URI = "rdrs-op";
		final String RD_REQUEST_NOTIFICATION_IN_PORT_URI = "rdrn-ip";
		final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrn-op";
		 
		ApplicationVM applicationVM;
		RequestDispatcher requestDispatcher;			
		// --------------------------------------------------------------------		
		applicationVM = new ApplicationVM(
				"vm0",
				AVM_MANAGEMENT_IN_PORT_URI,
			    AVM_REQUEST_SUBMISSION_IN_PORT_URI,
			    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
						
		AbstractCVM.theCVM.addDeployedComponent(applicationVM);
				
		applicationVM.toggleTracing();
		applicationVM.toggleLogging();
		
		this.avmOutPort = new ApplicationVMManagementOutboundPort(
						AVM_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avmOutPort.publishPort();
		this.avmOutPort.doConnection(
				AVM_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());			
		// --------------------------------------------------------------------
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
		
		this.doPortConnection(
				acRequestSubmissionOutPortUri,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		requestDispatcher.doPortConnection(
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				acRequestNotificationInPortUri,
				RequestSubmissionConnector.class.getCanonicalName());
		
		requestDispatcher.doPortConnection(
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				AVM_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		applicationVM.doPortConnection(
				AVM_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestNotificationConnector.class.getCanonicalName());
	}
	
	public void rejectRequest(RequestI r) {
		this.logMessage("Admission controller can't hundle request " + r.getRequestURI() + " because of lack of resources");
	}
	
	/*@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {			
			this.logMessage("Admission controller notifying request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
				
		this.rnop.notifyRequestTermination(r);
	}*/
}
