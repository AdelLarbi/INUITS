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
/**
 * La class <code>Application</code> represente un composant application qui veut soumettre ses requetes aux Centre de calcul.
 */
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
	
	/**
	 * Permet de cree le composant Application.
	 * @param appURI Uri de l'application
	 * @param meanInterArrivalTime Temps entre 2 envoie de requetes
	 * @param meanNumberOfInstructions nombre d'instructions des requête a éxecuter en ms
	 * @param applicationManagementInboundPortURI
	 * @param applicationServicesInboundPortURI Uri de 
	 * @param applicationSubmissionOutboundPortURI
	 * @param applicationNotificationInboundPortURI 
	 * @throws Exception
	 * 
	 * pre meanInterArrivalTime > 0.0
	 * pre meanNumberOfInstructions > 0
	 * pre applicationManagementInboundPortURI != null
	 * pre applicationServicesInboundPortURI != null
	 * pre applicationSubmissionOutboundPortURI != null
	 * pre applicationNotificationInboundPortURI != null
	 * 
	 * post this.appURI != null && this.appURI.length() > 0
	 * post this.amip != null && this.amip instanceof ApplicationManagementI
	 * post this.asip != null && this.asip instanceof ApplicationServicesI
	 * post this.asop != null && this.asop instanceof ApplicationSubmissionI
	 * post this.anip != null && this.anip instanceof ApplicationNotificationI	
	 * post this.rgmop != null && this.rgmop instanceof RequestGeneratorManagementI
	 */
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
	
	/**
	 * Permet la preparation du composant RequestGenerator
	 * 
	 */
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
	
	/**
	 * Methode permettant l'arrêt du composant Application, en déconnectant les différents ports.
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
	
	/**
	 * Permet la creation dynamique du RequestGenerator
	 * @throws Exception
	 */
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
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) throws Exception {		
		
		rop.doPortConnection(
				this.rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				RequestSubmissionConnector.class.getCanonicalName());				
	}
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForSubmission(String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForSubmission(String dispatcherRequestSubmissionInboundPortUri) throws Exception {					
				
		this.rop.doPortConnection(
				this.rgRequestSubmissionOutboundPortURI,
				dispatcherRequestSubmissionInboundPortUri,
				Javassist.getRequestSubmissionConnectorClassName());		
	}	
	
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {			
		
		ropForRequestDispatcher.doPortConnection(
				dispatcherRequestNotificationOutboundPortUri,
				rgRequestNotificationInboundPortURI,
				RequestNotificationConnector.class.getCanonicalName());
	}
	
	/** 
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationManagementI#doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort, String)
	 */
	@Override
	public void doDynamicConnectionWithDispatcherForNotification(ReflectionOutboundPort ropForRequestDispatcher,
			String dispatcherRequestNotificationOutboundPortUri) throws Exception {			
		
		ropForRequestDispatcher.doPortConnection(
				dispatcherRequestNotificationOutboundPortUri,
				rgRequestNotificationInboundPortURI,
				Javassist.getRequestNotificationConnectorClassName());
	}
	
	/**
	* @see fr.upmc.inuits.software.application.interfaces.ApplicationServicesI#sendRequestForApplicationExecution(int)
	*/
	@Override
	public void	sendRequestForApplicationExecution(int coresToReserve) throws Exception {
						
		if (Application.DEBUG_LEVEL == 1) {	
			this.logMessage("Application " + this.appURI + " asking for execution permission.");
		}
		
		dynamicRequestGeneratorDeploy();
		
		this.asop.submitApplicationAndNotify(this.appURI, coresToReserve);
	}
	/**
	 * @see fr.upmc.inuits.software.application.interfaces.ApplicationNotificationHandlerI#acceptApplicationAdmissionNotification(boolean) 
	 */
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
	/**
	 * Méthode qui lance la generation de requêtes et qui la stop.
	 * @throws Exception
	 */
	public void	launch() throws Exception {						
		this.rgmop.doConnection(
				this.rgManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName());			
									
		this.rgmop.startGeneration();		
		this.rgmop.setMeanInterArrivalTime(300);
		
		Thread.sleep(40000L);				
		this.rgmop.stopGeneration();
	}
}
