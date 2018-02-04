package fr.upmc.inuits.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.utils.TimeProcessing;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementNotificationI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataInboundPort;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherManagementInboundPort;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherManagementNotificationOutboundPort;

/** 
 * La class <code>RequestDispatcher</code> permet de répartir les requêtes des applications aux differentes AVM(s) associées aux applications.  
 */
public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestDispatcherManagementI, RequestSubmissionHandlerI, RequestNotificationHandlerI, PushModeControllingI {

	public static int DEBUG_LEVEL = 1;
	
	protected final String rdURI;
	protected final String appURI;
	protected int availableApplicationVm;
	protected int applicationVMSelector;
	
	protected RequestDispatcherManagementInboundPort rdmip;
	protected RequestDispatcherManagementNotificationOutboundPort rdmnop;
	protected RequestSubmissionInboundPort rsip;
	protected HashMap<Integer,RequestSubmissionOutboundPort> rsop;
	protected HashMap<Integer,RequestNotificationInboundPort> rnip;
	protected RequestNotificationOutboundPort rnop;
	
	protected RequestDispatcherDynamicStateDataInboundPort rddsdip;
	
	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;
	
	/** Variables et objets utilisé dans le calcule du temps moyens de l'execution des requêtes */
	protected Smoothing smoothing; 
	protected double exponentialSmoothing;
	protected Average average;
	protected double calculatedAverage;
	
	/** Variables representants le nombre de tâches soumises et nombre de tâches terminées   */
	protected int totalRequestSubmitted; 
	protected int totalRequestTerminated;
	
	/**
	 * Contructeur qui initialise le RequestDispatcher.
	 * Il permet d'initialiser les ports et de les publiers, les variables du calcul du temps moyens.
	 * 
	 * 
	 * <p><strong>Contract</strong></p>
	 * 
	 * <pre>
	 * pre rdURI != null
	 * pre requestDispatcherManagementIntboundPortURI != null && requestDispatcherManagementIntboundPortURI.length() > 0
	 * pre requestDispatcherManagementNotificationOutboundPortURI != null && requestDispatcherManagementNotificationOutboundPortURI.length() > 0		
	 * pre appURI != null;
	 * pre requestSubmissionIntboundPortURI != null && requestSubmissionIntboundPortURI.length() > 0
	 * pre requestSubmissionOutboundPortURI != null && requestSubmissionOutboundPortURI.size() > 0
	 * pre requestNotificationIntboundPortURI != null && requestNotificationIntboundPortURI.size() > 0
	 * pre requestSubmissionOutboundPortURI.size() == requestNotificationIntboundPortURI.size()
	 * pre requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0
	 * pre requestDispatcherDynamicStateDataInboundPortURI != null && requestDispatcherDynamicStateDataInboundPortURI.length() > 0
	 * 
	 * post this.rdURI != null
	 * post this.availableApplicationVm == requestNotificationIntboundPortURI.size()
	 * post this.appURI != null
	 * post this.applicationVMSelector == 0
	 * post this.rdmip != null && this.rdmip instanceof RequestDispatcherManagementI
	 * post this.rdmnop != null && this.rdmnop instanceof RequestDispatcherManagementNotificationI
	 * post this.rsip != null && this.rsip instanceof RequestSubmissionI
	 * post this.rsop != null && this.rsop.get(0) instanceof RequestSubmissionI
	 * post this.rnip != null && this.rnip.get(0) instanceof RequestNotificationI
	 * post this.rnop != null && this.rnop instanceof RequestNotificationI
	 * post this.rddsdip != null && this.rddsdip instanceof ControlledDataOfferedI.ControlledPullI
	 * </pre>
	 * 
	 * @param rdURI uri du request dispatcher
	 * @param requestDispatcherManagementIntboundPortURI Uri du port entrant permettant la creation des ports qui lieront le request generateur et le répartiteur de requêtes, permettant aux requetes de circuler.
	 * @param requestDispatcherManagementNotificationOutboundPortURI Uri du port sortant permettant l'acces a la creation des ports qui lieront le request generateur et le répartiteur de requêtes, permettant aux requetes de circuler.
	 * @param appURI uri de l'application associée
	 * @param requestSubmissionIntboundPortURI Uri du port entrant permettant aux dispatcher de recevoir la requete a répartir.
	 * @param requestSubmissionOutboundPortURI Uri du port sortant permettant d'envoyer la requete à une AVM.
	 * @param requestNotificationIntboundPortURI Uri du port entrant permettant de recevoir la notification de l'execution de la requête.
	 * @param requestNotificationOutboundPortURI Uri du port sortant permettant de renvoyer la notification de l'execution de la requête au requestGenerateur source de cette derniere.
	 * @param requestDispatcherDynamicStateDataInboundPortURI Port entrant permetant l'accés aux données dynamiques du repartiteur de requêtes (pour le temps moyens d'execution des requetes d'une application)
	 * @throws Exception
	 */
	public RequestDispatcher(
			String rdURI,
			String requestDispatcherManagementIntboundPortURI,
			String requestDispatcherManagementNotificationOutboundPortURI,
			String appURI,
			String requestSubmissionIntboundPortURI, 
			ArrayList<String> requestSubmissionOutboundPortURI,
			ArrayList<String> requestNotificationIntboundPortURI, 
			String requestNotificationOutboundPortURI,
			String requestDispatcherDynamicStateDataInboundPortURI) throws Exception {
		
		super(rdURI, 1, 1);
		
		assert rdURI != null;
		assert requestDispatcherManagementIntboundPortURI != null 
				&& requestDispatcherManagementIntboundPortURI.length() > 0;
		assert requestDispatcherManagementNotificationOutboundPortURI != null 
				&& requestDispatcherManagementNotificationOutboundPortURI.length() > 0;		
		assert appURI != null;
		assert requestSubmissionIntboundPortURI != null && requestSubmissionIntboundPortURI.length() > 0;
		assert requestSubmissionOutboundPortURI != null && requestSubmissionOutboundPortURI.size() > 0;
		assert requestNotificationIntboundPortURI != null && requestNotificationIntboundPortURI.size() > 0;
		assert requestSubmissionOutboundPortURI.size() == requestNotificationIntboundPortURI.size();
		assert requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0;
		assert requestDispatcherDynamicStateDataInboundPortURI != null 
				&& requestDispatcherDynamicStateDataInboundPortURI.length() > 0;
		
		this.rdURI = rdURI;
		this.availableApplicationVm = requestSubmissionOutboundPortURI.size();
		this.appURI = appURI;
		this.applicationVMSelector = 0;
		this.rsop = new HashMap<>();
		this.rnip = new HashMap<>();								
		
		this.addOfferedInterface(RequestDispatcherManagementI.class);
		this.rdmip = new RequestDispatcherManagementInboundPort(requestDispatcherManagementIntboundPortURI, this);
		this.addPort(this.rdmip);
		this.rdmip.publishPort();
		
		this.addRequiredInterface(RequestDispatcherManagementNotificationI.class);
		this.rdmnop = new RequestDispatcherManagementNotificationOutboundPort(requestDispatcherManagementNotificationOutboundPortURI, this);
		this.addPort(this.rdmnop);
		this.rdmnop.publishPort();
		
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(requestSubmissionIntboundPortURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();			
				
		this.addRequiredInterface(RequestSubmissionI.class);
		this.addOfferedInterface(RequestNotificationI.class);
		
		for (int i = 0; i < this.availableApplicationVm; i++) {						
			this.rsop.put(i, new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI.get(i), this));
			this.addPort(this.rsop.get(i));
			this.rsop.get(i).publishPort();			
			
			this.rnip.put(i, new RequestNotificationInboundPort(requestNotificationIntboundPortURI.get(i), this));
			this.addPort(this.rnip.get(i));
			this.rnip.get(i).publishPort();
		}				
		
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.rddsdip = 
				new RequestDispatcherDynamicStateDataInboundPort(requestDispatcherDynamicStateDataInboundPortURI, this);
		this.addPort(rddsdip);
		this.rddsdip.publishPort();
		
		this.smoothing = new Smoothing();
		this.average = new Average();
		this.totalRequestSubmitted = 0; 
		this.totalRequestTerminated = 0;
		
		assert this.rdURI != null;
		assert this.availableApplicationVm == requestNotificationIntboundPortURI.size();
		assert this.appURI != null;
		assert this.applicationVMSelector == 0;
		assert this.rdmip != null && this.rdmip instanceof RequestDispatcherManagementI;
		assert this.rdmnop != null && this.rdmnop instanceof RequestDispatcherManagementNotificationI;		
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rsop != null && this.rsop.get(0) instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip.get(0) instanceof RequestNotificationI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;		
		assert this.rddsdip != null && this.rddsdip instanceof ControlledDataOfferedI.ControlledPullI;
	}
	
	/**
	 * Methode permettant l'arrêt du composant RequestDispatcher, en déconnectant les différents ports.
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
			for (RequestSubmissionOutboundPort thisRsop : this.rsop.values()) {
				if (thisRsop.connected()) {
					thisRsop.doDisconnection();
				}
			}			
			if (this.rdmnop.connected()) {				
				this.rdmnop.doDisconnection();							
			}
			if (this.rnop.connected()) {				
				this.rnop.doDisconnection();							
			}
			if (this.rddsdip.connected()) {
				this.rddsdip.doDisconnection();
			}
		} catch (Exception e) {			
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}	

	/**	
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#createRequestSubmissionAndNotificationPorts(String, String)
	 */
	@Override
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri,
			String requestNotificationIntboundPortUri) throws Exception {

		assert requestSubmissionOutboundPortUri != null && requestSubmissionOutboundPortUri.length() > 0;
		assert requestNotificationIntboundPortUri != null && requestNotificationIntboundPortUri.length() > 0;
		
		int i = availableApplicationVm;
		
		// requestSubmissionOutboundPortUri
		this.rsop.put(i, new RequestSubmissionOutboundPort(requestSubmissionOutboundPortUri, this));
		this.addPort(this.rsop.get(i));
		this.rsop.get(i).publishPort();
		
		// requestNotificationIntboundPortUri
		this.rnip.put(i, new RequestNotificationInboundPort(requestNotificationIntboundPortUri, this));
		this.addPort(this.rnip.get(i));
		this.rnip.get(i).publishPort();
		
		this.availableApplicationVm++;
		
		assert this.rsop != null && this.rsop.get(i) instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip.get(i) instanceof RequestNotificationI;
		
		// notify Admission Controller
		this.rdmnop.notifyCreateRequestSubmissionAndNotificationPorts(appURI, rdURI);
	}

	/**	
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherManagementI#destroyRequestSubmissionAndNotificationPorts()
	 */
	@Override
	public void destroyRequestSubmissionAndNotificationPorts() throws Exception {

		assert this.availableApplicationVm >= 3;
		
		this.availableApplicationVm--;
		
		int i = availableApplicationVm;		
		this.rsop.get(i).destroyPort();
		this.rnip.get(i).destroyPort();
		
		assert this.availableApplicationVm >= 2;
	}
	
	protected HashMap<String, Long> beginningTime = new HashMap<>();
	protected HashMap<String, Long> executionTime = new HashMap<>();
	
	/**
	 * Class permettant la gestion de de la moyenne du temps d'execution des requetes selon la technique de la moyenne mouvante . 
	 *
	 */
	private class Average {
		
		private final int N = 4;
		private ArrayList<Double> observedValue;
		private int internalCounter;
		/**
		 * Crée l'objet Average
		 */
		public Average() {
			this.observedValue = new ArrayList<>();
			this.internalCounter = 1;
		}
		/**
		 * Permet de calculer la Moyenne du temps d'execution des requetes
		 * @param newObservedValue temps d'execution d'une requête.
		 * @return Moyenne du temps d'execution des requetes.
		 */
		public int calculateAverage(double newObservedValue) {
			
			double sum = 0.0;
			this.observedValue.add(newObservedValue);
			
			if (internalCounter >= N) {
				for (int i = internalCounter - N; i < observedValue.size(); i++) {					
					sum += observedValue.get(i);	
				}
			} 
						
			internalCounter++;
			
			return (int) Math.round(sum / N);
		}
	}
	/**
	 *  Class permettant la gestion de la moyenne du temps d'execution des requetes selon la technique du lissage exceptionnel .  
	 */
	private class Smoothing {
		
		private final double ALPHA = 0.7;		
		private double observedValue;
		private double oldSmoothedValue;		
		private int internalCounter;
		/**
		 * Permet de cree l'objet Smoothing
		 */
		public Smoothing() {
			this.internalCounter = 0;
		}
		
		/**
		 * Permet de calculer la Moyenne du temps d'execution des requetes
		 * 
		 * @param newMesurmentValue temps d'execution d'une requête.
		 * @return Moyenne du temps d'execution des requetes.

		 */
		public int calculateExponentialSmoothing(double newMesurmentValue) {
			
			if (internalCounter > 1) {
				oldSmoothedValue = ALPHA * observedValue + (1 - ALPHA) * oldSmoothedValue;					
				
			} else if (internalCounter == 1) {
				oldSmoothedValue = (observedValue + newMesurmentValue) / 2;
				
			} else {
				oldSmoothedValue = -1;				
			}	
			
			observedValue = newMesurmentValue;
			internalCounter++;
			
			return (int) Math.round(oldSmoothedValue);
		}

		/*private double[] alpha            = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		private double[] observedValue    = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		private double[] oldSmoothedValue = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		private int[] internalCounter     = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		
		private double[] cse              = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};*/
		
		/*public double foo(double newMesurmentValue, int i) {
			if (internalCounter[i] > 1) {
				oldSmoothedValue[i] = alpha[i] * observedValue[i] + (1 - alpha[i]) * oldSmoothedValue[i];
				
				cse[i] += (oldSmoothedValue[i] - newMesurmentValue) * (oldSmoothedValue[i] - newMesurmentValue);
				//System.out.println("								CSE of (" + alpha[i] + ") : " + cse[i]);				
				
			} else if (internalCounter[i] == 1) {
				oldSmoothedValue[i] = (observedValue[i] + newMesurmentValue) / 2;
				
			} else {
				oldSmoothedValue[i] = -1;				
			}	
			
			observedValue[i] = newMesurmentValue;
			internalCounter[i]++;
			
			return cse[i];
		}*/		
	}
	/**
	 * TODO -> verifier
	 * Permet de récuperer les requetes et de les répartir aux avm(s).
	 * 
	 * Met a jour le nombre de requetes et le temps correspondant au début de la requete 
	 * et permettant de faire la moyenne.
	 * 
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmission(RequestI)
	 * 
	 * @param r requete a soumettre a l'AVM
	 * @throws Exception
	 */
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		beginningTime.put(r.getRequestURI(), System.currentTimeMillis());
		
		this.rsop.get(getNextApplicationVM()).submitRequest(r);		
	}
	
	/**
	 * Permet de récuperer les requetes du requestGenerator et de les répartir aux avm(s).
	 * 
	 * Met a jour le nombre de requetes et le temps correspondant au début de la requete ( permettant de faire la moyenne) .
	 * 
	 * @see fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI#acceptRequestSubmissionAndNotify(RequestI)
	 * 
	 * @param r requete a soumettre a l'AVM
	 * @throws Exception
	 */
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
				
		beginningTime.put(r.getRequestURI(), System.currentTimeMillis());
		totalRequestSubmitted++;
		
		this.rsop.get(getNextApplicationVM()).submitRequestAndNotify(r);
	}
	
	/**
	 * Permet de savoir à quel avm on doit envoyer la requete.
	 * @return l'indice de l'avm a qui envoyer la requete.
	 */
	public int getNextApplicationVM() {
		
		return (applicationVMSelector++ % availableApplicationVm);		
	}	
	
	/**
	 * Permet de récuperer les requetes qui viennent de finir et de les renvoyés au requestGenerateur.
	 * 
	 * Met a jour le nombre de requetes et le temps correspondant a la fin de la requete 
	 * et lance le calcul de la moyenne du temps d'executions des requetes.
	 * 
	 * @see fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI#acceptRequestTerminationNotification(RequestI)
	 * 
	 * @param r requete
	 * @throws Exception
	 */
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {			
			this.logMessage("Request dispatcher " + this.rdURI + " notifying request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());					
		}		
				
		long beginning = beginningTime.get(r.getRequestURI());
		executionTime.put(r.getRequestURI(), System.currentTimeMillis() - beginning);
		
		//this.logMessage("							[Execution time of " + r.getRequestURI() + "] : " + executionTime.get(r.getRequestURI()));		
		
		long currentExecutionTime = executionTime.get(r.getRequestURI());
		
		synchronized (this) {
			exponentialSmoothing = smoothing.calculateExponentialSmoothing(currentExecutionTime);
			calculatedAverage = average.calculateAverage(currentExecutionTime);
		}
			
		//this.logMessage("							[Exponential smoothing] : " + exponentialSmoothing);
		//this.logMessage("							[Calculated average]    : " + calculatedAverage);
		
		totalRequestTerminated++;
		//this.logMessage("							[TOTAL] : " + totalRequestTerminated + "/" + totalRequestSubmitted);
		
		this.rnop.notifyRequestTermination(r);
	}
	
	/**
	 * Permet d'obtenir les informations dynamique du dispatcher (notamment ce qui est relatif a la moyenne du temps d'executions des requetes du dispatcher)
	 *  
	 * @return l'objet du type RequestDispatcherDynamicState
	 * @throws Exception
	 */
	public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
				
		return new RequestDispatcherDynamicState(rdURI, exponentialSmoothing, calculatedAverage, availableApplicationVm,
				totalRequestSubmitted, totalRequestTerminated);
	}	
	/**
	 * Permet d'envoyer les données dynamiques (RequestDispatcherDynamicState) toutes les millisecondes (interval) pour un nombre maximum d'envoi (numberOfPushes). 
	 * 
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startLimitedPushing(int, int)
	 * 
	 * @param interval délai entre les envoies
	 * @param numberOfPushes nombre maximum d'envoi de données dynamique
	 * @throws Exception
	 */
	@Override
	public void startLimitedPushing(int interval, int numberOfPushes) throws Exception {
		
		assert numberOfPushes > 0;

		this.logMessage(this.rdURI + " startLimitedPushing with interval " + interval + " ms for " + numberOfPushes 
				+ " times.");

		final RequestDispatcher rd = this;
		
		this.pushingFuture =
			this.scheduleTask(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState(interval, numberOfPushes);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}
	/**
	 * Envoie de données dynamiques.
	 * 
	 * @param interval délai entre les envoies
	 * @param numberOfRemainingPushes nombre maximum d'envoi de données dynamique
	 * @throws Exception
	 */
	public void	sendDynamicState(final int interval, int numberOfRemainingPushes) throws Exception {
			
		this.sendDynamicState();
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1;
		
		if (fNumberOfRemainingPushes > 0) {
			final RequestDispatcher rd = this;
			
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										rd.sendDynamicState(interval, fNumberOfRemainingPushes);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							},
							TimeManagement.acceleratedDelay(interval),
							TimeUnit.MILLISECONDS
					);
		}
	}
	
	/**
	 * Permet d'envoyer les données dynamiques (RequestDispatcherDynamicState) toutes les millisecondes (interval) sans restriction. 
	 * 
	 * 
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#startUnlimitedPushing(int)
	 * 
	 * @param interval délai entre les envoies
	 * @param numberOfPushes nombre maximum d'envoi de données dynamique
	 * @throws Exception
	 */
	@Override
	public void startUnlimitedPushing(int interval) throws Exception {		
		final RequestDispatcher rd = this;
		
		this.pushingFuture = 
				this.scheduleTaskAtFixedRate(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									rd.sendDynamicState();
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						},
						TimeManagement.acceleratedDelay(interval),
						TimeManagement.acceleratedDelay(interval),
						TimeUnit.MILLISECONDS
				);		
	}
	/**
	 * Envoie de données dynamiques.
	 * 
	 * @throws Exception
	 */
	public void sendDynamicState() throws Exception {
		
		if (this.rddsdip.connected()) {
			RequestDispatcherDynamicStateI rdds = this.getDynamicState();
			this.rddsdip.send(rdds);
		}
	}
	/**
	 * Arrête l'envoie de données dynamiques.
	 * 
	 * @see fr.upmc.datacenter.interfaces.PushModeControllingI#stopPushing()
	 * 
	 * @throws Exception
	 */
	@Override
	public void stopPushing() throws Exception {
		
		if (this.pushingFuture != null && !(this.pushingFuture.isCancelled() || this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false);
		}		
	}	
}
