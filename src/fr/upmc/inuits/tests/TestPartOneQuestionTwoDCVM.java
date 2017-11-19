package fr.upmc.inuits.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.cvm.AbstractDistributedCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.processors.Processor;
import fr.upmc.inuits.software.admissioncontroller.AdmissionController;
import fr.upmc.inuits.software.application.Application;
import fr.upmc.inuits.software.application.connectors.ApplicationManagementConnector;
import fr.upmc.inuits.software.application.connectors.ApplicationNotificationConnector;
import fr.upmc.inuits.software.application.connectors.ApplicationServicesConnector;
import fr.upmc.inuits.software.application.connectors.ApplicationSubmissionConnector;
import fr.upmc.inuits.software.application.ports.ApplicationServicesOutboundPort;

public class TestPartOneQuestionTwoDCVM
	extends AbstractDistributedCVM {
	
	protected static String	FIRST_JVM_URI = "first";
	protected static String	SECOND_JVM_URI = "second";

	public static final String C_SERVICES_IN_PORT_URI = "cs-ip";
	public static final String C_STATIC_STATE_DATA_IN_PORT_URI = "cssd-ip";
	public static final String C_DYNAMIC_STATE_DATA_IN_PORT_URI = "cdsd-ip";
	
	public static final String[] AC_SERVICES_OUT_PORT_URI = {"acs-op"};
	public static final String[] AC_STATIC_STATE_DATA_OUT_PORT_URI = {"acssd-op"};
	public static final String[] AC_DYNAMIC_STATE_DATA_OUT_PORT_URI = {"acdsd-op"};
	public static final String[] AC_APPLICATION_MANAGEMENT_OUT_PORT_URI = {"a1cam-op", "a2cam-op"};
	public static final String[] AC_APPLICATION_SUBMISSION_IN_PORT_URI = {"a1cas-ip", "a2cas-ip"};
	public static final String[] AC_APPLICATION_NOTIFICATION_OUT_PORT_URI = {"acan-op", "a2can-op"};	
	
	public static final String A1_MANAGEMENT_IN_PORT_URI = "a1m-ip";
	public static final String A1_SERVICES_IN_PORT_URI = "a1s-ip";
	public static final String A1_APPLICATION_SUBMISSION_OUT_PORT_URI = "a1as-op";
	public static final String A1_APPLICATION_NOTIFICATION_IN_PORT_URI = "a1an-ip";
	
	public static final String A2_MANAGEMENT_IN_PORT_URI = "a2m-ip";
	public static final String A2_SERVICES_IN_PORT_URI = "a2s-ip";
	public static final String A2_APPLICATION_SUBMISSION_OUT_PORT_URI = "a2as-op";
	public static final String A2_APPLICATION_NOTIFICATION_IN_PORT_URI = "a2an-ip";
	
	public static final String A1_SERVICES_MOCKUPPORT_OUT_PORT_URI = "a1sm-op";
	public static final String A2_SERVICES_MOCKUPPORT_OUT_PORT_URI = "a2sm-op";
	
	protected AdmissionController admissionController;
	protected Application application1;	
	protected Application application2;
	protected ApplicationServicesOutboundPort asMockUpOutPort1;
	protected ApplicationServicesOutboundPort asMockUpOutPort2;
	
	public TestPartOneQuestionTwoDCVM(String[] args) throws Exception {
		super(args);
	}
	
	@Override
	public void	initialise() throws Exception {
		super.initialise();
	}
	
	@Override
	public void instantiateAndPublish() throws Exception {
		
		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			Processor.DEBUG = true;
			// --------------------------------------------------------------------
			String[] computersURI = new String[1];
			
			int numberOfProcessors = 5;
			int numberOfCores = 4;
			computersURI[0] = "computer0";		
			Set<Integer> admissibleFrequencies = new HashSet<Integer>();
			admissibleFrequencies.add(1500);
			admissibleFrequencies.add(3000);
			Map<Integer,Integer> processingPower = new HashMap<Integer,Integer>();
			processingPower.put(1500, 1500000);
			processingPower.put(3000, 3000000);
			
			
			Computer computer = new Computer(
					computersURI[0], 
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
			// --------------------------------------------------------------------
			this.admissionController = new AdmissionController(								
					computersURI,
					AC_SERVICES_OUT_PORT_URI,
					AC_STATIC_STATE_DATA_OUT_PORT_URI, 
					AC_DYNAMIC_STATE_DATA_OUT_PORT_URI,
					AC_APPLICATION_MANAGEMENT_OUT_PORT_URI,
					AC_APPLICATION_SUBMISSION_IN_PORT_URI,
					AC_APPLICATION_NOTIFICATION_OUT_PORT_URI);
			
			this.addDeployedComponent(this.admissionController);
			
			AdmissionController.DEBUG_LEVEL = 1;
			this.admissionController.toggleTracing();
			this.admissionController.toggleLogging();		
			// --------------------------------------------------------------------
			assert computer != null && this.admissionController != null;
			assert this.application1 == null && this.application2 == null && this.asMockUpOutPort1 == null && this.asMockUpOutPort1 == null;			

		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {
			
			this.application1 = new Application(				
					"app0",
					0,
					500.0,
					6000000000L,
					A1_MANAGEMENT_IN_PORT_URI,
					A1_SERVICES_IN_PORT_URI,
					A1_APPLICATION_SUBMISSION_OUT_PORT_URI,
					A1_APPLICATION_NOTIFICATION_IN_PORT_URI);
			
			this.addDeployedComponent(application1);
		
			Application.DEBUG_LEVEL = 1;
			this.application1.toggleTracing();
			this.application1.toggleLogging();
			
			this.asMockUpOutPort1 = new ApplicationServicesOutboundPort(				
					A1_SERVICES_MOCKUPPORT_OUT_PORT_URI,
					new AbstractComponent(0, 0) {});
			
			this.asMockUpOutPort1.publishPort();
			
			this.application2 = new Application(				
					"app1",
					1,
					500.0,
					6000000000L,
					A2_MANAGEMENT_IN_PORT_URI,
					A2_SERVICES_IN_PORT_URI,
					A2_APPLICATION_SUBMISSION_OUT_PORT_URI,
					A2_APPLICATION_NOTIFICATION_IN_PORT_URI);
			
			this.addDeployedComponent(application2);
		
			Application.DEBUG_LEVEL = 1;
			this.application2.toggleTracing();
			this.application2.toggleLogging();
			
			this.asMockUpOutPort2 = new ApplicationServicesOutboundPort(				
					A2_SERVICES_MOCKUPPORT_OUT_PORT_URI,
					new AbstractComponent(0, 0) {});
			
			this.asMockUpOutPort2.publishPort();
			// --------------------------------------------------------------------
			assert this.application1 != null && this.application2 != null && this.asMockUpOutPort1 != null && this.asMockUpOutPort1 != null;
			assert this.admissionController == null;			

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.instantiateAndPublish();
	}

	@Override
	public void	interconnect() throws Exception {
		
		assert this.instantiationAndPublicationDone;

		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			assert this.admissionController != null;
			assert this.application1 == null && this.application2 == null && this.asMockUpOutPort1 == null && this.asMockUpOutPort1 == null;
			
			this.admissionController.doPortConnection(				
					AC_SERVICES_OUT_PORT_URI[0],
					C_SERVICES_IN_PORT_URI,
					ComputerServicesConnector.class.getCanonicalName());
			
			this.admissionController.doPortConnection(
					AC_STATIC_STATE_DATA_OUT_PORT_URI[0],
					C_STATIC_STATE_DATA_IN_PORT_URI,
					DataConnector.class.getCanonicalName());

			this.admissionController.doPortConnection(
					AC_DYNAMIC_STATE_DATA_OUT_PORT_URI[0],
					C_DYNAMIC_STATE_DATA_IN_PORT_URI,
					ControlledDataConnector.class.getCanonicalName());
			
			this.admissionController.doPortConnection(				
					AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[0],
					A1_APPLICATION_NOTIFICATION_IN_PORT_URI,
					ApplicationNotificationConnector.class.getCanonicalName());
			
			this.admissionController.doPortConnection(				
					AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[0],
					A1_MANAGEMENT_IN_PORT_URI,
					ApplicationManagementConnector.class.getCanonicalName());
			
			this.admissionController.doPortConnection(				
					AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[1],
					A2_APPLICATION_NOTIFICATION_IN_PORT_URI,
					ApplicationNotificationConnector.class.getCanonicalName());
			
			this.admissionController.doPortConnection(				
					AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[1],
					A2_MANAGEMENT_IN_PORT_URI,
					ApplicationManagementConnector.class.getCanonicalName());

			assert this.admissionController.isPortConnected(AC_SERVICES_OUT_PORT_URI[0]);
			assert this.admissionController.isPortConnected(AC_STATIC_STATE_DATA_OUT_PORT_URI[0]);
			assert this.admissionController.isPortConnected(AC_DYNAMIC_STATE_DATA_OUT_PORT_URI[0]);
			assert this.admissionController.isPortConnected(AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[0]);
			assert this.admissionController.isPortConnected(AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[0]);
			assert this.admissionController.isPortConnected(AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[1]);
			assert this.admissionController.isPortConnected(AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[1]);
			
			
		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {

			assert this.application1 != null && this.application2 != null && this.asMockUpOutPort1 != null && this.asMockUpOutPort1 != null;
			assert this.admissionController == null;
			
			this.application1.doPortConnection(
					A1_APPLICATION_SUBMISSION_OUT_PORT_URI,
					AC_APPLICATION_SUBMISSION_IN_PORT_URI[0],
					ApplicationSubmissionConnector.class.getCanonicalName());
			
			this.asMockUpOutPort1.doConnection(
					A1_SERVICES_IN_PORT_URI,
					ApplicationServicesConnector.class.getCanonicalName());
			
			this.application2.doPortConnection(
					A2_APPLICATION_SUBMISSION_OUT_PORT_URI,
					AC_APPLICATION_SUBMISSION_IN_PORT_URI[1],
					ApplicationSubmissionConnector.class.getCanonicalName());
			
			this.asMockUpOutPort2.doConnection(
					A2_SERVICES_IN_PORT_URI,
					ApplicationServicesConnector.class.getCanonicalName());

			assert this.application1.isPortConnected(A1_APPLICATION_SUBMISSION_OUT_PORT_URI);
			assert this.asMockUpOutPort1.connected();
			assert this.application2.isPortConnected(A2_APPLICATION_SUBMISSION_OUT_PORT_URI);
			assert this.asMockUpOutPort2.connected();			

		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.interconnect();
	}
	
	@Override
	public void shutdown() throws Exception {
		
		if (thisJVMURI.equals(FIRST_JVM_URI)) {

			assert this.admissionController != null;
			assert this.application1 == null && this.application2 == null && this.asMockUpOutPort1 == null && this.asMockUpOutPort1 == null;
														
			this.admissionController.doPortDisconnection(AC_SERVICES_OUT_PORT_URI[0]);
			this.admissionController.doPortDisconnection(AC_STATIC_STATE_DATA_OUT_PORT_URI[0]);
			this.admissionController.doPortDisconnection(AC_DYNAMIC_STATE_DATA_OUT_PORT_URI[0]);
			this.admissionController.doPortDisconnection(AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[0]);
			this.admissionController.doPortDisconnection(AC_APPLICATION_MANAGEMENT_OUT_PORT_URI[1]);
			this.admissionController.doPortDisconnection(AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[0]);
			this.admissionController.doPortDisconnection(AC_APPLICATION_NOTIFICATION_OUT_PORT_URI[1]);
			
		} else if (thisJVMURI.equals(SECOND_JVM_URI)) {

			assert this.application1 != null && this.application2 != null && this.asMockUpOutPort1 != null && this.asMockUpOutPort1 != null;
			assert this.admissionController == null;			

			this.asMockUpOutPort1.doDisconnection();
			this.asMockUpOutPort2.doDisconnection();
			
		} else {
			System.out.println("Unknown JVM URI... " + thisJVMURI);
		}

		super.shutdown();
	}
	
	public void scenarioUniqueApplicationAndThreeAVMs_accept() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario unique application and two AVMs [ACCEPT]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(9);
		}
	}
	
	public void scenarioUniqueApplicationAndThreeAVMs_refuse() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario unique application and two AVMs [REFUSE]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(21);
		}
	}
		
	public void scenarioTwoApplicationsSimultaneouslyAndThreeAVMsEach_accept() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario one application then another two AVMs each [ACCEPT]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(9);
			this.asMockUpOutPort2.sendRequestForApplicationExecution(8);
		}
	}
	
	public void scenarioTwoApplicationsSimultaneouslyAndThreeAVMsEach_refuse() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario one application then another two AVMs each [REFUSE]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(14);
			this.asMockUpOutPort2.sendRequestForApplicationExecution(8);
		}
	}
	
	public void scenarioOneApplicationThenAnotherAndThreeAVMsEach_accept() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario two applications simultaneously two aVMs each [ACCEPT]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(9);
			Thread.sleep(5000L);
			this.asMockUpOutPort2.sendRequestForApplicationExecution(8);
		}
	}
	
	public void scenarioOneApplicationThenAnotherAndThreeAVMsEach_refuse() throws Exception {
		
		if (thisJVMURI.equals(SECOND_JVM_URI)) {
			System.out.println("-- Scenario two applications simultaneously two aVMs each [REFUSE]");
			this.asMockUpOutPort1.sendRequestForApplicationExecution(9);
			Thread.sleep(5000L);
			this.asMockUpOutPort2.sendRequestForApplicationExecution(15);
		}
	}
	
	public static void	main(String[] args) {
										
		try {
			final TestPartOneQuestionTwoDCVM test = new TestPartOneQuestionTwoDCVM(args);
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						switch (args[2]) {
						case "1":
							test.scenarioUniqueApplicationAndThreeAVMs_accept();								
							break;
							
						case "2":
							test.scenarioUniqueApplicationAndThreeAVMs_refuse();							
							break;

						case "3":
							test.scenarioTwoApplicationsSimultaneouslyAndThreeAVMsEach_accept();							
							break;
							
						case "4":
							test.scenarioTwoApplicationsSimultaneouslyAndThreeAVMsEach_refuse();							
							break;
							
						case "5":
							test.scenarioOneApplicationThenAnotherAndThreeAVMsEach_accept();							
							break;
							
						case "6":
							test.scenarioOneApplicationThenAnotherAndThreeAVMsEach_refuse();							
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
