package fr.upmc.inuits.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.datacenter.hardware.tests.ComputerMonitor;
import fr.upmc.datacenter.software.applicationvm.ApplicationVM;
import fr.upmc.datacenter.software.applicationvm.connectors.ApplicationVMManagementConnector;
import fr.upmc.datacenter.software.applicationvm.ports.ApplicationVMManagementOutboundPort;
import fr.upmc.datacenter.software.connectors.RequestNotificationConnector;
import fr.upmc.datacenter.software.connectors.RequestSubmissionConnector;
import fr.upmc.datacenterclient.requestgenerator.RequestGenerator;
import fr.upmc.datacenterclient.requestgenerator.connectors.RequestGeneratorManagementConnector;
import fr.upmc.datacenterclient.requestgenerator.ports.RequestGeneratorManagementOutboundPort;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class Test extends AbstractCVM {

	public static final String ComputerServicesInboundPortURI = "cs-ibp";
	public static final String ComputerServicesOutboundPortURI = "cs-obp";
	public static final String ComputerStaticStateDataInboundPortURI = "css-dip";
	public static final String ComputerStaticStateDataOutboundPortURI = "css-dop";
	public static final String ComputerDynamicStateDataInboundPortURI = "cds-dip";
	public static final String ComputerDynamicStateDataOutboundPortURI = "cds-dop";
	public static final String ApplicationVMManagementInboundPortURI = "avm-ibp";
	public static final String ApplicationVMManagementOutboundPortURI = "avm-obp";
	
	public static final String AVM_REQUEST_SUBMISSION_IN_PORT_URI = "arsip";
	public static final String AVM_REQUEST_NOTIFICATION_OUT_PORT_URI = "arnop";
	public static final String RD_REQUEST_SUBMISSION_IN_PORT_URI = "rdrsip";
	public static final String RD_REQUEST_SUBMISSION_OUT_PORT_URI = "rdrsop";
	public static final String RD_REQUEST_NOTIFICATION_IN_PORT_URI = "rdrnip";
	public static final String RD_REQUEST_NOTIFICATION_OUT_PORT_URI = "rdrnop";
	public static final String RG_REQUEST_SUBMISSION_OUT_PORT_URI = "rgrsop";
	public static final String RG_REQUEST_NOTIFICATION_IN_PORT_URI = "rgrnip";
	
	public static final String	RequestGeneratorManagementInboundPortURI = "rgmip";
	public static final String	RequestGeneratorManagementOutboundPortURI = "rgmop";
	
	protected ComputerServicesOutboundPort csOutPort;
	protected ComputerMonitor computerMonitor;
	protected ApplicationVM applicationVM;
	protected RequestGenerator requestGenerator;
	protected RequestDispatcher requestDispatcher;
	protected ApplicationVMManagementOutboundPort avmOutPort;
	protected RequestGeneratorManagementOutboundPort rgmOutPort;

	public Test() throws Exception {
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
				ComputerServicesInboundPortURI, 
				ComputerStaticStateDataInboundPortURI, 
				ComputerDynamicStateDataInboundPortURI);
		
		this.addDeployedComponent(computer);	
		// --------------------------------------------------------------------
		this.csOutPort = new ComputerServicesOutboundPort(
				ComputerServicesOutboundPortURI, 
				new AbstractComponent(0, 0){});
		this.csOutPort.publishPort();
		this.csOutPort.doConnection(
				ComputerServicesInboundPortURI, 
				ComputerServicesConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.computerMonitor = new ComputerMonitor(
				computerURI, 
				true, 
				ComputerStaticStateDataOutboundPortURI, 
				ComputerDynamicStateDataOutboundPortURI);
		
		this.addDeployedComponent(this.computerMonitor);
		
		this.computerMonitor.doPortConnection(
				ComputerStaticStateDataOutboundPortURI,
				ComputerStaticStateDataInboundPortURI,
				DataConnector.class.getCanonicalName());

		this.computerMonitor.doPortConnection(
				ComputerDynamicStateDataOutboundPortURI,
				ComputerDynamicStateDataInboundPortURI,
				ControlledDataConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.applicationVM = new ApplicationVM(
				"vm0",
			    ApplicationVMManagementInboundPortURI,
			    AVM_REQUEST_SUBMISSION_IN_PORT_URI,
			    AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(this.applicationVM);
		
		this.avmOutPort = new ApplicationVMManagementOutboundPort(
						ApplicationVMManagementOutboundPortURI,
						new AbstractComponent(0, 0) {});
		this.avmOutPort.publishPort();
		this.avmOutPort.doConnection(
				ApplicationVMManagementInboundPortURI,
				ApplicationVMManagementConnector.class.getCanonicalName());
		
		this.applicationVM.toggleTracing();
		this.applicationVM.toggleLogging();
		// --------------------------------------------------------------------
		this.requestGenerator = new RequestGenerator(				
				"rg",
				500.0,
				6000000000L,
				RequestGeneratorManagementInboundPortURI,
				RG_REQUEST_SUBMISSION_OUT_PORT_URI,
				RG_REQUEST_NOTIFICATION_IN_PORT_URI);
		
		this.addDeployedComponent(requestGenerator);
	
		//RequestGenerator.DEBUG_LEVEL = 2;
		this.requestGenerator.toggleTracing();
		this.requestGenerator.toggleLogging();
	
		this.requestGenerator.doPortConnection(
				RG_REQUEST_SUBMISSION_OUT_PORT_URI,
				AVM_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
	
		this.applicationVM.doPortConnection(
				AVM_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG_REQUEST_NOTIFICATION_IN_PORT_URI,
				RequestNotificationConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.rgmOutPort = new RequestGeneratorManagementOutboundPort(				
				RequestGeneratorManagementOutboundPortURI,
				new AbstractComponent(0, 0) {});
		
		this.rgmOutPort.publishPort();
		
		this.rgmOutPort.doConnection(
				RequestGeneratorManagementInboundPortURI,
				RequestGeneratorManagementConnector.class.getCanonicalName());
		// --------------------------------------------------------------------
		this.requestDispatcher = new RequestDispatcher(				
				"rd0",							
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RD_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_NOTIFICATION_IN_PORT_URI,
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
		
		this.addDeployedComponent(requestDispatcher);
		
		//this.requestDispatcher.toggleTracing();
		//this.requestDispatcher.toggleLogging();
		
		this.requestGenerator.doPortConnection(
				RG_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD_REQUEST_SUBMISSION_IN_PORT_URI,
				RequestSubmissionConnector.class.getCanonicalName());
		
		this.requestDispatcher.doPortConnection(
				RD_REQUEST_NOTIFICATION_OUT_PORT_URI,
				RG_REQUEST_NOTIFICATION_IN_PORT_URI,
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
		
		this.csOutPort.doDisconnection();
		this.avmOutPort.doDisconnection();
		this.computerMonitor.doPortDisconnection(ComputerStaticStateDataOutboundPortURI);
		this.computerMonitor.doPortDisconnection(ComputerDynamicStateDataOutboundPortURI);
		this.requestGenerator.doPortDisconnection(RG_REQUEST_SUBMISSION_OUT_PORT_URI);		
		this.applicationVM.doPortDisconnection(AVM_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.requestDispatcher.doPortDisconnection(RD_REQUEST_SUBMISSION_OUT_PORT_URI);
		this.requestDispatcher.doPortDisconnection(RD_REQUEST_NOTIFICATION_OUT_PORT_URI);
		this.rgmOutPort.doDisconnection();

		// print logs on files, if activated
		/*this.applicationVM.printExecutionLogOnFile("applicationVM");
		this.requestDispatcher.printExecutionLogOnFile("requestDispatcher");
		this.requestGenerator.printExecutionLogOnFile("requestGenerator");*/
		
		super.shutdown() ;
	}
	
	public void testScenario() throws Exception {
		
		this.rgmOutPort.startGeneration();
		Thread.sleep(20000L);		
		this.rgmOutPort.stopGeneration();
	}
	
	public static void main(String[] args) {
		
		try {
			final Test test = new Test();
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
			
			Thread.sleep(90000L) ;
			
			System.out.println("shutting down...");
			test.shutdown();
			
			System.out.println("ending...");
			System.exit(0);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
