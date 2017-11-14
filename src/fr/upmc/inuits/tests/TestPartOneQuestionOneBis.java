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

public class TestPartOneQuestionOneBis extends AbstractCVM {

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
	
	public static final String RG1_MANAGEMENT_IN_PORT_URI = "rg1m-ip";
	public static final String RG1_MANAGEMENT_OUT_PORT_URI = "rg1m1-op";
	public static final String RG1_REQUEST_SUBMISSION_OUT_PORT_URI = "rg1rs-op";
	public static final String RG1_REQUEST_NOTIFICATION_IN_PORT_URI = "rg1rn-ip";
	
	public static final String RG2_MANAGEMENT_IN_PORT_URI = "rg2m-ip";
	public static final String RG2_MANAGEMENT_OUT_PORT_URI = "rg2m-op";
	public static final String RG2_REQUEST_SUBMISSION_OUT_PORT_URI = "rg2rs-op";
	public static final String RG2_REQUEST_NOTIFICATION_IN_PORT_URI = "rg2rn-ip";
	
	protected ApplicationVM applicationVM;
	protected RequestGenerator requestGenerator1;
	protected RequestGenerator requestGenerator2;
	protected RequestDispatcher requestDispatcher;
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort avmOutPort;
	protected RequestGeneratorManagementOutboundPort rg1mOutPort;
	protected RequestGeneratorManagementOutboundPort rg2mOutPort;

	public TestPartOneQuestionOneBis() throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception {
		
		AbstractComponent.configureLogging(System.getProperty("user.home"), "log", 400, '|');
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
		
		this.addDeployedComponent(computer);	
		
		this.csOutPort = new ComputerServicesOutboundPort(
				C_SERVICES_OUT_PORT_URI, 
				new AbstractComponent(0, 0){});
		this.csOutPort.publishPort();
		this.csOutPort.doConnection(
				C_SERVICES_IN_PORT_URI, 
				ComputerServicesConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.applicationVM = new ApplicationVM(
				"vm0",
				AVM_MANAGEMENT_IN_PORT_URI,
			    AVM_REQUEST_SUBMISSION_IN_PORT_URI,
			    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(this.applicationVM);
		
		this.applicationVM.toggleTracing();
		this.applicationVM.toggleLogging();
		// --------------------------------------------------------------------
		this.avmOutPort = new ApplicationVMManagementOutboundPort(
						AVM_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avmOutPort.publishPort();
		this.avmOutPort.doConnection(
				AVM_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());		
		// --------------------------------------------------------------------
		this.requestGenerator1 = new RequestGenerator(				
				"rg1",
				500.0,
				6000000000L,
				RG1_MANAGEMENT_IN_PORT_URI,
				RG1_REQUEST_SUBMISSION_OUT_PORT_URI,
				RG1_REQUEST_NOTIFICATION_IN_PORT_URI);
		
		this.addDeployedComponent(requestGenerator1);
	
		RequestGenerator.DEBUG_LEVEL = 1;
		this.requestGenerator1.toggleTracing();
		this.requestGenerator1.toggleLogging();
		// --------------------------------------------------------------------
		this.requestGenerator2 = new RequestGenerator(				
				"rg2",
				500.0,
				6000000000L,
				RG2_MANAGEMENT_IN_PORT_URI,
				RG2_REQUEST_SUBMISSION_OUT_PORT_URI,
				RG2_REQUEST_NOTIFICATION_IN_PORT_URI);
		
		this.addDeployedComponent(requestGenerator2);
	
		RequestGenerator.DEBUG_LEVEL = 1;
		this.requestGenerator2.toggleTracing();
		this.requestGenerator2.toggleLogging();
		// --------------------------------------------------------------------
		this.rg1mOutPort = new RequestGeneratorManagementOutboundPort(				
				RG1_MANAGEMENT_OUT_PORT_URI,
				new AbstractComponent(0, 0) {});
		
		this.rg1mOutPort.publishPort();
		
		this.rg1mOutPort.doConnection(
				RG1_MANAGEMENT_IN_PORT_URI,
				RequestGeneratorManagementConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.rg2mOutPort = new RequestGeneratorManagementOutboundPort(				
				RG2_MANAGEMENT_OUT_PORT_URI,
				new AbstractComponent(0, 0) {});
		
		this.rg2mOutPort.publishPort();
		
		this.rg2mOutPort.doConnection(
				RG2_MANAGEMENT_IN_PORT_URI,
				RequestGeneratorManagementConnector.class.getCanonicalName());
		// --------------------------------------------------------------------		
		this.requestDispatcher = new RequestDispatcher(				
				"rd0",							
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(requestDispatcher);
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		this.requestDispatcher.toggleTracing();
		this.requestDispatcher.toggleLogging();
		
		this.requestGenerator1.doPortConnection(
				RG1_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,				
				RequestSubmissionConnector.class.getCanonicalName());				
		
		this.requestDispatcher.doPortConnection(
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG1_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.requestGenerator2.doPortConnection(
				RG2_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,				
				RequestSubmissionConnector.class.getCanonicalName());				
		
		this.requestDispatcher.doPortConnection(
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG2_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.requestDispatcher.doPortConnection(
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				AVM_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.applicationVM.doPortConnection(
				AVM_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestNotificationConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		super.deploy();
	}
	
	@Override
	public void start() throws Exception {
		
		super.start();

		AllocatedCore[] ac = this.csOutPort.allocateCores(4);
		this.avmOutPort.allocateCores(ac);
	}
	
	@Override
	public void shutdown() throws Exception {
						
		this.requestGenerator1.doPortDisconnection(RG1_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.requestGenerator2.doPortDisconnection(RG2_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.applicationVM.doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.requestDispatcher.doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.requestDispatcher.doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.csOutPort.doDisconnection();
		this.rg1mOutPort.doDisconnection();
		this.rg2mOutPort.doDisconnection();
		this.avmOutPort.doDisconnection();		

		// print logs on files, if activated
		/*this.applicationVM.printExecutionLogOnFile("applicationVM");
		this.requestDispatcher.printExecutionLogOnFile("requestDispatcher");
		this.requestGenerator.printExecutionLogOnFile("requestGenerator");*/
		
		super.shutdown();
	}
	
	public void testScenario1() throws Exception {
		
		this.rg1mOutPort.startGeneration();
		Thread.sleep(10000L);		
		this.rg1mOutPort.stopGeneration();
		
		this.rg2mOutPort.startGeneration();
		Thread.sleep(20000L);		
		this.rg2mOutPort.stopGeneration();
	}
	
	public void testScenario2() throws Exception {
		
		this.rg1mOutPort.startGeneration();
		this.rg2mOutPort.startGeneration();
		Thread.sleep(15000L);		
		this.rg1mOutPort.stopGeneration();
		this.rg2mOutPort.stopGeneration();						
	}
	
	public static void main(String[] args) {
		
		try {
			final TestPartOneQuestionOneBis test = new TestPartOneQuestionOneBis();
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.testScenario1();
						//test.testScenario2();
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
