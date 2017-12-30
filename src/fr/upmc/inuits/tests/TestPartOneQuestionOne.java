package fr.upmc.inuits.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class TestPartOneQuestionOne extends AbstractCVM {

	public static final String C_SERVICES_IN_PORT_URI = "cs-ip";
	public static final String C_SERVICES_OUT_PORT_URI = "cs-op";
	public static final String C_STATIC_STATE_DATA_IN_PORT_URI = "cssd-ip";
	public static final String C_DYNAMIC_STATE_DATA_IN_PORT_URI = "cdsd-ip";
	
	public static final String[] AVM_MANAGEMENT_IN_PORT_URI = {"a1m-ip", "a2m-ip", "a3m-ip", "a4m-ip"};
	public static final String[] AVM_MANAGEMENT_OUT_PORT_URI = {"a1m-op","a2m-op", "a3m-op", "a4m-op"};	
	public static final String[] AVM_REQUEST_SUBMISSION_IN_PORT_URI = {"a1rs-ip", "a2rs-ip", "a3rs-ip", "a4rs-ip"};
	public static final String[] AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = {"a1rn-op", "a2rn-op", "a3rn-op", "a4rn-op"};
	
	public static final String[] RD_REQUEST_SUBMISSION_IN_PORT_URI = {"rd1rs-ip", "rd2rs-ip"};
	public static final String[] RD_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd1rs-op", "rd2rs-op", "rd3rs-op", "rd4rs-op"};
	public static final String[] RD_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd1rn-ip", "rd2rn-ip", "rd3rn-ip", "rd4rn-ip"};
	public static final String[] RD_REQUEST_NOTIFICATION_OUT_PORT_URI = {"rd1rn-op", "rd2rn-op"};
	
	public static final String[] RG_MANAGEMENT_IN_PORT_URI = {"rg1m-ip", "rg2m-ip"};
	public static final String[] RG_MANAGEMENT_OUT_PORT_URI = {"rg1m-op", "rg2m-op"};
	public static final String[] RG_REQUEST_SUBMISSION_OUT_PORT_URI = {"rg1rs-op", "rg2rs-op"};
	public static final String[] RG_REQUEST_NOTIFICATION_IN_PORT_URI = {"rg1rn-ip", "rg2rn-ip"};
	
	protected ApplicationVM[] applicationVM = new ApplicationVM[4];
	protected RequestGenerator[] requestGenerator = new RequestGenerator[2];
	protected RequestDispatcher[] requestDispatcher = new RequestDispatcher[2];
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort[] avmOutPort = new ApplicationVMManagementOutboundPort[4];
	protected RequestGeneratorManagementOutboundPort[] rgmOutPort = new RequestGeneratorManagementOutboundPort[2];

	public TestPartOneQuestionOne() throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception {
		
		//AbstractComponent.configureLogging(System.getProperty("user.home"), "log", 400, '|');
		//AbstractCVM.toggleDebugMode();
		//Processor.DEBUG = false;
		// --------------------------------------------------------------------
		String computerURI = "computer0";
		int numberOfProcessors = 2;
		int numberOfCores = 4;
		Set<Integer> admissibleFrequencies = new HashSet<Integer>();
		admissibleFrequencies.add(1500);
		admissibleFrequencies.add(3000);
		admissibleFrequencies.add(6000);
		Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>();
		processingPower.put(1500, 1500000);
		processingPower.put(3000, 3000000);
		processingPower.put(6000, 6000000);
		
		Computer computer = new Computer(
				computerURI, 
				admissibleFrequencies, 
				processingPower, 
				1500,//1500 
				1500, 
				numberOfProcessors, 
				numberOfCores, 
				C_SERVICES_IN_PORT_URI, 
				C_STATIC_STATE_DATA_IN_PORT_URI, 
				C_DYNAMIC_STATE_DATA_IN_PORT_URI);
		
		this.addDeployedComponent(computer);	
		
		this.csOutPort = new ComputerServicesOutboundPort(
				C_SERVICES_OUT_PORT_URI, 
				new AbstractComponent(0, 0){});
		this.csOutPort.publishPort();
		this.csOutPort.doConnection(
				C_SERVICES_IN_PORT_URI, 
				ComputerServicesConnector.class.getCanonicalName());
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
			
			this.avmOutPort[i].doConnection(
							AVM_MANAGEMENT_IN_PORT_URI[i],
							ApplicationVMManagementConnector.class.getCanonicalName());					
		}
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
			this.requestGenerator[i] = new RequestGenerator(				
					"rg" + i,
					500.0,//500.0,
					6000000000L,
					RG_MANAGEMENT_IN_PORT_URI[i],
					RG_REQUEST_SUBMISSION_OUT_PORT_URI[i],
					RG_REQUEST_NOTIFICATION_IN_PORT_URI[i]);
			
			this.addDeployedComponent(this.requestGenerator[i]);
		
			RequestGenerator.DEBUG_LEVEL = 0;
			this.requestGenerator[i].toggleTracing();
			this.requestGenerator[i].toggleLogging();
			// --------------------------------------------------------------------
			this.rgmOutPort[i] = new RequestGeneratorManagementOutboundPort(				
					RG_MANAGEMENT_OUT_PORT_URI[i],
					new AbstractComponent(0, 0) {});
			
			this.rgmOutPort[i].publishPort();
			
			this.rgmOutPort[i].doConnection(
					RG_MANAGEMENT_IN_PORT_URI[i],
					RequestGeneratorManagementConnector.class.getCanonicalName());			
		}		
		// --------------------------------------------------------------------
		final String[] RD0_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd1rs-op", "rd2rs-op"};
		final String[] RD0_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd1rn-ip", "rd2rn-ip"};
					
		this.requestDispatcher[0] = new RequestDispatcher(				
				"rd" + 0,				
				RD_REQUEST_SUBMISSION_IN_PORT_URI[0],
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI[0]);
		
		this.addDeployedComponent(this.requestDispatcher[0]);
		
		RequestDispatcher.DEBUG_LEVEL = 0;
		this.requestDispatcher[0].toggleTracing();
		this.requestDispatcher[0].toggleLogging();
		
		final String[] RD1_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd3rs-op", "rd4rs-op"};
		final String[] RD1_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd3rn-ip", "rd4rn-ip"};
		
		this.requestDispatcher[1] = new RequestDispatcher(				
				"rd" + 1,				
				RD_REQUEST_SUBMISSION_IN_PORT_URI[1],
				RD1_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD1_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI[1]);
		
		this.addDeployedComponent(this.requestDispatcher[1]);
		
		RequestDispatcher.DEBUG_LEVEL = 0;
		this.requestDispatcher[1].toggleTracing();
		this.requestDispatcher[1].toggleLogging();
		// --------------------------------------------------------------------
		for (int i = 0; i < 4; i++) {
			this.applicationVM[i].doPortConnection(
					AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
					RD_REQUEST_NOTIFICATION_IN_PORT_URI[i],
					RequestNotificationConnector.class.getCanonicalName());
		}	
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
			this.requestGenerator[i].doPortConnection(
					RG_REQUEST_SUBMISSION_OUT_PORT_URI[i],
					RD_REQUEST_SUBMISSION_IN_PORT_URI[i],				
					RequestSubmissionConnector.class.getCanonicalName());				
			
			this.requestDispatcher[i].doPortConnection(
					RD_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
					RG_REQUEST_NOTIFICATION_IN_PORT_URI[i],
					RequestSubmissionConnector.class.getCanonicalName());						
		}				
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
			this.requestDispatcher[0].doPortConnection(
					RD_REQUEST_SUBMISSION_OUT_PORT_URI[i],
					AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
					RequestSubmissionConnector.class.getCanonicalName());
		}		
		for (int i = 2; i < 4; i++) {
			this.requestDispatcher[1].doPortConnection(
					RD_REQUEST_SUBMISSION_OUT_PORT_URI[i],
					AVM_REQUEST_SUBMISSION_IN_PORT_URI[i],
					RequestSubmissionConnector.class.getCanonicalName());
		}
		// --------------------------------------------------------------------
		super.deploy();
	}
	
	@Override
	public void start() throws Exception {
		
		super.start();

		AllocatedCore[] ac = this.csOutPort.allocateCores(4);
		
		for (int i = 0; i < 4; i++) {
			this.avmOutPort[i].allocateCores(ac);
		}
	}
	
	@Override
	public void shutdown() throws Exception {		
		this.csOutPort.doDisconnection();
		
		for (int i = 0; i < 4; i++) {
			this.applicationVM[i].doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);			
			this.avmOutPort[i].doDisconnection();
		}
		for (int i = 0; i < 2; i++) {
			this.requestGenerator[i].doPortDisconnection(RG_REQUEST_SUBMISSION_OUT_PORT_URI[i]);
			this.requestDispatcher[i].doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);
			this.requestDispatcher[0].doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI[i]);
			this.rgmOutPort[i].doDisconnection();
		}
		for (int i = 2; i < 4; i++) {		
			this.requestDispatcher[1].doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI[i]);
		}
		super.shutdown();
	}
	
	public void scenarioUniqueApplicationAndTwoAVMs() throws Exception {
		
		System.out.println("-- Scenario unique application and two AVMs.");
		
		this.rgmOutPort[0].startGeneration();
		Thread.sleep(20000L);		
		this.rgmOutPort[0].stopGeneration();
	}
	
	public void scenarioOneApplicationThenAnotherTwoAVMsEach() throws Exception {
		
		System.out.println("-- Scenario one application then another two AVMs each.");
		
		this.rgmOutPort[0].startGeneration();
		Thread.sleep(20000L);		
		this.rgmOutPort[0].stopGeneration();
		
		Thread.sleep(10000L);
		
		this.rgmOutPort[1].startGeneration();
		Thread.sleep(15000L);		
		this.rgmOutPort[1].stopGeneration();
	}
	
	public void scenarioTwoApplicationsSimultaneouslyTwoAVMsEach() throws Exception {
		
		System.out.println("-- Scenario two applications simultaneously two aVMs each.");
		
		this.rgmOutPort[0].startGeneration();
		this.rgmOutPort[1].startGeneration();
		
		Thread.sleep(20000L);		
		
		this.rgmOutPort[0].stopGeneration();
		this.rgmOutPort[1].stopGeneration();			
	}
	
	public static void main(String[] args) {
		
		try {
			final TestPartOneQuestionOne test = new TestPartOneQuestionOne();
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.scenarioUniqueApplicationAndTwoAVMs();
						//test.scenarioOneApplicationThenAnotherTwoAVMsEach();
						//test.scenarioTwoApplicationsSimultaneouslyTwoAVMsEach();
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
