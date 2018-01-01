package fr.upmc.inuits.software.autonomiccontroller;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;

public class AutonomicController 
	extends AbstractComponent 
	implements RequestDispatcherStateDataConsumerI {

	public static int DEBUG_LEVEL = 1;
	public static int ANALYSE_DATA_TIMER = 500;
	
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	
	protected double averageExecutionTime;
	
	public AutonomicController(
			String requestDispatcherUri, 
			String requestDispatcherDynamicStateDataOutboundPortURI)  throws Exception {
	
		super(1, 1);
		
		assert requestDispatcherDynamicStateDataOutboundPortURI != null 
				&& requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
				
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);		
		this.rddsdop = new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPortURI, this, requestDispatcherUri);
		this.addPort(this.rddsdop);
		this.rddsdop.publishPort();
		
		assert this.rddsdop != null && this.rddsdop instanceof ControlledDataRequiredI.ControlledPullI;
	}

	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
				
		try {									
			// start the pushing of dynamic state information from the request dispatcher.
			this.rddsdop.startUnlimitedPushing(ANALYSE_DATA_TIMER);					
													
		} catch (Exception e) {
			throw new ComponentStartException("Unable to start pushing dynamic data from the request dispatcher "
					+ "component.", e);
		}		
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {		
			if (this.rddsdop.connected()) {
				this.rddsdop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String rdURI, RequestDispatcherDynamicStateI currentDynamicState)
			throws Exception {
		
		this.averageExecutionTime = currentDynamicState.getCurrentAverageExecutionTime();
		
		if (AutonomicController.DEBUG_LEVEL == 2) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Autonomic controller accepting dynamic data from " + rdURI + "\n");
			sb.append("  average execution time : " + averageExecutionTime + "\n");
			//sb.append("  current time millis : " + System.currentTimeMillis() + "\n");			
			
			this.logMessage(sb.toString());
		}		
	}
}
