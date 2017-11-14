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
	
	public static final String AVM1_MANAGEMENT_IN_PORT_URI = "a1m-ip";
	public static final String AVM1_MANAGEMENT_OUT_PORT_URI = "a1m-op";	
	public static final String AVM1_REQUEST_SUBMISSION_IN_PORT_URI = "a1rs-ip";
	public static final String AVM1_REQUEST_NOTIFICATION_OUT_PORT_URI = "a1rn-op";
	
	public static final String AVM2_MANAGEMENT_IN_PORT_URI = "a2m-ip";
	public static final String AVM2_MANAGEMENT_OUT_PORT_URI = "a2m-op";	
	public static final String AVM2_REQUEST_SUBMISSION_IN_PORT_URI = "a2rs-ip";
	public static final String AVM2_REQUEST_NOTIFICATION_OUT_PORT_URI = "a2rn-op";
	
	/*public static final String AVM3_MANAGEMENT_IN_PORT_URI = "a3m-ip";
	public static final String AVM3_MANAGEMENT_OUT_PORT_URI = "a3m-op";	
	public static final String AVM3_REQUEST_SUBMISSION_IN_PORT_URI = "a3rs-ip";
	public static final String AVM3_REQUEST_NOTIFICATION_OUT_PORT_URI = "a3rn-op";*/
	
	public static final String RD1_REQUEST_SUBMISSION_IN_PORT_URI = "rd1rs-ip";
	public static String[] rd1RequestSubmissionOutPortUri = {"rd1.1rs-op","rd1.2rs-op"};
	public static String[] rd1RequestNotificationInPortUri = {"rd1.1rn-ip","rd1.2rn-ip"};
	public static final String RD1_REQUEST_NOTIFICATION_OUT_PORT_URI = "rd1rn-op";
	
	/*public static final String RD2_REQUEST_SUBMISSION_IN_PORT_URI = "rd2rs-ip";
	public static final String RD2_REQUEST_SUBMISSION_OUT_PORT_URI = "rd2rs-op";
	public static final String RD2_REQUEST_NOTIFICATION_IN_PORT_URI = "rd2rn-ip";
	public static final String RD2_REQUEST_NOTIFICATION_OUT_PORT_URI = "rd2rn-op";*/
	
	public static final String RG1_MANAGEMENT_IN_PORT_URI = "rg1m-ip";
	public static final String RG1_MANAGEMENT_OUT_PORT_URI = "rg1m1-op";
	public static final String RG1_REQUEST_SUBMISSION_OUT_PORT_URI = "rg1rs-op";
	public static final String RG1_REQUEST_NOTIFICATION_IN_PORT_URI = "rg1rn-ip";
	
	/*public static final String RG2_MANAGEMENT_IN_PORT_URI = "rg2m-ip";
	public static final String RG2_MANAGEMENT_OUT_PORT_URI = "rg2m-op";
	public static final String RG2_REQUEST_SUBMISSION_OUT_PORT_URI = "rg2rs-op";
	public static final String RG2_REQUEST_NOTIFICATION_IN_PORT_URI = "rg2rn-ip";*/
	
	protected ApplicationVM applicationVM1;
	protected ApplicationVM applicationVM2;
	//protected ApplicationVM applicationVM3;
	protected RequestGenerator requestGenerator1;
	//protected RequestGenerator requestGenerator2;
	protected RequestDispatcher requestDispatcher1;
	//protected RequestDispatcher requestDispatcher2;
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort avm1OutPort;
	protected ApplicationVMManagementOutboundPort avm2OutPort;
	//protected ApplicationVMManagementOutboundPort avm3OutPort;
	protected RequestGeneratorManagementOutboundPort rg1mOutPort;
	//protected RequestGeneratorManagementOutboundPort rg2mOutPort;

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
		
		this.addDeployedComponent(computer);	
		
		this.csOutPort = new ComputerServicesOutboundPort(
				C_SERVICES_OUT_PORT_URI, 
				new AbstractComponent(0, 0){});
		this.csOutPort.publishPort();
		this.csOutPort.doConnection(
				C_SERVICES_IN_PORT_URI, 
				ComputerServicesConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.applicationVM1 = new ApplicationVM(
				"vm1",
				AVM1_MANAGEMENT_IN_PORT_URI,
			    AVM1_REQUEST_SUBMISSION_IN_PORT_URI,
			    AVM1_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(this.applicationVM1);
		
		this.applicationVM1.toggleTracing();
		this.applicationVM1.toggleLogging();		
		// --------------------------------------------------------------------
				this.applicationVM2 = new ApplicationVM(
						"vm2",
						AVM2_MANAGEMENT_IN_PORT_URI,
					    AVM2_REQUEST_SUBMISSION_IN_PORT_URI,
					    AVM2_REQUEST_NOTIFICATION_OUT_PORT_URI);
				
				this.addDeployedComponent(this.applicationVM2);
				
				this.applicationVM2.toggleTracing();
				this.applicationVM2.toggleLogging();
		// --------------------------------------------------------------------
		/*this.applicationVM3 = new ApplicationVM(
				"vm3",
				AVM3_MANAGEMENT_IN_PORT_URI,
			    AVM3_REQUEST_SUBMISSION_IN_PORT_URI,
			    AVM3_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(this.applicationVM3);
		
		this.applicationVM3.toggleTracing();
		this.applicationVM3.toggleLogging();*/
		// -------------------------------------------------------------------
		this.avm1OutPort = new ApplicationVMManagementOutboundPort(
						AVM1_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avm1OutPort.publishPort();
		this.avm1OutPort.doConnection(
				AVM1_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());		
		// -------------------------------------------------------------------
		this.avm2OutPort = new ApplicationVMManagementOutboundPort(
						AVM2_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avm2OutPort.publishPort();
		this.avm2OutPort.doConnection(
				AVM2_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());		
		// -------------------------------------------------------------------
		/*this.avm3OutPort = new ApplicationVMManagementOutboundPort(
						AVM3_MANAGEMENT_OUT_PORT_URI,
						new AbstractComponent(0, 0) {});
		this.avm3OutPort.publishPort();
		this.avm3OutPort.doConnection(
				AVM3_MANAGEMENT_IN_PORT_URI,
				ApplicationVMManagementConnector.class.getCanonicalName());*/
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
		/*this.requestGenerator2 = new RequestGenerator(				
				"rg2",
				500.0,
				6000000000L,
				RG2_MANAGEMENT_IN_PORT_URI,
				RG2_REQUEST_SUBMISSION_OUT_PORT_URI,
				RG2_REQUEST_NOTIFICATION_IN_PORT_URI);
		
		this.addDeployedComponent(requestGenerator2);
	
		RequestGenerator.DEBUG_LEVEL = 1;
		this.requestGenerator2.toggleTracing();
		this.requestGenerator2.toggleLogging();*/
		// --------------------------------------------------------------------
		this.rg1mOutPort = new RequestGeneratorManagementOutboundPort(				
				RG1_MANAGEMENT_OUT_PORT_URI,
				new AbstractComponent(0, 0) {});
		
		this.rg1mOutPort.publishPort();
		
		this.rg1mOutPort.doConnection(
				RG1_MANAGEMENT_IN_PORT_URI,
				RequestGeneratorManagementConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		/*this.rg2mOutPort = new RequestGeneratorManagementOutboundPort(				
				RG2_MANAGEMENT_OUT_PORT_URI,
				new AbstractComponent(0, 0) {});
		
		this.rg2mOutPort.publishPort();
		
		this.rg2mOutPort.doConnection(
				RG2_MANAGEMENT_IN_PORT_URI,
				RequestGeneratorManagementConnector.class.getCanonicalName());*/
		// --------------------------------------------------------------------				
		// TODO tab of rd_sub & rd_notif
		this.requestDispatcher1 = new RequestDispatcher(				
				"rd1",							
				RD1_REQUEST_SUBMISSION_IN_PORT_URI,
				rd1RequestSubmissionOutPortUri,
				rd1RequestNotificationInPortUri,
				RD1_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(requestDispatcher1);
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		this.requestDispatcher1.toggleTracing();
		this.requestDispatcher1.toggleLogging();
		
		/*this.requestDispatcher2 = new RequestDispatcher(				
				"rd2",							
				RD2_REQUEST_SUBMISSION_IN_PORT_URI,
				RD2_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD2_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD2_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(requestDispatcher2);
		
		RequestDispatcher.DEBUG_LEVEL = 1;
		this.requestDispatcher2.toggleTracing();
		this.requestDispatcher2.toggleLogging();*/
		
		this.requestGenerator1.doPortConnection(
				RG1_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD1_REQUEST_SUBMISSION_IN_PORT_URI,				
				RequestSubmissionConnector.class.getCanonicalName());				
		
		this.requestDispatcher1.doPortConnection(
				RD1_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG1_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		/*this.requestGenerator2.doPortConnection(
				RG2_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD2_REQUEST_SUBMISSION_IN_PORT_URI,				
				RequestSubmissionConnector.class.getCanonicalName());				
		
		this.requestDispatcher2.doPortConnection(
				RD2_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG2_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());*/
		
		//for(int i = 0; i < rd1RequestSubmissionOutPortUri.length; i++) {
			this.requestDispatcher1.doPortConnection(
					rd1RequestSubmissionOutPortUri[0],
				AVM1_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());	
			
			this.requestDispatcher1.doPortConnection(
					rd1RequestSubmissionOutPortUri[1],
				AVM2_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		//}
		
		//for (int i = 0; i < rd1RequestNotificationInPortUri.length; i++) {
			this.applicationVM1.doPortConnection(
				AVM1_REQUEST_NOTIFICATION_OUT_PORT_URI,
				rd1RequestNotificationInPortUri[0],
				RequestNotificationConnector.class.getCanonicalName());	
			
			this.applicationVM2.doPortConnection(
				AVM2_REQUEST_NOTIFICATION_OUT_PORT_URI,
				rd1RequestNotificationInPortUri[1],
				RequestNotificationConnector.class.getCanonicalName());
		//}		
		
		/*this.requestDispatcher2.doPortConnection(
				RD2_REQUEST_SUBMISSION_OUT_PORT_URI,
				AVM3_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.applicationVM3.doPortConnection(
				AVM3_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RD2_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestNotificationConnector.class.getCanonicalName());*/
		// --------------------------------------------------------------------
		super.deploy();
	}
	
	@Override
	public void start() throws Exception {
		
		super.start();

		AllocatedCore[] ac = this.csOutPort.allocateCores(4);
		this.avm1OutPort.allocateCores(ac);
		this.avm2OutPort.allocateCores(ac);
		//this.avm3OutPort.allocateCores(ac);
	}
	
	@Override
	public void shutdown() throws Exception {
						
		this.requestGenerator1.doPortDisconnection(RG1_REQUEST_SUBMISSION_OUT_PORT_URI);
		//this.requestGenerator2.doPortDisconnection(RG2_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.applicationVM1.doPortDisconnection(AVM1_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.applicationVM2.doPortDisconnection(AVM2_REQUEST_NOTIFICATION_OUT_PORT_URI);
		//this.applicationVM3.doPortDisconnection(AVM3_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.requestDispatcher1.doPortDisconnection(rd1RequestSubmissionOutPortUri[0]);
		this.requestDispatcher1.doPortDisconnection(rd1RequestSubmissionOutPortUri[1]);
		this.requestDispatcher1.doPortDisconnection(RD1_REQUEST_NOTIFICATION_OUT_PORT_URI);
		//this.requestDispatcher2.doPortDisconnection(RD2_REQUEST_SUBMISSION_OUT_PORT_URI);
		//this.requestDispatcher2.doPortDisconnection(RD2_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.csOutPort.doDisconnection();
		this.rg1mOutPort.doDisconnection();
		//this.rg2mOutPort.doDisconnection();
		this.avm1OutPort.doDisconnection();
		this.avm2OutPort.doDisconnection();	
		//this.avm3OutPort.doDisconnection();

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
		
		/*this.rg2mOutPort.startGeneration();
		Thread.sleep(20000L);		
		this.rg2mOutPort.stopGeneration();*/
	}
	
	public void testScenario2() throws Exception {
		
		this.rg1mOutPort.startGeneration();
		//this.rg2mOutPort.startGeneration();
		Thread.sleep(15000L);		
		this.rg1mOutPort.stopGeneration();
		//this.rg2mOutPort.stopGeneration();						
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
