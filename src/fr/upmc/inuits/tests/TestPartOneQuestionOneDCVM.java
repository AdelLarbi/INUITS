package fr.upmc.inuits.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class TestPartOneQuestionOneDCVM
	extends AbstractDistributedCVM {
	
	protected static String	PROVIDER_JVM_URI = "provider";
	protected static String	CONSUMER_JVM_URI = "consumer";

	public static final String C_SERVICES_IN_PORT_URI = "cs-ip";
	public static final String C_SERVICES_OUT_PORT_URI = "cs-op";
	public static final String C_STATIC_STATE_DATA_IN_PORT_URI = "cssd-ip";
	public static final String C_DYNAMIC_STATE_DATA_IN_PORT_URI = "cdsd-ip";
	
	public static final String AVM_MANAGEMENT_IN_PORT_URI = "am-ip";
	public static final String AVM_MANAGEMENT_OUT_PORT_URI = "am-op";	
	public static final String AVM_REQUEST_SUBMISSION_IN_PORT_URI = "ars-ip";
	public static final String AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = "arn-op";
	
	public static final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrs-ip";
	public static final String RD_REQUEST_SUBMISSION_OUT_PORT_URI = "rdrs-op";
	public static final String RD_REQUEST_NOTIFICATION_IN_PORT_URI = "rdrn-ip";
	public static final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrn-op";
	
	public static final String RG_MANAGEMENT_IN_PORT_URI = "rgm-ip";
	public static final String RG_MANAGEMENT_OUT_PORT_URI = "rgm-op";
	public static final String RG_REQUEST_SUBMISSION_OUT_PORT_URI = "rgrs-op";
	public static final String RG_REQUEST_NOTIFICATION_IN_PORT_URI = "rgrn-ip";
	
	protected ApplicationVM applicationVM;
	protected RequestGenerator requestGenerator;
	protected RequestDispatcher requestDispatcher;
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort avmOutPort;
	protected RequestGeneratorManagementOutboundPort rgmOutPort;
	
	public TestPartOneQuestionOneDCVM(String[] args) throws Exception {
		super(args);
	}
	
	@Override
	public void	initialise() throws Exception {
		super.initialise();
		// any other application-specific initialisation must be put here

		// logging configuration putting log files in the user current directory
		/*AbstractComponent.configureLogging(
			System.getProperty("user.home"),		// directory for log files
			"log",								// log files name extension
			4000,								// initial buffer size for logs
			'|') ;								// character separator between
												// time stamps and log messages

		// debugging mode configuration; comment and uncomment the line to see
		// the difference
		AbstractCVM.toggleDebugMode();*/
	}
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			Processor.DEBUG = true;
			// --------------------------------------------------------------------
			String computerURI = "computer0";
			int numberOfProcessors = 2;
			int numberOfCores = 2;
			Set<Integer> admissibleFrequencies = new HashSet<Integer>();
			admissibleFrequencies.add(1500);
			admissibleFrequencies.add(3000);
			Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>();
			processingPower.put(1500, 1500000);
			processingPower.put(3000, 3000000);
			
			Computer computer = new Computer(
					computerURI, 
					admissibleFrequencies, 
					processingPower, 
					1500, 
					1500, 
					numberOfProcessors, 
					numberOfCores, 
					C_SERVICES_IN_PORT_URI, 
					C_STATIC_STATE_DATA_IN_PORT_URI, 
					C_DYNAMIC_STATE_DATA_IN_PORT_URI);
			
			this.deployedComponents.add(computer);	
			
			this.csOutPort = new ComputerServicesOutboundPort(
					C_SERVICES_OUT_PORT_URI, 
					new AbstractComponent(0, 0){});
			this.csOutPort.publishPort();
			// --------------------------------------------------------------------
			this.applicationVM = new ApplicationVM(
					"vm0",
					AVM_MANAGEMENT_IN_PORT_URI,
				    AVM_REQUEST_SUBMISSION_IN_PORT_URI,
				    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
			
			this.deployedComponents.add(this.applicationVM);
			
			this.applicationVM.toggleTracing();
			this.applicationVM.toggleLogging();
			// --------------------------------------------------------------------
			this.avmOutPort = new ApplicationVMManagementOutboundPort(
							AVM_MANAGEMENT_OUT_PORT_URI,
							new AbstractComponent(0, 0) {});
			this.avmOutPort.publishPort();		
			// --------------------------------------------------------------------
			assert computer != null && this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;			

		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {
			
			this.requestGenerator = new RequestGenerator(
					"rg",
					500.0,
					6000000000L,
					RG_MANAGEMENT_IN_PORT_URI,
					RG_REQUEST_SUBMISSION_OUT_PORT_URI,
					RG_REQUEST_NOTIFICATION_IN_PORT_URI);
			
			this.deployedComponents.add(requestGenerator);
		
			RequestGenerator.DEBUG_LEVEL = 1;
			this.requestGenerator.toggleTracing();
			this.requestGenerator.toggleLogging();
			// --------------------------------------------------------------------
			this.rgmOutPort = new RequestGeneratorManagementOutboundPort(				
					RG_MANAGEMENT_OUT_PORT_URI,
					new AbstractComponent(0, 0) {});
			
			this.rgmOutPort.publishPort();						
			// --------------------------------------------------------------------		
			this.requestDispatcher = new RequestDispatcher(				
					"rd0",							
					RD_REQUEST_SUBMISSION_IN_PORT_URI,
					RD_REQUEST_SUBMISSION_OUT_PORT_URI,
					RD_REQUEST_NOTIFICATION_IN_PORT_URI,
					RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
			
			this.deployedComponents.add(requestDispatcher);
			
			RequestDispatcher.DEBUG_LEVEL = 1;
			this.requestDispatcher.toggleTracing();
			this.requestDispatcher.toggleLogging();						
			// --------------------------------------------------------------------
			assert this.requestGenerator != null && this.rgmOutPort != null && this.requestDispatcher != null;
			assert this.applicationVM == null && this.csOutPort == null && this.avmOutPort == null;

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.instantiateAndPublish();
	}

	@Override
	public void	interconnect() throws Exception {
		
		assert	this.instantiationAndPublicationDone;

		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;
			
			this.applicationVM.doPortConnection(
					AVM_REQUEST_NOTIFICATION_OUT_PORT_URI,
					RD_REQUEST_NOTIFICATION_IN_PORT_URI,
					RequestNotificationConnector.class.getCanonicalName());
			
			this.csOutPort.doConnection(
					C_SERVICES_IN_PORT_URI, 
					ComputerServicesConnector.class.getCanonicalName());
			
			this.avmOutPort.doConnection(
					AVM_MANAGEMENT_IN_PORT_URI,
					ApplicationVMManagementConnector.class.getCanonicalName());

			assert this.applicationVM.isPortConnected(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
			assert this.csOutPort.connected();
			assert this.avmOutPort.connected();
			
		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert this.requestGenerator != null && this.rgmOutPort != null && this.requestDispatcher != null;
			assert this.applicationVM == null && this.csOutPort == null && this.avmOutPort == null;
			
			this.requestGenerator.doPortConnection(
					RG_REQUEST_SUBMISSION_OUT_PORT_URI,
					RD_REQUEST_SUBMISSION_IN_PORT_URI,				
					RequestSubmissionConnector.class.getCanonicalName());				
			
			this.rgmOutPort.doConnection(
					RG_MANAGEMENT_IN_PORT_URI,
					RequestGeneratorManagementConnector.class.getCanonicalName());
			
			this.requestDispatcher.doPortConnection(
					RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
					RG_REQUEST_NOTIFICATION_IN_PORT_URI,
					RequestSubmissionConnector.class.getCanonicalName());
			
			this.requestDispatcher.doPortConnection(
					RD_REQUEST_SUBMISSION_OUT_PORT_URI,
					AVM_REQUEST_SUBMISSION_IN_PORT_URI,
					RequestSubmissionConnector.class.getCanonicalName());					

			assert this.requestGenerator.isPortConnected(RG_REQUEST_SUBMISSION_OUT_PORT_URI);
			assert this.rgmOutPort.connected();
			assert this.requestDispatcher.isPortConnected(RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
			assert this.requestDispatcher.isPortConnected(RD_REQUEST_SUBMISSION_OUT_PORT_URI);			

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.interconnect();
	}
	
	@Override
	public void shutdown() throws Exception {
		
		if (thisJVMURI.equals(PROVIDER_JVM_URI)) {

			assert this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;
									
			this.applicationVM.doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);			
			this.csOutPort.doDisconnection();			
			this.avmOutPort.doDisconnection();
			
		} else if (thisJVMURI.equals(CONSUMER_JVM_URI)) {

			assert this.requestGenerator != null && this.rgmOutPort != null && this.requestDispatcher != null;
			assert this.applicationVM == null && this.csOutPort == null && this.avmOutPort == null;			

			this.requestGenerator.doPortDisconnection(RG_REQUEST_SUBMISSION_OUT_PORT_URI);			
			this.requestDispatcher.doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI);
			this.requestDispatcher.doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI);			
			this.rgmOutPort.doDisconnection();
					
		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.shutdown();
	}

	void testScenario() throws Exception {
		
		this.rgmOutPort.startGeneration();
		Thread.sleep(20000L);		
		this.rgmOutPort.stopGeneration();
	}

	public static void	main(String[] args) {
		try {
			final TestPartOneQuestionOneDCVM test = new TestPartOneQuestionOneDCVM(args);
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.testScenario();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();
			
			Thread.sleep(90000L);
			
			System.out.println("shutting down...");
			test.shutdown();
			
			System.out.println("ending...");
			System.exit(0);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
