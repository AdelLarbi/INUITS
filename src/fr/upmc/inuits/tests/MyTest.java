package fr.upmc.inuits.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
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
import fr.upmc.inuits.software.autonomiccontroller.AutonomicController;
import fr.upmc.inuits.software.autonomiccontroller.connectors.AutonomicControllerManagementConnector;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerManagementOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class MyTest extends AbstractCVM {

	public static final String C_SERVICES_OUT_PORT_URI = "cs-op";
	public static final ArrayList<String> C_SERVICES_IN_PORT_URI = new ArrayList<>();	
	public static final ArrayList<String> C_STATIC_STATE_DATA_IN_PORT_URI = new ArrayList<>();
	public static final ArrayList<String> C_DYNAMIC_STATE_DATA_IN_PORT_URI = new ArrayList<>();
	{
		C_SERVICES_IN_PORT_URI.add("cs-ip");
		C_STATIC_STATE_DATA_IN_PORT_URI.add("cssd-ip");
		C_DYNAMIC_STATE_DATA_IN_PORT_URI.add("cdsd-ip");
	}
	
	public static final String[] AVM_MANAGEMENT_IN_PORT_URI = {"a1m-ip", "a2m-ip"};
	public static final String[] AVM_MANAGEMENT_OUT_PORT_URI = {"a1m-op","a2m-op"};	
	public static final String[] AVM_REQUEST_SUBMISSION_IN_PORT_URI = {"a1rs-ip", "a2rs-ip"};
	public static final String[] AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = {"a1rn-op", "a2rn-op"};
	
	public static final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rd1rs-ip";
	public static final String[] RD_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd1rs-op", "rd2rs-op"};
	public static final String[] RD_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd1rn-ip", "rd2rn-ip"};
	public static final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rd1rn-op";	
	public static final String RD_DYNAMIC_STATE_DATA_IN_PORT_URI = "rddsd-ip";
	
	public static final String ATC_MANAGEMENT_IN_PORT_URI = "atcm-ip";
	public static final String ATC_MANAGEMENT_OUT_PORT_URI = "atcm-op";
	public static final ArrayList<String> ATC_SERVICES_OUT_PORT_URI = new ArrayList<>();
	public static final ArrayList<String> ATC_C_STATIC_STATE_DATA_OUT_PORT_URI = new ArrayList<>();
	public static final ArrayList<String> ATC_C_DYNAMIC_STATE_DATA_OUT_PORT_URI = new ArrayList<>();
	public static final String ATC_RD_DYNAMIC_STATE_DATA_OUT_PORT_URI = "atcrddsd-op";
	{
		ATC_SERVICES_OUT_PORT_URI.add("atcs-op");
		ATC_C_STATIC_STATE_DATA_OUT_PORT_URI.add("atccssd-op");
		ATC_C_DYNAMIC_STATE_DATA_OUT_PORT_URI.add("atccdsd-op");
	}
	
	public static final String RG_MANAGEMENT_IN_PORT_URI = "rg1m-ip";
	public static final String RG_MANAGEMENT_OUT_PORT_URI = "rg1m-op";
	public static final String RG_REQUEST_SUBMISSION_OUT_PORT_URI = "rg1rs-op";
	public static final String RG_REQUEST_NOTIFICATION_IN_PORT_URI = "rg1rn-ip";
	
	protected ApplicationVM[] applicationVM = new ApplicationVM[2];
	protected RequestGenerator requestGenerator;
	protected RequestDispatcher requestDispatcher;
	protected AutonomicController autonomicController;
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ApplicationVMManagementOutboundPort[] avmOutPort = new ApplicationVMManagementOutboundPort[2];
	protected RequestGeneratorManagementOutboundPort rgmOutPort;
	protected AutonomicControllerManagementOutboundPort atcmOutPort;

	public MyTest() throws Exception {
		super();
	}

	@Override
	public void deploy() throws Exception {
		
		//AbstractComponent.configureLogging(System.getProperty("user.home"), "log", 400, '|');
		//AbstractCVM.toggleDebugMode();
		//Processor.DEBUG = false;
		// --------------------------------------------------------------------
		ArrayList<String> computersURI = new ArrayList<>();
		computersURI.add("computer0");
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
				computersURI.get(0), 
				admissibleFrequencies, 
				processingPower, 
				6000,//1500 
				1500, 
				numberOfProcessors, 
				numberOfCores, 
				C_SERVICES_IN_PORT_URI.get(0), 
				C_STATIC_STATE_DATA_IN_PORT_URI.get(0), 
				C_DYNAMIC_STATE_DATA_IN_PORT_URI.get(0));
		
		this.addDeployedComponent(computer);	
		
		this.csOutPort = new ComputerServicesOutboundPort(
				C_SERVICES_OUT_PORT_URI, 
				new AbstractComponent(0, 0){});
		
		this.csOutPort.publishPort();
		
		this.csOutPort.doConnection(
				C_SERVICES_IN_PORT_URI.get(0), 
				ComputerServicesConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
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
		
		this.requestGenerator = new RequestGenerator(				
				"rg0",
				500.0,//500.0,
				6000000000L,
				RG_MANAGEMENT_IN_PORT_URI,
				RG_REQUEST_SUBMISSION_OUT_PORT_URI,
				RG_REQUEST_NOTIFICATION_IN_PORT_URI);
		
		this.addDeployedComponent(this.requestGenerator);
	
		RequestGenerator.DEBUG_LEVEL = 0;
		this.requestGenerator.toggleTracing();
		this.requestGenerator.toggleLogging();
		// --------------------------------------------------------------------
		this.rgmOutPort = new RequestGeneratorManagementOutboundPort(				
				RG_MANAGEMENT_OUT_PORT_URI,
				new AbstractComponent(0, 0) {});
		
		this.rgmOutPort.publishPort();
		
		this.rgmOutPort.doConnection(
				RG_MANAGEMENT_IN_PORT_URI,
				RequestGeneratorManagementConnector.class.getCanonicalName());						
		// --------------------------------------------------------------------					
		this.requestDispatcher = new RequestDispatcher(				
				"rd0",				
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RD_DYNAMIC_STATE_DATA_IN_PORT_URI);
		
		this.addDeployedComponent(this.requestDispatcher);
		
		RequestDispatcher.DEBUG_LEVEL = 0;
		this.requestDispatcher.toggleTracing();
		this.requestDispatcher.toggleLogging();						
		// --------------------------------------------------------------------
		this.autonomicController = new AutonomicController(
				computersURI,
				ATC_SERVICES_OUT_PORT_URI,
				ATC_C_STATIC_STATE_DATA_OUT_PORT_URI, 
				ATC_C_DYNAMIC_STATE_DATA_OUT_PORT_URI,
				"rd0", 
				ATC_RD_DYNAMIC_STATE_DATA_OUT_PORT_URI,
				ATC_MANAGEMENT_IN_PORT_URI);
		
		this.addDeployedComponent(this.autonomicController);
		
		AutonomicController.DEBUG_LEVEL = 2;
		this.autonomicController.toggleTracing();
		this.autonomicController.toggleLogging();
		// --------------------------------------------------------------------		
		this.atcmOutPort = new AutonomicControllerManagementOutboundPort(
				ATC_MANAGEMENT_OUT_PORT_URI, 
				new AbstractComponent(0, 0){});
		
		this.atcmOutPort.publishPort();
		
		this.atcmOutPort.doConnection(
				ATC_MANAGEMENT_IN_PORT_URI, 
				AutonomicControllerManagementConnector.class.getCanonicalName());
		// --------------------------------------------------------------------				
		atcmOutPort.doConnectionWithComputerForServices(C_SERVICES_IN_PORT_URI);
		atcmOutPort.doConnectionWithComputerForStaticState(C_STATIC_STATE_DATA_IN_PORT_URI);
		atcmOutPort.doConnectionWithComputerForDynamicState(C_DYNAMIC_STATE_DATA_IN_PORT_URI, true);
		atcmOutPort.doConnectionWithRequestDispatcherForDynamicState(RD_DYNAMIC_STATE_DATA_IN_PORT_URI, true);
		/*
		 this.autonomicController.doPortConnection(				
				ATC_SERVICES_OUT_PORT_URI.get(0),
				C_SERVICES_IN_PORT_URI.get(0),
				ComputerServicesConnector.class.getCanonicalName());
				
		 this.autonomicController.doPortConnection(
				ATC_C_STATIC_STATE_DATA_OUT_PORT_URI.get(0),
				C_STATIC_STATE_DATA_IN_PORT_URI,
				DataConnector.class.getCanonicalName());

		this.autonomicController.doPortConnection(
				ATC_C_DYNAMIC_STATE_DATA_OUT_PORT_URI.get(0),
				C_DYNAMIC_STATE_DATA_IN_PORT_URI,
				ControlledDataConnector.class.getCanonicalName());
		
		this.autonomicController.doPortConnection(
				ATC_RD_DYNAMIC_STATE_DATA_OUT_PORT_URI,
				RD_DYNAMIC_STATE_DATA_IN_PORT_URI,
				ControlledDataConnector.class.getCanonicalName());*/
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
			this.applicationVM[i].doPortConnection(
					AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i],
					RD_REQUEST_NOTIFICATION_IN_PORT_URI[i],
					RequestNotificationConnector.class.getCanonicalName());
		}	
		// --------------------------------------------------------------------		
		this.requestGenerator.doPortConnection(
				RG_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,				
				RequestSubmissionConnector.class.getCanonicalName());				
		
		this.requestDispatcher.doPortConnection(
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());								
		// --------------------------------------------------------------------
		for (int i = 0; i < 2; i++) {
			this.requestDispatcher.doPortConnection(
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
		
		for (int i = 0; i < 2; i++) {
			this.avmOutPort[i].allocateCores(ac);
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		
		for (int i = 0; i < 2; i++) {
			this.requestDispatcher.doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI[i]);
			this.applicationVM[i].doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI[i]);			
			this.avmOutPort[i].doDisconnection();
		}
		
		this.requestGenerator.doPortDisconnection(RG_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.requestDispatcher.doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI);				
		this.autonomicController.doPortDisconnection(ATC_SERVICES_OUT_PORT_URI.get(0));
		this.autonomicController.doPortDisconnection(ATC_C_STATIC_STATE_DATA_OUT_PORT_URI.get(0));
		this.autonomicController.doPortDisconnection(ATC_C_DYNAMIC_STATE_DATA_OUT_PORT_URI.get(0));
		this.autonomicController.doPortDisconnection(ATC_RD_DYNAMIC_STATE_DATA_OUT_PORT_URI);

		this.csOutPort.doDisconnection();
		this.rgmOutPort.doDisconnection();
		this.atcmOutPort.doDisconnection();

		super.shutdown();
	}
		
	public void scenarioUniqueApplicationAndTwoAVMs() throws Exception {
		
		System.out.println("-- Scenario unique application and two AVMs.");
		
		this.rgmOutPort.startGeneration();
		Thread.sleep(20000L);		
		this.rgmOutPort.stopGeneration();
	}	
	
	public static void main(String[] args) {
				
		try {
			final MyTest test = new MyTest();
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.scenarioUniqueApplicationAndTwoAVMs();	
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
