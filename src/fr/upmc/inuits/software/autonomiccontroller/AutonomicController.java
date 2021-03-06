package fr.upmc.inuits.software.autonomiccontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.inuits.software.autonomiccontroller.connectors.AutonomicControllerAVMsManagementConnector;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerAVMsManagementI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationHandlerI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerCoordinationI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerServicesI;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerAVMsManagementOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerCoordinationInboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerCoordinationOutboundPort;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerManagementInboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;
/**
 * 
 * La classe qui permet controler la pérformence du centre de calcul et essayer de maintenire une valeur cible
 *
 */
public class AutonomicController 
	extends AbstractComponent 
	implements AutonomicControllerManagementI, AutonomicControllerServicesI, AutonomicControllerCoordinationHandlerI, 
		ComputerStateDataConsumerI, RequestDispatcherStateDataConsumerI {

	public static int DEBUG_LEVEL = 1;
	
	/** le temps entre chaque analyse de données */
	protected final int ANALYSE_DATA_TIMER = 1000;//500	
	
	protected final String atcURI;
	protected final String applicationURI;
	protected final ArrayList<String> computersURI;
	protected final int TOTAL_COMPUTERS_USED;
	
	protected ArrayList<String> computerServicesOutboundPortURI;
	protected ArrayList<String> computerStaticStateDataOutboundPortURI;
	protected ArrayList<String> computerDynamicStateDataOutboundPortURI;
	protected String requestDispatcherURI;
	protected String requestDispatcherDynamicStateDataOutboundPortURI;
	protected String admissionControllerAVMsManagementOutboundPortURI;
	
	protected ComputerServicesOutboundPort[] csop;
	protected ComputerStaticStateDataOutboundPort[] cssdop;
	protected ComputerDynamicStateDataOutboundPort[] cdsdop;	
	
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	protected AutonomicControllerManagementInboundPort atcmip;
	protected AutonomicControllerAVMsManagementOutboundPort atcamop;	
	protected AutonomicControllerCoordinationOutboundPort atccop;
	protected AutonomicControllerCoordinationInboundPort atccip;
	
	/** Utils pour calculer la moyenne */
	protected double exponentialSmoothing;
	protected double averageExecutionTime;
	protected int availableAVMsCount;
	
	/** Les coeur réserver par ordinateurs */
	protected HashMap<String, Boolean[][]> reservedCoresPerComputer;
		
	/**
	 * Contructeur qui initialise le controlleur autonomic
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre  atcURI != null && atcURI.length() > 0;
	 * pre computersURI != null && computersURI.size() > 0;
	 * pre computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.size() > 0;
	 * pre computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.size() > 0;
	 * pre computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.size() > 0;
	 * pre requestDispatcherDynamicStateDataOutboundPortURI != null 
	 *		&& requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
	 * pre autonomicControllerManagementInboundPortURI != null;
	 * pre autonomicControllerAVMsManagementOutboundPortURI != null 
	 *		&& autonomicControllerAVMsManagementOutboundPortURI.length() > 0;				
	 * pre autonomicControllerCoordinationOutboundPortURI != null 
	 *		&& autonomicControllerCoordinationOutboundPortURI.length() > 0;						
	 * pre autonomicControllerCoordinationInboundPortURI != null 
	 *		&& autonomicControllerCoordinationInboundPortURI.length() > 0;
	 *
	 * post this.atcURI != null;
	 * post  this.applicationURI != null;
     * post this.computerServicesOutboundPortURI != null && this.computerServicesOutboundPortURI.size() > 0;
	 * post this.computerStaticStateDataOutboundPortURI != null && this.computerStaticStateDataOutboundPortURI.size() > 0;
	 * post this.computerDynamicStateDataOutboundPortURI != null && this.computerDynamicStateDataOutboundPortURI.size() > 0;
	 * post this.requestDispatcherDynamicStateDataOutboundPortURI != null && this.requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
	 * post this.requestDispatcherURI != null && this.requestDispatcherURI.length() > 0;
	 * post this.cssdop != null && this.cssdop[0] instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
	 * post this.cdsdop != null && this.cdsdop[0] instanceof ControlledDataRequiredI.ControlledPullI;
	 * post this.rddsdop != null && this.rddsdop instanceof ControlledDataRequiredI.ControlledPullI;
	 * post this.atcmip != null && this.atcmip instanceof AutonomicControllerManagementI;
	 * post this.atcamop != null && this.atcamop instanceof AutonomicControllerAVMsManagementI;
	 * post this.atccop != null && this.atccop instanceof AutonomicControllerCoordinationI;
	 * post this.atccip != null && this.atccip instanceof AutonomicControllerCoordinationI;
	 *		
	 * </pre>	
	 * 
	 * @param atcURI identifiant du controlleur autonomic
	 * @param computersURI identifiants des computers
	 * @param computerServicesOutboundPortURI manipuler les coeurs et les fréquences
	 * @param computerStaticStateDataOutboundPortURI avoir les données statique depuis l'ordinateur
	 * @param computerDynamicStateDataOutboundPortURI avoir les données dynamique depuis l'ordinateur
	 * @param applicationURI identifiant de l'application
	 * @param requestDispatcherURI identifiant de ripartiteur de reuquetes 
	 * @param requestDispatcherDynamicStateDataOutboundPortURI avoir les données dynamique depuis le ripartiteur de requetes
	 * @param autonomicControllerManagementInboundPortURI gerer la connection avet le controleur autonomique
	 * @param autonomicControllerAVMsManagementOutboundPortURI gerer les avm (ajout, supression)
	 * @param autonomicControllerCoordinationOutboundPortURI envoyer des demandes pour avoir les avm disponibles
	 * @param autonomicControllerCoordinationInboundPortURI recevoir des demandes pour avoir les avm disponibles
	 * @throws Exception
	 */
	public AutonomicController(
			String atcURI,
			ArrayList<String> computersURI,			
			ArrayList<String> computerServicesOutboundPortURI,
			ArrayList<String> computerStaticStateDataOutboundPortURI,
			ArrayList<String> computerDynamicStateDataOutboundPortURI,
			String applicationURI,
			String requestDispatcherURI, 
			String requestDispatcherDynamicStateDataOutboundPortURI,
			String autonomicControllerManagementInboundPortURI,
			String autonomicControllerAVMsManagementOutboundPortURI,
			String autonomicControllerCoordinationOutboundPortURI,
			String autonomicControllerCoordinationInboundPortURI) throws Exception {
	
		super(atcURI, 1, 1);
		
		assert atcURI != null && atcURI.length() > 0;
		assert computersURI != null && computersURI.size() > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.size() > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.size() > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.size() > 0;
		assert requestDispatcherDynamicStateDataOutboundPortURI != null 
				&& requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
		assert autonomicControllerManagementInboundPortURI != null;
		assert autonomicControllerAVMsManagementOutboundPortURI != null 
				&& autonomicControllerAVMsManagementOutboundPortURI.length() > 0;				
		assert autonomicControllerCoordinationOutboundPortURI != null 
				&& autonomicControllerCoordinationOutboundPortURI.length() > 0;						
		assert autonomicControllerCoordinationInboundPortURI != null 
				&& autonomicControllerCoordinationInboundPortURI.length() > 0;
				
		this.atcURI = atcURI;
		this.applicationURI = applicationURI;
		this.computersURI = computersURI;
		this.TOTAL_COMPUTERS_USED = computersURI.size();
		
		this.computerServicesOutboundPortURI = computerServicesOutboundPortURI;
		this.computerStaticStateDataOutboundPortURI = computerStaticStateDataOutboundPortURI;
		this.computerDynamicStateDataOutboundPortURI = computerDynamicStateDataOutboundPortURI;
		this.requestDispatcherDynamicStateDataOutboundPortURI = requestDispatcherDynamicStateDataOutboundPortURI;
		this.requestDispatcherURI = requestDispatcherURI; 
		this.admissionControllerAVMsManagementOutboundPortURI = autonomicControllerAVMsManagementOutboundPortURI; 
		
		this.csop = new ComputerServicesOutboundPort[TOTAL_COMPUTERS_USED];
		this.cssdop = new ComputerStaticStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		this.cdsdop = new ComputerDynamicStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		
		this.addRequiredInterface(ComputerServicesI.class);
		// this.addOfferedInterface(ComputerStaticStateDataI.class); or :
		//this.addOfferedInterface(DataRequiredI.PushI.class);
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
			
		this.rddsdop = new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPortURI, this, requestDispatcherURI);
		this.addPort(this.rddsdop);
		this.rddsdop.publishPort();
		
		this.addOfferedInterface(AutonomicControllerManagementI.class);
		this.atcmip = new AutonomicControllerManagementInboundPort(autonomicControllerManagementInboundPortURI, this);
		this.addPort(this.atcmip);
		this.atcmip.publishPort();
		
		this.addRequiredInterface(AutonomicControllerAVMsManagementI.class);
		this.atcamop = new AutonomicControllerAVMsManagementOutboundPort(autonomicControllerAVMsManagementOutboundPortURI, this);
		this.addPort(this.atcamop);
		this.atcamop.publishPort();
		
		// To send coordination data 
		this.addRequiredInterface(AutonomicControllerCoordinationI.class);
		this.atccop = new AutonomicControllerCoordinationOutboundPort(autonomicControllerCoordinationOutboundPortURI, this);
		this.addPort(this.atccop);
		this.atccop.publishPort();
		
		// To receive coordination data
		this.atccip = new AutonomicControllerCoordinationInboundPort(autonomicControllerCoordinationInboundPortURI, this);
		this.addPort(this.atccip);
		this.atccip.publishPort();
		
		// init
		this.exponentialSmoothing = -1;
		this.averageExecutionTime = -1;
		this.availableAVMsCount = -1;
		this.reservedCoresPerComputer = new HashMap<>();
		
		assert this.atcURI != null;
		assert this.applicationURI != null;
		assert this.computerServicesOutboundPortURI != null && this.computerServicesOutboundPortURI.size() > 0;
		assert this.computerStaticStateDataOutboundPortURI != null && this.computerStaticStateDataOutboundPortURI.size() > 0;
		assert this.computerDynamicStateDataOutboundPortURI != null && this.computerDynamicStateDataOutboundPortURI.size() > 0;
		assert this.requestDispatcherDynamicStateDataOutboundPortURI != null && this.requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
		assert this.requestDispatcherURI != null && this.requestDispatcherURI.length() > 0;
		assert this.cssdop != null && this.cssdop[0] instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop[0] instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.rddsdop != null && this.rddsdop instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.atcmip != null && this.atcmip instanceof AutonomicControllerManagementI;
		assert this.atcamop != null && this.atcamop instanceof AutonomicControllerAVMsManagementI;
		assert this.atccop != null && this.atccop instanceof AutonomicControllerCoordinationI;
		assert this.atccip != null && this.atccip instanceof AutonomicControllerCoordinationI;
	}

	/**
	 * lancer le détecteur de variation de la moyenne
	 */
	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
		
		controlResources();			
	}
	
	/**
	 * Methode permettant l'arrêt du composant AutonomicController, en déconnectant les différents ports.
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
			if (this.rddsdop.connected()) {
				this.rddsdop.doDisconnection();
			}
			if (this.atcamop.connected()) {
				this.atcamop.doDisconnection();
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
	 * Faire une connexion entre l'ordinateur et le AutonomicController pour pouvoir gérer les coeurs et les fréquences
	 */
	@Override
	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) throws Exception {

		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(				
					computerServicesOutboundPortURI.get(i),
					computerServicesInboundPortUri.get(i),
					ComputerServicesConnector.class.getCanonicalName());
		}
	}

	/**
	 * Faire une connexion entre l'ordinateur et le AutonomicController pour avoir les données statique de l'ordinateur
	 */
	@Override
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) 
			throws Exception {
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(
					this.computerStaticStateDataOutboundPortURI.get(i),
					computerStaticStateInboundPortUri.get(i),
					DataConnector.class.getCanonicalName());
		}
	}
	
	/**
	 * Faire une connexion entre l'ordinateur et le AutonomicController pour avoir les données dynamique de l'ordinateur
	 */
	@Override
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(
					this.computerDynamicStateDataOutboundPortURI.get(i),
					computerDynamicStateInboundPortUri.get(i),
					ControlledDataConnector.class.getCanonicalName());
		}
		
		// start the pushing of dynamic state information from the computer if true.
		if (isStartPushing) {
			try {												
				for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
					this.cdsdop[i].startUnlimitedPushing(ANALYSE_DATA_TIMER); 
				}			
														
			} catch (Exception e) {
				throw new ComponentStartException("Unable to start pushing dynamic data from the computer component.", e);
			}	
		}		
	}
	
	/**
	 * Faire une connexion entre le dispatcher et le AutonomicController pour avoir les données dynamique de l'ordinateur
	 */
	@Override
	public void doConnectionWithRequestDispatcherForDynamicState(String requestDispatcherDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		this.doPortConnection(
				this.requestDispatcherDynamicStateDataOutboundPortURI,
				requestDispatcherDynamicStateInboundPortUri,
				ControlledDataConnector.class.getCanonicalName());
		
		// start the pushing of dynamic state information from the request dispatcher.
		if (isStartPushing) {
			try {												
				this.rddsdop.startUnlimitedPushing(ANALYSE_DATA_TIMER);					
														
			} catch (Exception e) {
				throw new ComponentStartException("Unable to start pushing dynamic data from the request dispatcher "
						+ "component.", e);
			}
		}
	}
	
	/**
	 * Faire une connexion entre le controleur d'admission et le AutonomicController pour gérer les avm (ajout, suppression)
	 */
	@Override
	public void doConnectionWithAdmissionControllerForAVMsManagement(
			String admissionControllerAtCAVMsManagementInboundPortUri) throws Exception {

		this.doPortConnection(
				this.admissionControllerAVMsManagementOutboundPortURI,
				admissionControllerAtCAVMsManagementInboundPortUri,
				AutonomicControllerAVMsManagementConnector.class.getCanonicalName());	
	}
	
	/*
	 * (non-Javadoc)
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerStaticData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI)
	 */
	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
		
		if (DEBUG_LEVEL == 4) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Autonomic controller accepting static data from " + computerURI + "\n");
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

	/*
	 * (non-Javadoc)
	 * @see fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI#acceptComputerStaticData(java.lang.String, fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI)
	 */
	@Override
	public synchronized void acceptComputerDynamicData(String computerURI, ComputerDynamicStateI currentDynamicState)
			throws Exception {					
		
		boolean[][] reservedCores = currentDynamicState.getCurrentCoreReservations();
		Boolean[][] reservedCoresBoolean = new Boolean[reservedCores.length][reservedCores[0].length];
		
		for (int i = 0; i < reservedCores.length; i++) {
			for (int j = 0; j < reservedCores[i].length; j++) {
				reservedCoresBoolean[i][j] = reservedCores[i][j]; 
			}		
		}
		
		this.reservedCoresPerComputer.put(computerURI, reservedCoresBoolean);
		
		if (DEBUG_LEVEL == 3) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Autonomic controller accepting dynamic data from " + computerURI + "\n");
			sb.append("  current frequency        : " + currentDynamicState.getCurrentFrequency() + "\n");
			//sb.append("  timestamp                : " + currentDynamicState.getTimeStamp() + "\n");
			//sb.append("  timestamper id           : " + currentDynamicState.getTimeStamperId() + "\n");
						
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
	 * Recupérer les données dynamiques depuis le RequestDispatcher (la moyenne, les AVMs disponibles, le nombre total des requets, les requets terminés 
	 */
	@Override
	public synchronized void acceptRequestDispatcherDynamicData(String rdURI, RequestDispatcherDynamicStateI currentDynamicState)
			throws Exception {
		
		if (rdURI == this.requestDispatcherURI) {
			this.exponentialSmoothing = currentDynamicState.getCurrentExponentialSmoothing();
			this.averageExecutionTime = currentDynamicState.getCurrentAverageExecutionTime();
			this.availableAVMsCount = currentDynamicState.getAvailableAVMsCount();			
			int totalRequestSubmitted = currentDynamicState.getTotalRequestSubmittedCount();
			int totalRequestTerminated = currentDynamicState.getTotalRequestTerminatedCount();
			
			if (AutonomicController.DEBUG_LEVEL == 3) {
				StringBuffer sb = new StringBuffer();

				sb.append("Autonomic controller accepting dynamic data from " + rdURI + "\n");
				sb.append("  exponential smoothing  : [" + exponentialSmoothing + "]\n");
				sb.append("  average execution time : [" + averageExecutionTime + "]\n");								
				sb.append("  available AVMs count   : [" + availableAVMsCount + "]\n");
				sb.append("  waiting / total        : [" + (totalRequestSubmitted - totalRequestTerminated) + "/" + totalRequestSubmitted + "]\n");
				sb.append("  the success rate       : [" + totalRequestTerminated * 100 / totalRequestSubmitted + " %]\n");
				 
				//sb.append("  current time millis : " + System.currentTimeMillis() + "\n");			
				
				this.logMessage(sb.toString());
			}
		}			
	}		
	
	/**
	 * Vérifier s'il existe des coeurs disponibles
	 * @param computerUri l'id de l'ordinateur
	 * @param mustHaveCores les coeurs qu'il faut avoir
	 * @return vrai ou faux s'il y des coeurs libres
	 */
	public synchronized boolean isResourcesAvailable(String computerUri, int mustHaveCores) {		
		
		int availableCores = 0;
		Boolean[][] reservedCores = reservedCoresPerComputer.get(computerUri);
		
		for (int p = 0; p < reservedCores.length; p++) {
			for (int c = 0; c < reservedCores[0].length; c++) {
				
				if (!reservedCores[p][c]) {					
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
	 * Utiliser pour la coordination entre controleur autonomiques et le controleur d'admission, en passant des données
	 * @param originSenderUri l'id du controleur qui à initialiser l'appel (utilisé comme condition d'arrêt
	 * @param thisSenderUri l'id du controleur qui à transmet l'information
	 * @param availableAVMs la liste des AVMs disponibles
	 */
	@Override
	public void acceptSentDataAndNotify(String originSenderUri, String thisSenderUri, ArrayList<String> availableAVMs)
			throws Exception {

		this.logMessage("~~~~~~~ " + this.atcURI + " accepting data from " + thisSenderUri);
		
		// passe data to next component
		if (this.atcURI != originSenderUri) {
			this.atccop.sendDataAndNotify(originSenderUri, this.atcURI, availableAVMs);
		
		// stop condition : this made a whole loop	
		} else {
			// pick an AVM and send request to admission controller to update
			if (availableAVMs != null && availableAVMs.size() > 0) {				
				showLogMessageL3("____Adding AVMs...");
				//this.atcamop.doRequestAddAVM(availableAVMs.remove(0), this.allocatedCoresHistory);
				this.atccop.sendDataAndNotify("admissionController", this.atcURI, availableAVMs);
				
			} else {
				this.logMessage("No more AVMs available.");		
			}
		}		
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	protected final int CONTROL_RESOURCES_TIMER = ANALYSE_DATA_TIMER * 5;
	
	protected final int LOWER_THRESHOLD = 400;
	protected  int HIGHER_THRESHOLD = 600; //1500
	
	protected final int VM_TO_ALLOCATE_COUNT = 1;
	protected final int VM_TO_DEALLOCATED_COUNT = VM_TO_ALLOCATE_COUNT;
	
	protected final int CORES_TO_ADD_COUNT = 4;
	protected final int CORES_TO_REMOVE_COUNT = CORES_TO_ADD_COUNT;	
	// Do not remove AVM when only 2 AVMs left
	protected final int MUST_HAVE_VM_COUNT = 2;
	// Do not add more AVMs if already 5 are deployed 
	protected final int MAXIMUM_ALLOWED_VM_COUNT = 5;
	
	// Archive allocated cores history
	HashMap<Integer,ArrayList<AllocatedCore[]>> allocatedCoresHistory = new HashMap<>();
	
	/**
	 * Une tâche récursive pour controler les recources
	 */
	public void controlResources() {
		
		this.scheduleTask(
				new ComponentI.ComponentTask() {
					
					@Override
					public void run() {
						try {
							applyAdaptationPolicy();
							
						} catch (Exception e) {							
							e.printStackTrace();
						}
						
						controlResources();
					}

				}, CONTROL_RESOURCES_TIMER, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * La politique de gestion de recources
	 * @throws Exception
	 */
	protected void applyAdaptationPolicy() throws Exception {
		// uncomment to see difference
		//int average = (int) this.exponentialSmoothing;
		int average = (int) this.averageExecutionTime;
				
		if (average > 0) {
			// The higher threshold is crossed upwards.	
			if (averageExecutionTime >= HIGHER_THRESHOLD) {								
				showLogMessageL3("__[The higher threshold " + HIGHER_THRESHOLD + " is crossed upwards : " + average + "]");
				
				// 1- Increase frequency if possible.
				if (increaseFrequency()) {							
					showLogMessageL3("______[[Frequency increased]]");
					
				// 2- Add cores if possible.
				} else if (addCores()) {					
					showLogMessageL3("______[[Cores added]]");
				
				// 3- Add AVMs (always possible).
				} else if (addAVMs()) {					
					showLogMessageL3("______[[AVMs added]]");
					
				// 4- Nothing else to do.
				} else {
					showLogMessageL3("____Can't do nothing else. " + MAXIMUM_ALLOWED_VM_COUNT + " are the maximum AVMs allowed.");
				}
				
			// The lower threshold is crossed down.
			} else if (averageExecutionTime <= LOWER_THRESHOLD) {						
				showLogMessageL3("__[The lower threshold " + LOWER_THRESHOLD + " is crossed down : " + average + "]");			
				
				// 1- Decrease frequency if possible.
				if (decreaseFrequency()) {
					showLogMessageL3("______[[Frequency decreased]]");				
					
				// 2- Remove cores if possible.
				} else if (removeCores()) {				
					showLogMessageL3("______[[Cores removed]]");
					
				// 3- Remove AVMs if possible.
				} else if (removeAVMs()) {				
					showLogMessageL3("______[[AVMs removed]]");
				
				// 4- Nothing else to do.
				} else {
					showLogMessageL3("____Can't do nothing else. " + MUST_HAVE_VM_COUNT + " are the minimum AVMs required.");
				}
				
			// Normal situation.
			} else {			
				showLogMessageL3("__[Normal situation between " + LOWER_THRESHOLD + " & " + HIGHER_THRESHOLD + "]"); 
			}
			
		// Ignore.
		} else {
			showLogMessageL3("__[Waiting for the average execution time]");
		}		
	}

	/**
	 * augmenter la fréquence des coeurs
	 * @return vrai ou faux si l'augmentation est possible 
	 */
	@Override
	public boolean increaseFrequency() throws Exception {
		
		showLogMessageL3("____Increasing frequency...");
		
		boolean canIncreaseFrequency = false;		
		int frequency;
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			frequency = this.csop[i].increaseFrequency();
			if (frequency != -1) {
				canIncreaseFrequency = true;
				break;
			}
		}
		
		if (!canIncreaseFrequency) {
			showLogMessageL3("______[[Failed]]");
		}
		
		return canIncreaseFrequency;				
	}

	/**
	 * Baisseer la fréquence des coeurs
	 * @return vrai ou faux si la baisse est possible 
	 */
	@Override
	public boolean decreaseFrequency() throws Exception {
		
		showLogMessageL3("____Decreasing frequency...");
		
		boolean canDecreaseFrequency = false;
		int frequency;
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			frequency = this.csop[i].decreaseFrequency();
			if (frequency != -1) {
				canDecreaseFrequency = true;
				break;
			}
		}
		
		if (!canDecreaseFrequency) {
			showLogMessageL3("______[[Failed]]");
		}
		
		return canDecreaseFrequency;		
	}

	/**
	 * Ajouter des cours
	 * @return vrai ou faux si l'ajout des coeurs est possible 
	 */
	@Override
	public boolean addCores() throws Exception {

		showLogMessageL3("____Adding cores...");
		
		boolean canAddCores = false;
		int computerToUseIndex = -1;				
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			String computer = computersURI.get(i);
			canAddCores = isResourcesAvailable(computer, CORES_TO_ADD_COUNT);
			
			if (canAddCores) {
				computerToUseIndex = i;
				break;
			}
		}
		
		if (canAddCores) {
			AllocatedCore[] allocatedCore = this.csop[computerToUseIndex].allocateCores(CORES_TO_ADD_COUNT);
			this.atcamop.doRequestAddCores(this.applicationURI, allocatedCore, this.availableAVMsCount);
			
			// Update History
			Integer computerToUseIndexInteger = computerToUseIndex;
			ArrayList<AllocatedCore[]> allocatedCoreList = this.allocatedCoresHistory.get(computerToUseIndexInteger);
			
			if (allocatedCoreList != null) {
				allocatedCoreList.add(allocatedCore);
				this.allocatedCoresHistory.put(computerToUseIndexInteger, allocatedCoreList);
				
			} else {
				ArrayList<AllocatedCore[]> allocatedCoreListTmp = new ArrayList<>();
				allocatedCoreListTmp.add(allocatedCore);
				this.allocatedCoresHistory.put(computerToUseIndexInteger, allocatedCoreListTmp);
			}
									
		} else {
			showLogMessageL3("______[[Failed]]");
		}
		
		return canAddCores;
	}

	/**
	 * Enlever des cours
	 * @return vrai ou faux si la supression des coeurs est possible 
	 */
	@Override
	public boolean removeCores() throws Exception {

		showLogMessageL3("____Removing cores...");
		
		boolean canRemoveCores = false;
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			ArrayList<AllocatedCore[]> allocatedCoreList = this.allocatedCoresHistory.get(i);
			
			if (allocatedCoreList != null && allocatedCoreList.size() > 0) {
				AllocatedCore[] coresToRelease = allocatedCoreList.remove(allocatedCoreList.size() - 1);
								
				this.csop[i].releaseCores(coresToRelease);
				
				canRemoveCores = true;
				break;
			}
		}		
		
		if (!canRemoveCores) {
			showLogMessageL3("______[[Failed]]");
		}
		
		return canRemoveCores;
	}

	/**
	 * Ajouter des AVM
	 * @return vrai ou faux si l'ajout des AVM est possible 
	 */
	@Override
	public boolean addAVMs() throws Exception {
		
		showLogMessageL3("____Adding AVMs...");
			
		// get an AVM id if exists using coordination between autonomic controller
		//this.atccop.sendDataAndNotify(this.atcURI, this.atcURI, null);
		
		boolean canAddAVM = false;
		
		if (this.availableAVMsCount < MAXIMUM_ALLOWED_VM_COUNT) {
			this.atcamop.doRequestAddAVM(this.applicationURI, this.allocatedCoresHistory);
			canAddAVM = true;
			
		} else {
			showLogMessageL3("______[[Failed]]");	
		}
		
		return canAddAVM;
	}

	/**
	 * Retrancher des AVM
	 * @return vrai ou faux si cette opération est possible 
	 */
	@Override
	public boolean removeAVMs() throws Exception {
		
		showLogMessageL3("____Removing AVMs...");
		
		boolean canRemoveAVM = false;
		
		if (this.availableAVMsCount > MUST_HAVE_VM_COUNT) {
			this.atcamop.doRequestRemoveAVM(this.applicationURI, this.requestDispatcherURI);	
			canRemoveAVM = true;
			
		} else {
			showLogMessageL3("______[[Failed]]");	
		}				
		
		return canRemoveAVM;
	}
	
	protected void showLogMessageL3(String message) {
		
		if (DEBUG_LEVEL == 3) {
			this.logMessage(message);
		}
	}
}
