package fr.upmc.inuits.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
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
	
	protected static String	FIRST_JVM_URI = "first";
	protected static String	SECOND_JVM_URI = "second";

	public static final String C_SERVICES_IN_PORT_URI = "cs-ip";
	public static final String C_SERVICES_OUT_PORT_URI = "cs-op";
	public static final String C_STATIC_STATE_DATA_IN_PORT_URI = "cssd-ip";
	public static final String C_DYNAMIC_STATE_DATA_IN_PORT_URI = "cdsd-ip";
	
	public static final String[] AVM_MANAGEMENT_IN_PORT_URI = {"a1m-ip", "a2m-ip", "a3m-ip", "a4m-ip"};
	public static final String[] AVM_MANAGEMENT_OUT_PORT_URI = {"a1m-op","a2m-op", "a3m-op", "a4m-op"};	
	public static final String[] AVM_REQUEST_SUBMISSION_IN_PORT_URI = {"a1rs-ip", "a2rs-ip", "a3rs-ip", "a4rs-ip"};
	public static final String[] AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = {"a1rn-op", "a2rn-op", "a3rn-op", "a4rn-op"};
	
	public static final String[] RD_REQUEST_SUBMISSION_IN_PORT_URI = {"rd1rs-ip", "rd2rs-ip"};
	public static final ArrayList<String> RD_REQUEST_SUBMISSION_OUT_PORT_URI = new ArrayList<>();
	public static final ArrayList<String> RD_REQUEST_NOTIFICATION_IN_PORT_URI = new ArrayList<>();
	public static final String[] RD_REQUEST_NOTIFICATION_OUT_PORT_URI = {"rd1rn-op", "rd2rn-op"};
	{
		RD_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd1rs-op");
		RD_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd2rs-op");
		RD_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd3rs-op");
		RD_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd4rs-op");
		
		RD_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd1rn-ip");
		RD_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd2rn-ip");
		RD_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd3rn-ip");
		RD_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd4rn-ip");
	}
	
	public static final String[] RG_MANAGEMENT_IN_PORT_URI = {"rg1m-ip", "rg2m-ip"};
	public static final String[] RG_MANAGEMENT_OUT_PORT_URI = {"rg1m-op", "rg2m-op"};
	public static final String[] RG_REQUEST_SUBMISSION_OUT_PORT_URI = {"rg1rs-op", "rg2rs-op"};
	public static final String[] RG_REQUEST_NOTIFICATION_IN_PORT_URI = {"rg1rn-ip", "rg2rn-ip"};
	
	protected ApplicationVM[] applicationVM;
	protected RequestGenerator[] requestGenerator;
	protected RequestDispatcher[] requestDispatcher;
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort[] avmOutPort;
	protected RequestGeneratorManagementOutboundPort[] rgmOutPort;
	
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
		
		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			this.applicationVM = new ApplicationVM[4];
			this.avmOutPort = new ApplicationVMManagementOutboundPort[4];
			
			Processor.DEBUG = true;
			// --------------------------------------------------------------------
			String computerURI = "computer0";
			int numberOfProcessors = 2;
			int numberOfCores = 4;
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
			for (int i = 0; i < 4; i++) {
				this.applicationVM[i] = new ApplicationVM(
						"vm" + i,
						AVM_MANAGEMENT_IN_PORT_URI[i],
					    AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
					    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);
				
				this.addDeployedComponent(this.applicationVM[i]);
				
				this.applicationVM[i].toggleTracing();
				this.applicationVM[i].toggleLogging();			
				// --------------------------------------------------------------------
				this.avmOutPort[i] = new ApplicationVMManagementOutboundPort(
						AVM_MANAGEMENT_OUT_PORT_URI[i],			
						new AbstractComponent(0, 0) {});
				
				this.avmOutPort[i].publishPort();							
			}		
			// --------------------------------------------------------------------
			assert computer != null && this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;			

		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {
			
			this.requestGenerator = new RequestGenerator[2];
			this.requestDispatcher = new RequestDispatcher[2];
			this.rgmOutPort = new RequestGeneratorManagementOutboundPort[2];
			
			for (int i = 0; i < 2; i++) {
				this.requestGenerator[i] = new RequestGenerator(				
						"rg" + i,
						500.0,
						6000000000L,
						RG_MANAGEMENT_IN_PORT_URI[i],
						RG_REQUEST_SUBMISSION_OUT_PORT_URI[i],
						RG_REQUEST_NOTIFICATION_IN_PORT_URI[i]);
				
				this.addDeployedComponent(this.requestGenerator[i]);
			
				RequestGenerator.DEBUG_LEVEL = 1;
				this.requestGenerator[i].toggleTracing();
				this.requestGenerator[i].toggleLogging();
				// --------------------------------------------------------------------
				this.rgmOutPort[i] = new RequestGeneratorManagementOutboundPort(				
						RG_MANAGEMENT_OUT_PORT_URI[i],
						new AbstractComponent(0, 0) {});
				
				this.rgmOutPort[i].publishPort();						
			}		
			// --------------------------------------------------------------------
			final ArrayList<String> RD0_REQUEST_SUBMISSION_OUT_PORT_URI = new ArrayList<>();
			final ArrayList<String> RD0_REQUEST_NOTIFICATION_IN_PORT_URI = new ArrayList<>();
			{
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd1rs-op");
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd2rs-op");
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd1rn-ip");
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd2rn-ip");
			}
						
			this.requestDispatcher[0] = new RequestDispatcher(				
					"rd" + 0,		
					"requestDispatcherManagementIntboundPortURI" + 0, //FIXME
					RD_REQUEST_SUBMISSION_IN_PORT_URI[0],
					RD0_REQUEST_SUBMISSION_OUT_PORT_URI,
					RD0_REQUEST_NOTIFICATION_IN_PORT_URI,
					RD_REQUEST_NOTIFICATION_OUT_PORT_URI[0],
					"requestDispatcherDynamicStateDataInboundPortURI" + 0); //FIXME
			
			this.addDeployedComponent(this.requestDispatcher[0]);
			
			RequestDispatcher.DEBUG_LEVEL = 1;
			this.requestDispatcher[0].toggleTracing();
			this.requestDispatcher[0].toggleLogging();
			
			final ArrayList<String> RD1_REQUEST_SUBMISSION_OUT_PORT_URI = new ArrayList<>();
			final ArrayList<String> RD1_REQUEST_NOTIFICATION_IN_PORT_URI = new ArrayList<>();
			{
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd3rs-op");
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI.add("rd4rs-op");
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd3rn-ip");
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI.add("rd4rn-ip");
			}
			
			this.requestDispatcher[1] = new RequestDispatcher(				
					"rd" + 1,				
					"requestDispatcherManagementIntboundPortURI" + 1, //FIXME
					RD_REQUEST_SUBMISSION_IN_PORT_URI[1],
					RD1_REQUEST_SUBMISSION_OUT_PORT_URI,
					RD1_REQUEST_NOTIFICATION_IN_PORT_URI,
					RD_REQUEST_NOTIFICATION_OUT_PORT_URI[1],
					"requestDispatcherDynamicStateDataInboundPortURI" + 1); //FIXME
			
			this.addDeployedComponent(this.requestDispatcher[1]);
			
			RequestDispatcher.DEBUG_LEVEL = 1;
			this.requestDispatcher[1].toggleTracing();
			this.requestDispatcher[1].toggleLogging();						
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

		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			assert this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;
			
			this.csOutPort.doConnection(
					C_SERVICES_IN_PORT_URI, 
					ComputerServicesConnector.class.getCanonicalName());
			
			for (int i = 0; i < 4; i++) {
				this.applicationVM[i].doPortConnection(
						AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
						RD_REQUEST_NOTIFICATION_IN_PORT_URI.get(i),
						RequestNotificationConnector.class.getCanonicalName());				
				
				this.avmOutPort[i].doConnection(
						AVM_MANAGEMENT_IN_PORT_URI[i],
						ApplicationVMManagementConnector.class.getCanonicalName());
			}											

			assert this.applicationVM[0].isPortConnected(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[0]);
			assert this.csOutPort.connected();
			assert this.avmOutPort[0].connected();
			
		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {

			assert this.requestGenerator != null && this.rgmOutPort != null && this.requestDispatcher != null;
			assert this.applicationVM == null && this.csOutPort == null && this.avmOutPort == null;
			
			for (int i = 0; i < 2; i++) {
				this.requestGenerator[i].doPortConnection(
						RG_REQUEST_SUBMISSION_OUT_PORT_URI[i],
						RD_REQUEST_SUBMISSION_IN_PORT_URI[i],				
						RequestSubmissionConnector.class.getCanonicalName());				
				
				this.requestDispatcher[i].doPortConnection(
						RD_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
						RG_REQUEST_NOTIFICATION_IN_PORT_URI[i],
						RequestSubmissionConnector.class.getCanonicalName());
				
				this.rgmOutPort[i].doConnection(
						RG_MANAGEMENT_IN_PORT_URI[i],
						RequestGeneratorManagementConnector.class.getCanonicalName());							
				
				this.requestDispatcher[0].doPortConnection(
						RD_REQUEST_SUBMISSION_OUT_PORT_URI.get(i),
						AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
						RequestSubmissionConnector.class.getCanonicalName());
			}																						
			for (int i = 2; i < 4; i++) {
				this.requestDispatcher[1].doPortConnection(
						RD_REQUEST_SUBMISSION_OUT_PORT_URI.get(i),
						AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
						RequestSubmissionConnector.class.getCanonicalName());
			}								

			assert this.requestGenerator[0].isPortConnected(RG_REQUEST_SUBMISSION_OUT_PORT_URI[0]);
			assert this.rgmOutPort[0].connected();
			assert this.requestDispatcher[0].isPortConnected(RD_REQUEST_NOTIFICATION_OUT_PORT_URI[0]);
			assert this.requestDispatcher[0].isPortConnected(RD_REQUEST_SUBMISSION_OUT_PORT_URI.get(0));			

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.interconnect();
	}
	
	@Override
	public void shutdown() throws Exception {
		
		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			assert this.applicationVM != null && this.csOutPort != null && this.avmOutPort != null;
			assert this.requestGenerator == null && this.rgmOutPort == null && this.requestDispatcher == null;
														
			this.csOutPort.doDisconnection();			
			for (int i = 0; i < 4; i++) {
				this.applicationVM[i].doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);			
				this.avmOutPort[i].doDisconnection();
			}
			
		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {

			assert this.requestGenerator != null && this.rgmOutPort != null && this.requestDispatcher != null;
			assert this.applicationVM == null && this.csOutPort == null && this.avmOutPort == null;			

			for (int i = 0; i < 2; i++) {
				this.requestGenerator[i].doPortDisconnection(RG_REQUEST_SUBMISSION_OUT_PORT_URI[i]);
				this.requestDispatcher[i].doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);
				this.requestDispatcher[0].doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI.get(i));
				this.rgmOutPort[i].doDisconnection();
			}
			for (int i = 2; i < 4; i++) {		
				this.requestDispatcher[1].doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI.get(i));
			}					
		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.shutdown();
	}

	@Override
	public void start() throws Exception {
		
		super.start();

		if (thisJVMURI.equals(FIRST_JVM_URI)) {			
			AllocatedCore[] ac = this.csOutPort.allocateCores(4);
			
			for (int i = 0; i < 4; i++) {
				this.avmOutPort[i].allocateCores(ac);
			}					
		}
	}	
	
	public void scenarioUniqueApplicationAndTwoAVMs() throws Exception {
		
		System.out.println("-- Scenario unique application and two AVMs.");
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			this.rgmOutPort[0].startGeneration();
			Thread.sleep(20000L);		
			this.rgmOutPort[0].stopGeneration();
		}
	}
	
	public void scenarioOneApplicationThenAnotherTwoAVMsEach() throws Exception {
		
		System.out.println("-- Scenario one application then another two AVMs each.");
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			this.rgmOutPort[0].startGeneration();
			Thread.sleep(20000L);		
			this.rgmOutPort[0].stopGeneration();
			
			Thread.sleep(10000L);
			
			this.rgmOutPort[1].startGeneration();
			Thread.sleep(15000L);		
			this.rgmOutPort[1].stopGeneration();
		}
	}
	
	public void scenarioTwoApplicationsSimultaneouslyTwoAVMsEach() throws Exception {
		
		System.out.println("-- Scenario two applications simultaneously two aVMs each.");
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			this.rgmOutPort[0].startGeneration();
			this.rgmOutPort[1].startGeneration();
			
			Thread.sleep(20000L);		
			
			this.rgmOutPort[0].stopGeneration();
			this.rgmOutPort[1].stopGeneration();			
		}
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
						switch (args[2]) {
						case "1":
							test.scenarioUniqueApplicationAndTwoAVMs();	
							break;
							
						case "2":
							test.scenarioOneApplicationThenAnotherTwoAVMsEach();
							break;

						case "3":
							test.scenarioTwoApplicationsSimultaneouslyTwoAVMsEach();
							break;
							
						default:
							System.out.println("Wrong argument. Please choose between 1, 2 or 3");
							System.exit(0);
							break;
						}						
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
