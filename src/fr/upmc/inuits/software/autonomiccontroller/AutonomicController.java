package fr.upmc.inuits.software.autonomiccontroller;

import java.util.ArrayList;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.connectors.DataConnector;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.components.exceptions.ComponentStartException;
import fr.upmc.components.interfaces.DataRequiredI;
import fr.upmc.datacenter.connectors.ControlledDataConnector;
import fr.upmc.datacenter.hardware.computers.Computer;
import fr.upmc.datacenter.hardware.computers.Computer.AllocatedCore;
import fr.upmc.datacenter.hardware.computers.connectors.ComputerServicesConnector;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerDynamicStateI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerServicesI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStateDataConsumerI;
import fr.upmc.datacenter.hardware.computers.interfaces.ComputerStaticStateI;
import fr.upmc.datacenter.hardware.computers.ports.ComputerDynamicStateDataOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerServicesOutboundPort;
import fr.upmc.datacenter.hardware.computers.ports.ComputerStaticStateDataOutboundPort;
import fr.upmc.datacenter.interfaces.ControlledDataRequiredI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerServicesI;
import fr.upmc.inuits.software.autonomiccontroller.ports.AutonomicControllerManagementInboundPort;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherStateDataConsumerI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataOutboundPort;

public class AutonomicController 
	extends AbstractComponent 
	implements AutonomicControllerManagementI, AutonomicControllerServicesI, ComputerStateDataConsumerI, 
	           RequestDispatcherStateDataConsumerI {

	public static int DEBUG_LEVEL = 1;
	public static int ANALYSE_DATA_TIMER = 1000;//500
	
	protected final String atcURI;
	protected final int TOTAL_COMPUTERS_USED;
	
	protected ArrayList<String> computerServicesOutboundPortURI;
	protected ArrayList<String> computerStaticStateDataOutboundPortURI;
	protected ArrayList<String> computerDynamicStateDataOutboundPortURI;
	protected String requestDispatcherURI;
	protected String requestDispatcherDynamicStateDataOutboundPortURI;
	
	protected ComputerServicesOutboundPort[] csop;
	protected ComputerStaticStateDataOutboundPort[] cssdop;
	protected ComputerDynamicStateDataOutboundPort[] cdsdop;	
	
	protected RequestDispatcherDynamicStateDataOutboundPort rddsdop;
	protected AutonomicControllerManagementInboundPort atcmip;
	
	protected double averageExecutionTime;
	
	public AutonomicController(
			String atcURI,
			ArrayList<String> computersURI,			
			ArrayList<String> computerServicesOutboundPortURI,
			ArrayList<String> computerStaticStateDataOutboundPortURI,
			ArrayList<String> computerDynamicStateDataOutboundPortURI,			
			String requestDispatcherURI, 
			String requestDispatcherDynamicStateDataOutboundPortURI,
			String autonomicControllerManagementInboundPortURI)  throws Exception {
	
		super(atcURI, 1, 1);
		
		assert atcURI != null && atcURI.length() > 0;
		assert computersURI != null && computersURI.size() > 0;
		assert computerServicesOutboundPortURI != null && computerServicesOutboundPortURI.size() > 0;
		assert computerStaticStateDataOutboundPortURI != null && computerStaticStateDataOutboundPortURI.size() > 0;
		assert computerDynamicStateDataOutboundPortURI != null && computerDynamicStateDataOutboundPortURI.size() > 0;
		assert requestDispatcherDynamicStateDataOutboundPortURI != null 
				&& requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
		assert autonomicControllerManagementInboundPortURI != null;
				
		this.atcURI = atcURI;
		this.TOTAL_COMPUTERS_USED = computersURI.size();
		
		this.computerServicesOutboundPortURI = computerServicesOutboundPortURI;
		this.computerStaticStateDataOutboundPortURI = computerStaticStateDataOutboundPortURI;
		this.computerDynamicStateDataOutboundPortURI = computerDynamicStateDataOutboundPortURI;
		this.requestDispatcherDynamicStateDataOutboundPortURI = requestDispatcherDynamicStateDataOutboundPortURI;
		this.requestDispatcherURI = requestDispatcherURI; 
		
		this.csop = new ComputerServicesOutboundPort[TOTAL_COMPUTERS_USED];
		this.cssdop = new ComputerStaticStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		this.cdsdop = new ComputerDynamicStateDataOutboundPort[TOTAL_COMPUTERS_USED];
		
		this.addRequiredInterface(ComputerServicesI.class);
		// this.addOfferedInterface(ComputerStaticStateDataI.class); or :
		//this.addOfferedInterface(DataRequiredI.PushI.class);
		this.addRequiredInterface(DataRequiredI.PullI.class);
		this.addRequiredInterface(ControlledDataRequiredI.ControlledPullI.class);
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.csop[i] = new ComputerServicesOutboundPort(computerServicesOutboundPortURI.get(i), this);
			this.addPort(this.csop[i]);
			this.csop[i].publishPort();			
					
			this.cssdop[i] = new ComputerStaticStateDataOutboundPort(computerStaticStateDataOutboundPortURI.get(i), this, computersURI.get(i));
			this.addPort(this.cssdop[i]);
			this.cssdop[i].publishPort();
			
			this.cdsdop[i] = new ComputerDynamicStateDataOutboundPort(computerDynamicStateDataOutboundPortURI.get(i), this, computersURI.get(i));
			this.addPort(this.cdsdop[i]);
			this.cdsdop[i].publishPort();	
		}
			
		this.rddsdop = new RequestDispatcherDynamicStateDataOutboundPort(requestDispatcherDynamicStateDataOutboundPortURI, this, requestDispatcherURI);
		this.addPort(this.rddsdop);
		this.rddsdop.publishPort();
		
		this.addOfferedInterface(AutonomicControllerManagementI.class);
		this.atcmip = new AutonomicControllerManagementInboundPort(autonomicControllerManagementInboundPortURI, this);
		this.addPort(this.atcmip);
		this.atcmip.publishPort();
		
		assert this.atcURI != null;
		assert this.computerServicesOutboundPortURI != null && this.computerServicesOutboundPortURI.size() > 0;
		assert this.computerStaticStateDataOutboundPortURI != null && this.computerStaticStateDataOutboundPortURI.size() > 0;
		assert this.computerDynamicStateDataOutboundPortURI != null && this.computerDynamicStateDataOutboundPortURI.size() > 0;
		assert this.requestDispatcherDynamicStateDataOutboundPortURI != null && this.requestDispatcherDynamicStateDataOutboundPortURI.length() > 0;
		assert this.requestDispatcherURI != null && this.requestDispatcherURI.length() > 0;
		assert this.cssdop != null && this.cssdop[0] instanceof DataRequiredI.PullI; // or : ComputerStaticStateDataI
		assert this.cdsdop != null && this.cdsdop[0] instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.rddsdop != null && this.rddsdop instanceof ControlledDataRequiredI.ControlledPullI;
		assert this.atcmip != null && this.atcmip instanceof AutonomicControllerManagementI;
	}

	@Override
	public void start() throws ComponentStartException {
		
		super.start();			
											
	}
	
	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {
			for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
				if (this.csop[i].connected()) {
					this.csop[i].doDisconnection();
				}
				if (this.cssdop[i].connected()) {
					this.cssdop[i].doDisconnection();
				}
				if (this.cdsdop[i].connected()) {
					this.cdsdop[i].doDisconnection();
				}
			}
			if (this.rddsdop.connected()) {
				this.rddsdop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}
	
	@Override
	public void doConnectionWithComputerForServices(ArrayList<String> computerServicesInboundPortUri) throws Exception {

		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(				
					computerServicesOutboundPortURI.get(i),
					computerServicesInboundPortUri.get(i),
					ComputerServicesConnector.class.getCanonicalName());
		}
	}

	@Override
	public void doConnectionWithComputerForStaticState(ArrayList<String> computerStaticStateInboundPortUri) 
			throws Exception {
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(
					this.computerStaticStateDataOutboundPortURI.get(i),
					computerStaticStateInboundPortUri.get(i),
					DataConnector.class.getCanonicalName());
		}
	}
	
	@Override
	public void doConnectionWithComputerForDynamicState(ArrayList<String> computerDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
			this.doPortConnection(
					this.computerDynamicStateDataOutboundPortURI.get(i),
					computerDynamicStateInboundPortUri.get(i),
					ControlledDataConnector.class.getCanonicalName());
		}
		
		// start the pushing of dynamic state information from the computer if true.
		if (isStartPushing) {
			try {												
				for (int i = 0; i < TOTAL_COMPUTERS_USED; i++) {
					this.cdsdop[i].startUnlimitedPushing(ANALYSE_DATA_TIMER); 
				}			
														
			} catch (Exception e) {
				throw new ComponentStartException("Unable to start pushing dynamic data from the computer component.", e);
			}	
		}		
	}
	
	@Override
	public void doConnectionWithRequestDispatcherForDynamicState(String requestDispatcherDynamicStateInboundPortUri, 
			boolean isStartPushing) throws Exception {
		
		this.doPortConnection(
				this.requestDispatcherDynamicStateDataOutboundPortURI,
				requestDispatcherDynamicStateInboundPortUri,
				ControlledDataConnector.class.getCanonicalName());
		
		// start the pushing of dynamic state information from the request dispatcher.
		if (isStartPushing) {
			try {												
				this.rddsdop.startUnlimitedPushing(ANALYSE_DATA_TIMER);					
														
			} catch (Exception e) {
				throw new ComponentStartException("Unable to start pushing dynamic data from the request dispatcher "
						+ "component.", e);
			}
		}
	}
	
	@Override
	public void acceptComputerStaticData(String computerURI, ComputerStaticStateI staticState) throws Exception {
			
		//TODO
		
		if (AutonomicController.DEBUG_LEVEL == 3) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Autonomic controller accepting static data from " + computerURI + "\n");
			sb.append("  timestamp                     : " + staticState.getTimeStamp() + "\n");		   							
			sb.append("  timestamper id                : " + staticState.getTimeStamperId() + "\n");									
			sb.append("  number of processors          : " + staticState.getNumberOfProcessors() + "\n");			
			sb.append("  number of cores per processor : " + staticState.getNumberOfCoresPerProcessor() + "\n");									
			
			for (int p = 0; p < staticState.getNumberOfProcessors(); p++) {
				if (p == 0) {
					sb.append("  processor URIs                 : ");
					
				} else {
					sb.append("                                 : ");
				}
				sb.append(p + "  " + staticState.getProcessorURIs().get(p) + "\n");
			}
			
			sb.append("  processor port URIs            : " + "\n");
			sb.append(Computer.printProcessorsInboundPortURI(10, staticState.getNumberOfProcessors(), 
					staticState.getProcessorURIs(), staticState.getProcessorPortMap()));
			
			this.logMessage(sb.toString());
		}
	}

	@Override
	public void acceptComputerDynamicData(String computerURI, ComputerDynamicStateI currentDynamicState)
			throws Exception {
				
		//TODO					
		
		if (AutonomicController.DEBUG_LEVEL == 3) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("Autonomic controller accepting dynamic data from " + computerURI + "\n");
			sb.append("  timestamp                : " + currentDynamicState.getTimeStamp() + "\n");
			sb.append("  timestamper id           : " + currentDynamicState.getTimeStamperId() + "\n");
			
			boolean[][] reservedCores = currentDynamicState.getCurrentCoreReservations();
			for (int p = 0; p < reservedCores.length; p++) {
				if (p == 0) {
					sb.append("  reserved cores           : ");
					
				} else {
					sb.append("                             ");
				}								
						
				for (int c = 0; c < reservedCores[p].length; c++) {										 			
					if (reservedCores[p][c]) {
						sb.append("T ");
						
					} else {
						sb.append("F ");
					}
				}
			}
			
			this.logMessage(sb.toString());
		}			
	}
	
	@Override
	public void acceptRequestDispatcherDynamicData(String rdURI, RequestDispatcherDynamicStateI currentDynamicState)
			throws Exception {
		
		if (rdURI == this.requestDispatcherURI) {
			this.averageExecutionTime = currentDynamicState.getCurrentAverageExecutionTime();
			
			if (AutonomicController.DEBUG_LEVEL == 2) {
				StringBuffer sb = new StringBuffer();
				
				sb.append("Autonomic controller accepting dynamic data from " + rdURI + "\n");
				sb.append("  average execution time : [" + averageExecutionTime + "]\n");
				//sb.append("  current time millis : " + System.currentTimeMillis() + "\n");			
				
				this.logMessage(sb.toString());
			}
		}			
	}
	
	public void foo(int nbC) throws Exception {

		//TODO
		AllocatedCore[] ac = this.csop[0].allocateCores(nbC);		
	}

	@Override
	public void changeFrequency() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addCores() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeCores() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addAVM() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAVM() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
