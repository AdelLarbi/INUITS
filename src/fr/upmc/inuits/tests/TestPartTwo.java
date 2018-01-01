package fr.upmc.inuits.tests;

import fr.upmc.components.cvm.AbstractCVM;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.inuits.software.autonomiccontroller.AutonomicController;
import fr.upmc.inuits.software.requestdispatcher.RequestDispatcher;

public class TestPartTwo extends AbstractCVM {

	public static final String RD_DYNAMIC_STATE_DATA_IN_PORT_URI = "rddsd-ip";
	public static final String ATC_DYNAMIC_STATE_DATA_OUT_PORT_URI = "atcdsd-op";
	
	public static final String RD_URI = "rd0";
	
	protected RequestDispatcher requestDispatcher;
	protected AutonomicController autonomicController;
	
	public TestPartTwo() throws Exception {
		super();
	}
	
	@Override
	public void deploy() throws Exception {
		
		final String[] RD0_REQUEST_SUBMISSION_OUT_PORT_URI = {"rd1rs-op", "rd2rs-op"};
		final String[] RD0_REQUEST_NOTIFICATION_IN_PORT_URI = {"rd1rn-ip", "rd2rn-ip"};
		
		this.requestDispatcher = new RequestDispatcher(				
				RD_URI,				
				"111",
				RD0_REQUEST_SUBMISSION_OUT_PORT_URI,
				RD0_REQUEST_NOTIFICATION_IN_PORT_URI,
				"222",
				RD_DYNAMIC_STATE_DATA_IN_PORT_URI);
		
		this.addDeployedComponent(this.requestDispatcher);
		
		RequestDispatcher.DEBUG_LEVEL = 0;
		this.requestDispatcher.toggleTracing();
		this.requestDispatcher.toggleLogging();		
		// --------------------------------------------------------------------
		this.autonomicController = new AutonomicController(
				RD_URI, 
				ATC_DYNAMIC_STATE_DATA_OUT_PORT_URI);
		
		this.addDeployedComponent(this.autonomicController);
		
		AutonomicController.DEBUG_LEVEL = 2;
		this.autonomicController.toggleTracing();
		this.autonomicController.toggleLogging();
				
		this.autonomicController.doPortConnection(
				ATC_DYNAMIC_STATE_DATA_OUT_PORT_URI,
				RD_DYNAMIC_STATE_DATA_IN_PORT_URI,
				ControlledDataConnector.class.getCanonicalName());
		super.deploy();
	}		
	
	@Override
	public void shutdown() throws Exception {
				
		this.autonomicController.doPortDisconnection(ATC_DYNAMIC_STATE_DATA_OUT_PORT_URI);
		
		super.shutdown();
	}
	
	public void scenarioFoo() throws Exception {
		//TODO
	}
	
	public static void main(String[] args) {
		
		try {
			final TestPartTwo test = new TestPartTwo();
			test.deploy();
			
			System.out.println("starting...");
			test.start();
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						test.scenarioFoo();
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}).start();
					
			Thread.sleep(40000L);
			
			System.out.println("shutting down...");
			test.shutdown();
			
			System.out.println("ending...");
			System.exit(0);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
