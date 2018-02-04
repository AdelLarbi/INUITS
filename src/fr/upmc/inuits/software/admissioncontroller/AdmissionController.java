package fr.upmc.inuits.software.admissioncontroller;

import java.util.ArrayList;
import java.util.HashMap;

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
import fr.upmc.inuits.software.autonomiccontroller.AutonomicController;
import fr.upmc.inuits.software.autonomiccontroller.connectors.AutonomicControllerCoordinationConnector;
import fr.upmc.inuits.software.autonomiccontroller.connectors.AutonomicControllerManagementConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerAVMsManagementInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerCoordinationInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerCoordinationOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerManagementOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;
import fr.upmc.inuits.software.requestdispatcher.connector.RequestDispatcherManagementConnector;
import fr.upmc.inuits.software.requestdispatcher.connector.RequestDispatcherManagementNotificationConnector;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationHandlerI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherManagementNotificationInboundPort;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherManagementOutboundPort;
import fr.upmc.inuits.utils.Javassist;
/**
 * La class <code>AdmissionController</code> represente le composant AdmissionController qui gère l'acces aux ressources du centre de calcul par les applications .
 */
public class AdmissionController 
	extends AbstractComponent 
	implements ComputerStateDataConsumerI, ApplicationSubmissionHandlerI, 
		RequestDispatcherManagementNotificationHandlerI, AutonomicControllerAVMsManagementHandlerI, 
		AutonomicControllerCoordinationHandlerI {
	
	public static int DEBUG_LEVEL = 1;
	
	protected final int ANALYSE_DATA_TIMER = 1000; //1000			
	protected final int AVMS_TO_ALLOCATE_COUNT = 2;
	protected final int MAXIMUM_AVMS_TO_ALLOCATE_COUNT = 20;
		
	protected final String AC_URI;
	
	protected final ArrayList<String> COMPUTERS_URI;
	protected final ArrayList<String> COMPUTER_SERVICES_IN_BOUND_PORT_URI;
	protected final ArrayList<String> COMPUTER_SERVICES_OUT_BOUND_PORT_URI;
	protected final ArrayList<String> COMPUTER_STATIC_STATE_DATA_IN_BOUND_PORT_URI;
	protected final ArrayList<String> COMPUTER_STATIC_STATE_DATA_OUT_BOUND_PORT_URI;
	protected final ArrayList<String> COMPUTER_DYNAMIC_STATE_DATA_IN_BOUND_PORT_URI;
	protected final ArrayList<String> COMPUTER_DYNAMIC_STATE_DATA_OUT_BOUND_PORT_URI;
	
	// Data coordination : Send & Receive
	protected final String ADMISSION_CONTROLLER_COORDINATION_IN_BOUND_PORT_URI;
	protected final String ADMISSION_CONTROLLER_COORDINATION_OUT_BOUND_PORT_URI;	
		
	protected HashMap<String,String> avmManagementInPortUri;
	protected HashMap<String,String> avmManagementOutPortUri;	
	protected HashMap<String,String> avmRequestSubmissionInPortUri;
	protected HashMap<String,String> avmRequestNotificationOutPortUri;
	// Association between applications and their AVM index count.
	protected HashMap<String, Integer> avmIndexPerApp;
	// Association between applications and their cores at the beginning.
	protected HashMap<String, AllocatedCore[]> mustHaveCoresPerApp;
		
	protected HashMap<String,String> rdRequestSubmissionInPortUri;
	protected HashMap<String,ArrayList<String>> rdRequestSubmissionOutPortUri;
	protected HashMap<String,ArrayList<String>> rdRequestNotificationInPortUri;
	protected HashMap<String,String> rdRequestNotificationOutPortUri;
	
	protected final HashMap<String,String> atcAvmsManagementInPortUri;
	
	protected final String APPLICATION_VM_JVM_URI = "";
	protected final String REQUEST_DISPATCHER_JVM_URI = "";
	protected final String AUTONOMIC_CONTROLLER_JVM_URI = "";
	
	protected ComputerServicesOutboundPort[] csop;
	protected ComputerStaticStateDataOutboundPort[] cssdop;
	protected ComputerDynamicStateDataOutboundPort[] cdsdop;
	protected HashMap<String,ApplicationManagementOutboundPort> amop;
	protected HashMap<String,ApplicationSubmissionInboundPort> asip;
	protected HashMap<String,ApplicationNotificationOutboundPort> anop;
	protected HashMap<String,AutonomicControllerAVMsManagementInboundPort> atcamip;
	protected HashMap<String,RequestDispatcherManagementOutboundPort> rdmop;
	protected HashMap<String,RequestDispatcherManagementNotificationInboundPort> rdmnip;	
	
	protected HashMap<String,DynamicComponentCreationOutboundPort> portToApplicationVMJVM;	
	protected HashMap<String,DynamicComponentCreationOutboundPort> portToRequestDispatcherJVM;
	protected HashMap<String,DynamicComponentCreationOutboundPort> portToAutonomicControllerJVM;
	protected HashMap<String,AutonomicControllerManagementOutboundPort> atcmOutPort;
	protected HashMap<String,ApplicationVMManagementOutboundPort> avmOutPort;
	
	protected AutonomicControllerCoordinationInboundPort atccip;
	protected AutonomicControllerCoordinationOutboundPort atccop;
	
	protected final int TOTAL_COMPUTERS_USED;
	protected final int TOTAL_APPLICATION_EXECUTION_REQUESTED;
	protected int computerIndex; 
	// useful for coordination
	protected int totalApplicationAccepted;
	// Useful for coordination (save tail)	
	protected String savedLastAutonomicControllerCoordinationInPortUri;
	// List of AVM's ids created at the beginning by the admission controller
	protected ArrayList<String> availableAVMsList;
	
	protected int numberOfProcessors;
	protected int numberOfCoresPerProcessor;	
	protected boolean[][] reservedCores;
		/**
		 * Permet la creation du composant AdmissionController
		 * @param acURI uri de l'admission controller
		 * @param computersURI uri du computer 
		 * @param computerServicesInboundPortURI uri du port computerServicesInboundPortURI
		 * @param computerServicesOutboundPortURI uri du port computerStaticStateDataInboundPortURI
		 * @param computerStaticStateDataInboundPortURI uri du port computerStaticStateDataInboundPortURI
		 * @param computerStaticStateDataOutboundPortURI uri du port computerStaticStateDataOutboundPortURI
		 * @param computerDynamicStateDataInboundPortURI uri du port computerDynamicStateDataInboundPortURI
		 * @param computerDynamicStateDataOutboundPortURI uri du port computerDynamicStateDataOutboundPortURI
		 * @param appsURI uri de l'application
		 * @param applicationManagementOutboundPortURI uri du port applicationManagementOutboundPortURI
		 * @param applicationSubmissionInboundPortURI uri du port applicationSubmissionInboundPortURI
		 * @param applicationNotificationOutboundPortURI uri du port applicationNotificationOutboundPortURI
		 * @param autonomicControllerAVMsManagementInboundPortURI uri du port autonomicControllerAVMsManagementInboundPortURI
		 * @param autonomicControllerCoordinationInboundPortURI uri du port autonomicControllerCoordinationInboundPortURI
		 * @param autonomicControllerCoordinationOutboundPortURI uri du port autonomicControllerCoordinationOutboundPortURI
		 * @throws Exception
		 */
	public AdmissionController(
			String acURI,	
			ArrayList<String> computersURI,			
			ArrayList<String> computerServicesInboundPortURI,
			ArrayList<String> computerServicesOutboundPortURI,
			ArrayList<String> computerStaticStateDataInboundPortURI,
			ArrayList<String> computerStaticStateDataOutboundPortURI,
			ArrayList<String> computerDynamicStateDataInboundPortURI,
			ArrayList<String> computerDynamicStateDataOutboundPortURI,
			ArrayList<String> appsURI,			
			ArrayList<String> applicationManagementOutboundPortURI,
			ArrayList<String> applicationSubmissionInboundPortURI,
			ArrayList<String> applicationNotificationOutboundPortURI,
			ArrayList<String> autonomicControllerAVMsManagementInboundPortURI,
			String autonomicControllerCoordinationInboundPortURI,
			String autonomicControllerCoordinationOutboundPortURI) throws Exception {
		
		super(1, 1);
		
		assert acURI != null;
		assert computersURI != null && computersURI.size() > 0;
		assert appsURI != null;
		assert computerServicesInboundPortURI != null && computerServicesInboundPortURI.size() > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.size() > 0;
		assert computerStaticStateDataInboundPortURI != null && computerStaticStateDataInboundPortURI.size() > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.size() > 0;
		assert computerDynamicStateDataInboundPortURI != null && computerDynamicStateDataInboundPortURI.size() > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.size() > 0;
		assert applicationManagementOutboundPortURI != null && applicationManagementOutboundPortURI.size() > 0;
		assert applicationSubmissionInboundPortURI != null && applicationSubmissionInboundPortURI.size() > 0;
		assert applicationNotificationOutboundPortURI != null && applicationNotificationOutboundPortURI.size() > 0;
		assert autonomicControllerAVMsManagementInboundPortURI != null 
				&& autonomicControllerAVMsManagementInboundPortURI.size() > 0;
		assert autonomicControllerCoordinationInboundPortURI != null;
		assert autonomicControllerCoordinationOutboundPortURI != null;
					
		this.AC_URI = acURI;
		this.COMPUTERS_URI = computersURI;
		this.COMPUTER_SERVICES_IN_BOUND_PORT_URI = computerServicesInboundPortURI;
		this.COMPUTER_SERVICES_OUT_BOUND_PORT_URI = computerServicesOutboundPortURI;
		this.COMPUTER_STATIC_STATE_DATA_IN_BOUND_PORT_URI = computerStaticStateDataInboundPortURI;
		this.COMPUTER_STATIC_STATE_DATA_OUT_BOUND_PORT_URI = computerStaticStateDataOutboundPortURI;
		this.COMPUTER_DYNAMIC_STATE_DATA_IN_BOUND_PORT_URI = computerDynamicStateDataInboundPortURI;
		this.COMPUTER_DYNAMIC_STATE_DATA_OUT_BOUND_PORT_URI = computerDynamicStateDataOutboundPortURI;
		this.ADMISSION_CONTROLLER_COORDINATION_IN_BOUND_PORT_URI = autonomicControllerCoordinationInboundPortURI;
		this.ADMISSION_CONTROLLER_COORDINATION_OUT_BOUND_PORT_URI = autonomicControllerCoordinationOutboundPortURI;		
		
		this.TOTAL_COMPUTERS_USED = computersURI.size();
		this.TOTAL_APPLICATION_EXECUTION_REQUESTED = appsURI.size();			
		this.computerIndex = 0;
		
		this.totalApplicationAccepted = 0;		
		this.availableAVMsList = new ArrayList<>();
		for (int i = 0; i < this.MAXIMUM_AVMS_TO_ALLOCATE_COUNT; i++) {
			availableAVMsList.add("added-avm-" + i);
		}
		
		this.avmManagementInPortUri = new HashMap<>();
		this.avmManagementOutPortUri = new HashMap<>();	
		this.avmRequestSubmissionInPortUri = new HashMap<>();
		this.avmRequestNotificationOutPortUri = new HashMap<>();
		this.avmIndexPerApp = new HashMap<>();
		this.mustHaveCoresPerApp = new HashMap<>();
						
		//this.rdManagementInPortUri = new HashMap<>();
		this.rdRequestSubmissionInPortUri = new HashMap<>();
		this.rdRequestSubmissionOutPortUri = new HashMap<>();
		this.rdRequestNotificationInPortUri = new HashMap<>();
		this.rdRequestNotificationOutPortUri = new HashMap<>();
		
		this.atcAvmsManagementInPortUri = new HashMap<>();
		
		this.amop = new HashMap<>();
		this.asip = new HashMap<>();
		this.anop = new HashMap<>();
		this.atcamip = new HashMap<>();
		this.rdmop = new HashMap<>();
		this.rdmnip = new HashMap<>();		
		
		this.portToApplicationVMJVM = new HashMap<>();		
		this.portToRequestDispatcherJVM = new HashMap<>();
		this.portToAutonomicControllerJVM = new HashMap<>();		
		
		this.csop = new ComputerServicesOutboundPort[TOTAL_COMPUTERS_USED];
		this.cssdop = new ComputerStaticStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		this.cdsdop = new ComputerDynamicStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		
		// To manage the AtC component (connect to different other components).
		this.addRequiredInterface(AutonomicControllerManagementI.class);
		this.atcmOutPort = new HashMap<>();
		
		// To manage the AVM component (allocate cores).
		this.addRequiredInterface(ApplicationVMManagementI.class);
		this.avmOutPort = new HashMap<>();
		
		// To manage the RD component (create RequestSubmission, RequestNotification ports).
		this.addRequiredInterface(RequestDispatcherManagementI.class);
		
		// To manage the RD component notification when (create RequestSubmission, RequestNotification ports).
		this.addOfferedInterface(RequestDispatcherManagementNotificationI.class);
				
		this.addRequiredInterface(ComputerServicesI.class);
		// this.addOfferedInterface(ComputerStaticStateDataI.class); or :
		this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);		
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.csop[i] = new ComputerServicesOutboundPort(computerServicesOutboundPortURI.get(i), this);
			this.addPort(this.csop[i]);
			this.csop[i].publishPort();			
					
			this.cssdop[i] = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI.get(i), this, computersURI.get(i));
			this.addPort(this.cssdop[i]);
			this.cssdop[i].publishPort();
			
			this.cdsdop[i] = new ComputerDynamicStateDataOutboundPort(computerDynamicStateDataOutboundPortURI.get(i), this, computersURI.get(i));
			this.addPort(this.cdsdop[i]);
			this.cdsdop[i].publishPort();
		}		
				
		this.addRequiredInterface(ApplicationManagementI.class);
		this.addOfferedInterface(ApplicationSubmissionI.class);
		this.addRequiredInterface(ApplicationNotificationI.class);
		this.addRequiredInterface(AutonomicControllerAVMsManagementI.class);		
		
		for (int i = 0; i < TOTAL_APPLICATION_EXECUTION_REQUESTED; i++) {
			this.amop.put(appsURI.get(i), 
					new ApplicationManagementOutboundPort(applicationManagementOutboundPortURI.get(i), this));
			this.addPort(this.amop.get(appsURI.get(i)));
			this.amop.get(appsURI.get(i)).publishPort();		
			
			this.asip.put(appsURI.get(i), 
					new ApplicationSubmissionInboundPort(applicationSubmissionInboundPortURI.get(i), this));			
			this.addPort(this.asip.get(appsURI.get(i)));
			this.asip.get(appsURI.get(i)).publishPort();		
			
			this.anop.put(appsURI.get(i), 
					new ApplicationNotificationOutboundPort(applicationNotificationOutboundPortURI.get(i), this));
			this.addPort(this.anop.get(appsURI.get(i)));
			this.anop.get(appsURI.get(i)).publishPort();
						
			this.atcamip.put(appsURI.get(i), 
					new AutonomicControllerAVMsManagementInboundPort(autonomicControllerAVMsManagementInboundPortURI.get(i), this));
			this.addPort(this.atcamip.get(appsURI.get(i)));
			this.atcamip.get(appsURI.get(i)).publishPort();									
			
			// Initialize each application with -1 as index for AVMs allocated.
			this.avmIndexPerApp.put(appsURI.get(i), -1);
			
			// Useful for autonomic controller AVMs management (Add AVM, Remove AVM..)
			this.atcAvmsManagementInPortUri.put(appsURI.get(i), autonomicControllerAVMsManagementInboundPortURI.get(i));
		}													
		
		this.addRequiredInterface(DynamicComponentCreationI.class);									
		
		// To receive coordination data
		this.addOfferedInterface(AutonomicControllerCoordinationI.class);
		this.atccip = new AutonomicControllerCoordinationInboundPort(autonomicControllerCoordinationInboundPortURI, this);
		this.addPort(this.atccip);
		this.atccip.publishPort();
		
		// To send coordination data 
		this.atccop = new AutonomicControllerCoordinationOutboundPort(autonomicControllerCoordinationOutboundPortURI, this);
		this.addPort(this.atccop);
		this.atccop.publishPort();
		
		assert this.cssdop != null && this.cssdop[0] instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop[0] instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.amop != null && this.amop.get(appsURI.get(0)) instanceof ApplicationManagementI;
		assert this.asip != null && this.asip.get(appsURI.get(0)) instanceof ApplicationSubmissionI;
		assert this.anop != null && this.anop.get(appsURI.get(0)) instanceof ApplicationNotificationI;
		assert this.atcamip != null && this.atcamip.get(appsURI.get(0)) instanceof AutonomicControllerAVMsManagementI;
		assert this.atccop != null && this.atccop instanceof AutonomicControllerCoordinationI;
		assert this.atccip != null && this.atccip instanceof AutonomicControllerCoordinationI;
	}
	
	/**
	 * initialise la reservation des corps
	 * @see fr.upmc.components.AbstractComponent#start()
	 * 
	 * pre pas de preCondition
	 * post this.reservedCores != null
	 */
	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
				
		try {									
			// start the pushing of dynamic state information from the computer.
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
	
	/**
	 * Methode permettant l'arrêt du composant AdmissionController, en déconnectant les différents ports.
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre	true				// pas plus de  preconditions.
	 * post	true				// pas plus de postconditions.
	 * </pre>
	 * 
	 * @see fr.upmc.components.AbstractComponent#shutdown()
	 * @throws ComponentShutdownException capture toute erreurs liée à la déconnexion
	 */
	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {
			for (ApplicationManagementOutboundPort thisAmop : amop.values()) {
				if (thisAmop.connected()) {
					thisAmop.doDisconnection();
				}
			}
			for (ApplicationNotificationOutboundPort thisAnop : anop.values()) {
				if (thisAnop.connected()) {
					thisAnop.doDisconnection();
				}
			}
			for (DynamicComponentCreationOutboundPort thisPortToApplicationVMJVM : portToApplicationVMJVM.values()) {
				if (thisPortToApplicationVMJVM.connected()) {
					thisPortToApplicationVMJVM.doDisconnection();
				}
			}
			for (DynamicComponentCreationOutboundPort thisPortToRequestDispatcherJVM : portToRequestDispatcherJVM.values()) {
				if (thisPortToRequestDispatcherJVM.connected()) {
					thisPortToRequestDispatcherJVM.doDisconnection();
				}
			}
			for (DynamicComponentCreationOutboundPort thisPortToAutonomicControllerJVM : portToAutonomicControllerJVM.values()) {
				if (thisPortToAutonomicControllerJVM.connected()) {
					thisPortToAutonomicControllerJVM.doDisconnection();
				}
			}			
			if (this.atccop.connected()) {
				this.atccop.doDisconnection();
			}	
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}

	/**
	 * permet d'afficher les données static.
	 */
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

	/**
	 * permet d'afficher les données dynamiques.
	 */
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

	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionHandlerI#acceptApplicationSubmissionAndNotify(String, int)
	 */
	@Override
	public void acceptApplicationSubmissionAndNotify(String appUri, int mustHaveCores) throws Exception {
				
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller checking for available resources to execute " + appUri + ".");
		}
		
		synchronized (this) {			
			if (isResourcesAvailable(mustHaveCores)) {
				acceptApplication(appUri, mustHaveCores);
				totalApplicationAccepted++;
				this.anop.get(appUri).notifyApplicationAdmission(true);
				
			} else {
				rejectApplication(appUri);
				this.anop.get(appUri).notifyApplicationAdmission(false);
			}
			
			// Necessary await time to update data when several applications runs simultaneously 
			wait(ANALYSE_DATA_TIMER);			
		}					
	}
	/**
	 * Permet de verifier que le nombre de core demandée par l'application pour sont éxécution est disponible.
	 * @param mustHaveCores nombre de core dont a besoin l'application pour s'executer.
	 * @return un booleen true s'il y a assez de core pour l'application
	 */
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

	/**
	 * Si l'application est est accepter on deploie les composant et on alloue les corps
	 * @param appUri
	 * @param mustHaveCores
	 * @throws Exception
	 */
	public void acceptApplication(String appUri, int mustHaveCores) throws Exception {
		
		this.logMessage("Admission controller allow application " + appUri + " to be executed.");
				
		deployComponents(appUri, AVMS_TO_ALLOCATE_COUNT);		
		this.logMessage("Admission controller deployed " + AVMS_TO_ALLOCATE_COUNT + " AVMs for " + appUri);
		
		allocateCores(appUri, mustHaveCores);		
		this.logMessage("Admission controller allocated " + mustHaveCores + " cores for " + appUri);						
	}
	
	/**
	 * Alloue les cores
	 * @param appUri
	 * @param coresCount
	 * @throws Exception
	 */
	public void allocateCores(String appUri, int coresCount) throws Exception {
				
		this.logMessage("Admission controller allocating " + coresCount + " for " + appUri + "...");
		
		AllocatedCore[] ac = this.csop[computerIndex].allocateCores(coresCount);		
		
		for (int i = 0; i < AVMS_TO_ALLOCATE_COUNT; i++) {
			this.avmOutPort.get(appUri + i).allocateCores(ac);		
		}
		
		this.mustHaveCoresPerApp.put(appUri, ac);
	}
	
	/**
	 * Message pour afficher qu'une application est rejetée par manque de ressource.
	 * @param appUri
	 */
	public void rejectApplication(String appUri) {
		
		this.logMessage("Admission controller can't accept application " + appUri + " because of lack of resources.");		
	}		
	/**
	 * Permet de deployer les composents AVM et Dispatcher .
	 * @param appUri uri de l'app
	 * @param applicationVMCount nombre d'avm
	 * @throws Exception 
	 */
	public void deployComponents(String appUri, int applicationVMCount) throws Exception {
				
		final String RD_URI = "rd-" + appUri;
		final String ATC_URI = "atc-" + appUri;
		
		prepareDeployment(appUri, RD_URI, ATC_URI, applicationVMCount);		
						
		this.logMessage("Admission controller deploying components for " + appUri + "...");
		
		for (int i = 0; i < applicationVMCount; i++) {
			final String AVM_URI = "avm-" + i + "-" + appUri;
			
			this.portToApplicationVMJVM.get(appUri + i).createComponent(
					ApplicationVM.class.getCanonicalName(),
					new Object[] {
							AVM_URI,
							avmManagementInPortUri.get(appUri + i),
						    avmRequestSubmissionInPortUri.get(appUri + i),
						    avmRequestNotificationOutPortUri.get(appUri + i)
					});	
		}	
		
		// --------------------------------------------------------------------				
		final String RD_MANAGEMENT_IN_PORT_URI = RD_URI + "-mip";
		final String RD_MANAGEMENT_NOTIFICATION_OUT_PORT_URI = RD_URI + "-mnop";
		this.rdRequestSubmissionInPortUri.put(RD_URI, RD_URI + "-rrsip");
		this.rdRequestNotificationOutPortUri.put(RD_URI, RD_URI + "-rrnop");
		final String RD_DYNAMIC_STATE_DATA_IN_PORT_URI = RD_URI + "-dsd-ip";
				
		this.portToRequestDispatcherJVM.get(RD_URI).createComponent(
				RequestDispatcher.class.getCanonicalName(),
				new Object[] {
						RD_URI,							
						RD_MANAGEMENT_IN_PORT_URI,
						RD_MANAGEMENT_NOTIFICATION_OUT_PORT_URI,
						appUri,
						rdRequestSubmissionInPortUri.get(RD_URI),
						rdRequestSubmissionOutPortUri.get(RD_URI),
						rdRequestNotificationInPortUri.get(RD_URI),
						rdRequestNotificationOutPortUri.get(RD_URI),
						RD_DYNAMIC_STATE_DATA_IN_PORT_URI
				});
				
		// --------------------------------------------------------------------			
		// Create a ports to manage the RD component (create RequestSubmission, create RequestNotification ports).		
		final String RD_MANAGEMENT_OUT_PORT_URI = RD_URI + "-mop";
		final String RD_MANAGEMENT_NOTIFICATION_IN_PORT_URI = RD_URI + "-mnip";
		
		this.rdmop.put(RD_URI, 
				new RequestDispatcherManagementOutboundPort(RD_MANAGEMENT_OUT_PORT_URI, this));
		this.addPort(this.rdmop.get(RD_URI));
		this.rdmop.get(RD_URI).publishPort();						
					
		this.rdmop.get(RD_URI).doConnection(
				RD_MANAGEMENT_IN_PORT_URI,
				RequestDispatcherManagementConnector.class.getCanonicalName());
		
		this.rdmnip.put(RD_URI, 
				new RequestDispatcherManagementNotificationInboundPort(RD_MANAGEMENT_NOTIFICATION_IN_PORT_URI, this));
		this.addPort(this.rdmnip.get(RD_URI));
		this.rdmnip.get(RD_URI).publishPort();
		
		// --------------------------------------------------------------------				
		final String RD_DYNAMIC_STATE_DATA_OUT_PORT_URI = RD_URI + "-dsd-op";
		final String ATC_MANAGEMENT_IN_PORT_URI = ATC_URI + "-m-ip";
		final String ATC_AVMS_MANAGEMENT_OUT_PORT_URI = ATC_URI + "-am-op";
		final String AUTONOMIC_CONTROLLER_COORDINATION_OUT_PORT_URI = ATC_URI + "-c-op";
		final String AUTONOMIC_CONTROLLER_COORDINATION_IN_PORT_URI = ATC_URI + "-c-ip";		
		
		this.portToAutonomicControllerJVM.get(ATC_URI).createComponent(
				AutonomicController.class.getCanonicalName(),
				new Object[] {
						ATC_URI,
						this.COMPUTERS_URI,			
						this.COMPUTER_SERVICES_OUT_BOUND_PORT_URI,
						this.COMPUTER_STATIC_STATE_DATA_OUT_BOUND_PORT_URI,
						this.COMPUTER_DYNAMIC_STATE_DATA_OUT_BOUND_PORT_URI,
						appUri,
						RD_URI, 
						RD_DYNAMIC_STATE_DATA_OUT_PORT_URI,
						ATC_MANAGEMENT_IN_PORT_URI,
						ATC_AVMS_MANAGEMENT_OUT_PORT_URI,
						AUTONOMIC_CONTROLLER_COORDINATION_OUT_PORT_URI,
						AUTONOMIC_CONTROLLER_COORDINATION_IN_PORT_URI
				});
				
		// --------------------------------------------------------------------
		final String ATC_MANAGEMENT_OUT_PORT_URI = ATC_URI + "-m-op";
		
		this.atcmOutPort.put(ATC_URI, new AutonomicControllerManagementOutboundPort(ATC_MANAGEMENT_OUT_PORT_URI, this));		
		this.addPort(this.atcmOutPort.get(ATC_URI));
		this.atcmOutPort.get(ATC_URI).publishPort();
		
		this.atcmOutPort.get(ATC_URI).doConnection(
				ATC_MANAGEMENT_IN_PORT_URI,
				AutonomicControllerManagementConnector.class.getCanonicalName());			
				
		// --------------------------------------------------------------------				
		for (int i = 0; i < applicationVMCount; i++) {
			this.avmOutPort.put(appUri + i, new ApplicationVMManagementOutboundPort(avmManagementOutPortUri.get(appUri + i), this));			
			this.addPort(this.avmOutPort.get(appUri + i));			
			this.avmOutPort.get(appUri + i).publishPort();			
			
			this.avmOutPort.get(appUri + i).doConnection(
					avmManagementInPortUri.get(appUri + i),
					ApplicationVMManagementConnector.class.getCanonicalName());			
		}
		
		// --------------------------------------------------------------------
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();					
		
		rop.doConnection(RD_URI, ReflectionConnector.class.getCanonicalName());		
		
		rop.toggleLogging();
		rop.toggleTracing();
	
		rop.doPortConnection(
				RD_MANAGEMENT_NOTIFICATION_OUT_PORT_URI,
				RD_MANAGEMENT_NOTIFICATION_IN_PORT_URI,
				RequestDispatcherManagementNotificationConnector.class.getCanonicalName());
		
		this.amop.get(appUri).doDynamicConnectionWithDispatcherForSubmission(rdRequestSubmissionInPortUri.get(RD_URI));
		this.amop.get(appUri).doDynamicConnectionWithDispatcherForNotification(rop, rdRequestNotificationOutPortUri.get(RD_URI));
				
		for (int i = 0; i < applicationVMCount; i++) {
			rop.doPortConnection(
					rdRequestSubmissionOutPortUri.get(RD_URI).get(i),
					avmRequestSubmissionInPortUri.get(appUri + i),
					Javassist.getRequestSubmissionConnectorClassName());					
		}		
		rop.doDisconnection();
		
		// --------------------------------------------------------------------
		rop.doConnection(ATC_URI, ReflectionConnector.class.getCanonicalName());
		
		// 3 for automatic adaptation log
		AutonomicController.DEBUG_LEVEL = 3;
		rop.toggleLogging();
		rop.toggleTracing();
		
		this.atcmOutPort.get(ATC_URI).doConnectionWithComputerForServices(this.COMPUTER_SERVICES_IN_BOUND_PORT_URI);
		this.atcmOutPort.get(ATC_URI).doConnectionWithComputerForStaticState(this.COMPUTER_STATIC_STATE_DATA_IN_BOUND_PORT_URI);
		this.atcmOutPort.get(ATC_URI).doConnectionWithComputerForDynamicState(this.COMPUTER_DYNAMIC_STATE_DATA_IN_BOUND_PORT_URI, true);
		this.atcmOutPort.get(ATC_URI).doConnectionWithRequestDispatcherForDynamicState(RD_DYNAMIC_STATE_DATA_IN_PORT_URI, true);
		this.atcmOutPort.get(ATC_URI).doConnectionWithAdmissionControllerForAVMsManagement(atcAvmsManagementInPortUri.get(appUri));
		
		// is second application or more
		if (totalApplicationAccepted > 0) {
			// disconnect the admission controller's outBound port to connect it to the new one
			this.doPortDisconnection(ADMISSION_CONTROLLER_COORDINATION_OUT_BOUND_PORT_URI);			
			// always connect the new autonomic controller's outBound port to the last autonomic controller's inBound port
			rop.doPortConnection(
					AUTONOMIC_CONTROLLER_COORDINATION_OUT_PORT_URI,
					savedLastAutonomicControllerCoordinationInPortUri,
					AutonomicControllerCoordinationConnector.class.getCanonicalName());					
			
		// is first application	
		} else {
			rop.doPortConnection(
					AUTONOMIC_CONTROLLER_COORDINATION_OUT_PORT_URI,
					ADMISSION_CONTROLLER_COORDINATION_IN_BOUND_PORT_URI,
					AutonomicControllerCoordinationConnector.class.getCanonicalName());
		}		
		// always connect the admission controller's outBound port to the new autonomic controller's inBound port 
		this.doPortConnection(
				ADMISSION_CONTROLLER_COORDINATION_OUT_BOUND_PORT_URI,
				AUTONOMIC_CONTROLLER_COORDINATION_IN_PORT_URI,
				AutonomicControllerCoordinationConnector.class.getCanonicalName());
		
		this.savedLastAutonomicControllerCoordinationInPortUri = AUTONOMIC_CONTROLLER_COORDINATION_IN_PORT_URI;
		
		rop.doDisconnection();
		
		// --------------------------------------------------------------------
		for (int i = 0; i < applicationVMCount; i++) {
			final String AVM_URI = "avm-" + i + "-" + appUri;
			
			rop.doConnection(AVM_URI, ReflectionConnector.class.getCanonicalName());
	
			RequestDispatcher.DEBUG_LEVEL = 1;
			rop.toggleTracing();
			rop.toggleLogging();
			
			rop.doPortConnection(
					avmRequestNotificationOutPortUri.get(appUri + i),
					rdRequestNotificationInPortUri.get(RD_URI).get(i),
					Javassist.getRequestNotificationConnectorClassName());
			
			rop.doDisconnection();
		}			
	}
	
	public void prepareDeployment(String appUri, String rdUri, String atcUri, int applicationVMCount) throws Exception {
		
		// Application virtual machines
		prepareAVMsDeployment(appUri, rdUri, applicationVMCount);				
		
		// Request dispatcher
		prepareRDDeployment(rdUri);
				
		// Autonomic controller
		prepareAtCDeployment(atcUri);
		
	}
	
	protected void prepareAVMsDeployment(String appUri, String rdUri, int applicationVMCount) throws Exception {
		
		ArrayList<String> localRdRequestSubmissionOutPortUri = new ArrayList<>();
		ArrayList<String> localRdRequestNotificationInPortUri = new ArrayList<>();		
		
		int startIndex = avmIndexPerApp.get(appUri);
		startIndex++;
			
		for (int i = startIndex; i < applicationVMCount + startIndex; i++) {
						
			this.portToApplicationVMJVM.put(appUri + i, new DynamicComponentCreationOutboundPort(this));
			this.portToApplicationVMJVM.get(appUri + i).localPublishPort();
			this.addPort(this.portToApplicationVMJVM.get(appUri + i));
			this.portToApplicationVMJVM.get(appUri + i).doConnection(					
					this.APPLICATION_VM_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
					DynamicComponentCreationConnector.class.getCanonicalName());
			
			this.avmManagementInPortUri.put(appUri + i, appUri + "-amip-" + i);
			this.avmManagementOutPortUri.put(appUri + i, appUri + "-amop-" + i);	
			this.avmRequestSubmissionInPortUri.put(appUri + i, appUri + "-arsip-" + i);
			this.avmRequestNotificationOutPortUri.put(appUri + i, appUri + "-arnop-" + i);
			localRdRequestSubmissionOutPortUri.add(rdUri + "-rrsop-" + i);
			localRdRequestNotificationInPortUri.add(rdUri + "-rrnip-" + i);		
			
			// Update AVMs index
			avmIndexPerApp.put(appUri, i);
		}
		
		this.rdRequestSubmissionOutPortUri.put(rdUri, localRdRequestSubmissionOutPortUri);
		this.rdRequestNotificationInPortUri.put(rdUri, localRdRequestNotificationInPortUri);
	}
	
	protected void prepareRDDeployment(String rdUri) throws Exception {
		
		this.portToRequestDispatcherJVM.put(rdUri, new DynamicComponentCreationOutboundPort(this));
		this.portToRequestDispatcherJVM.get(rdUri).localPublishPort();
		this.addPort(this.portToRequestDispatcherJVM.get(rdUri));
		this.portToRequestDispatcherJVM.get(rdUri).doConnection(					
				this.REQUEST_DISPATCHER_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());	
	}
	
	/**
	 * Prepare l'autonomicController
	 * @param atcUri
	 * @throws Exception
	 */
	protected void prepareAtCDeployment(String atcUri) throws Exception {
		
		this.portToAutonomicControllerJVM.put(atcUri, new DynamicComponentCreationOutboundPort(this));
		this.portToAutonomicControllerJVM.get(atcUri).localPublishPort();
		this.addPort(this.portToAutonomicControllerJVM.get(atcUri));
		this.portToAutonomicControllerJVM.get(atcUri).doConnection(					
				this.AUTONOMIC_CONTROLLER_JVM_URI + AbstractCVM.DCC_INBOUNDPORT_URI_SUFFIX,
				DynamicComponentCreationConnector.class.getCanonicalName());
	}
	
	@Override
	public void acceptRequestAddAVM(String appUri, HashMap<Integer,ArrayList<AllocatedCore[]>> allocatedCoresMap) 
			throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller adding AVM for " + appUri + "...");										
		}
		
		// Prepare AVM deployment.	
		final String RD_URI = "rd-" + appUri;
		prepareAVMsDeployment(appUri, RD_URI, 1);
		
		// Deployment a new AVM.
		deployNewAVM(appUri, RD_URI);
		
		// Allocate initial cores.						
		String index = appUri + avmIndexPerApp.get(appUri);
		AllocatedCore[] mustHaveCores = this.mustHaveCoresPerApp.get(appUri);
		this.avmOutPort.get(index).allocateCores(mustHaveCores);
		
		// Allocate historical cores.
		for(ArrayList<AllocatedCore[]> allocatedCoreList : allocatedCoresMap.values()) {
			for(AllocatedCore[] allocatedCore : allocatedCoreList) {
				this.avmOutPort.get(index).allocateCores(allocatedCore);	
			}				
		}
	}

	/**
	 * Permet de deployer une nouvelle AVM
	 * @param appUri
	 * @param rdUri
	 * @throws Exception
	 */
	public void deployNewAVM(String appUri, String rdUri) throws Exception {
		
		// Create component
		int i = avmIndexPerApp.get(appUri);
		
		final String AVM_URI = "avm-" + i + "-" + appUri;
		
		this.portToApplicationVMJVM.get(appUri + i).createComponent(
				ApplicationVM.class.getCanonicalName(),
				new Object[] {
						AVM_URI,
						avmManagementInPortUri.get(appUri + i),
					    avmRequestSubmissionInPortUri.get(appUri + i),
					    avmRequestNotificationOutPortUri.get(appUri + i)
				});
		
		// Add those ports to RD
		this.rdmop.get(rdUri).createRequestSubmissionAndNotificationPorts(
				this.rdRequestSubmissionOutPortUri.get(rdUri).get(0), 
				this.rdRequestNotificationInPortUri.get(rdUri).get(0));		
		
		// Wait to accept this creation request from RD		
	}
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationHandlerI#acceptCreateRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void acceptCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception {
		
		int i = avmIndexPerApp.get(appUri);
		final String AVM_URI = "avm-" + i + "-" + appUri;
		
		// Create a mock up port to manage the AVM component (allocate cores).
		this.avmOutPort.put(appUri + i, new ApplicationVMManagementOutboundPort(avmManagementOutPortUri.get(appUri + i), this));			
		this.addPort(this.avmOutPort.get(appUri + i));			
		this.avmOutPort.get(appUri + i).publishPort();			
		
		this.avmOutPort.get(appUri + i).doConnection(
				avmManagementInPortUri.get(appUri + i),
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		// Do connection
		ReflectionOutboundPort rop = new ReflectionOutboundPort(this);
		this.addPort(rop);
		rop.localPublishPort();					
		
		rop.doConnection(rdUri, ReflectionConnector.class.getCanonicalName());		
		
		/*rop.toggleLogging();
		rop.toggleTracing();*/				
		
		rop.doPortConnection(
				rdRequestSubmissionOutPortUri.get(rdUri).get(0),
				avmRequestSubmissionInPortUri.get(appUri + i),
				Javassist.getRequestSubmissionConnectorClassName());					
		
		rop.doDisconnection();					
		// --------------------------------------------------------------------				
		rop.doConnection(AVM_URI, ReflectionConnector.class.getCanonicalName());

		RequestDispatcher.DEBUG_LEVEL = 1;
		rop.toggleTracing();
		rop.toggleLogging();
		
		rop.doPortConnection(
				avmRequestNotificationOutPortUri.get(appUri + i),
				rdRequestNotificationInPortUri.get(rdUri).get(0),
				Javassist.getRequestNotificationConnectorClassName());

		rop.doDisconnection();
	}
	

	@Override
	public void acceptRequestRemoveAVM(String appUri, String rdUri) throws Exception {

		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller removing AVM for " + appUri + "...");
		}
		
		// Remove a submission & a notification port from RD.
		this.rdmop.get(rdUri).destroyRequestSubmissionAndNotificationPorts();
		
		// Get AVM to remove index.
		int avmToRemoveIndex = avmIndexPerApp.get(appUri);					
		
		// Destroy an AVM port.
		this.portToApplicationVMJVM.get(appUri + avmToRemoveIndex).destroyPort();
		this.avmOutPort.get(appUri + avmToRemoveIndex).destroyPort();
		
		// Update AVMs index.
		avmToRemoveIndex--;
		avmIndexPerApp.put(appUri, avmToRemoveIndex);				
	}
	
	@Override
	public void acceptRequestAddCores(String appUri, AllocatedCore[] allocatedCore, int availableAVMsCount)  
			throws Exception {
		
		if (AdmissionController.DEBUG_LEVEL == 1) {
			this.logMessage("Admission controller adding cores for " + appUri + "...");
		}
		
		for (int i = 0; i < availableAVMsCount; i++) {
			this.avmOutPort.get(appUri + i).allocateCores(allocatedCore);
		}		
	}
	
	@Override
	public void acceptSentDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs)
			throws Exception {
		
		this.logMessage("~~~~~~~ " + this.AC_URI + " accepting data from " + thisSenderUri);
				
		if (this.AC_URI != originSenderUri) {
			// first AVM request, send the existing list			
			if (availableAVMs == null) {				
				this.atccop.sendDataAndNotify(originSenderUri, this.AC_URI, this.availableAVMsList);
			// otherwise passe to next component 	
			} else {
				this.atccop.sendDataAndNotify(originSenderUri, this.AC_URI, availableAVMs);
			}
		// when a user pick an AVM we have to update
		} else {
			this.availableAVMsList = availableAVMs;
		}
	}
}