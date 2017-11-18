package fr.upmc.inuits.software.requestdispatcher;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.software.interfaces.RequestI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionHandlerI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.datacenter.software.ports.RequestNotificationInboundPort;
import fr.upmc.datacenter.software.ports.RequestNotificationOutboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionInboundPort;
import fr.upmc.datacenter.software.ports.RequestSubmissionOutboundPort;
import fr.upmc.datacenterclient.utils.TimeProcessing;

public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestSubmissionHandlerI, RequestNotificationHandlerI {

	public static int DEBUG_LEVEL = 1;
	
	protected final String rdURI;
	protected final int AVAILABLE_APPLICATION_VM;
	protected int applicationVMCounter;
	
	protected RequestSubmissionInboundPort rsip;
	protected RequestSubmissionOutboundPort[] rsop;
	protected RequestNotificationInboundPort[] rnip;
	protected RequestNotificationOutboundPort rnop;
	
	public RequestDispatcher(
			String rdURI, 
			String requestSubmissionIntboundPortURI, 
			String[] requestSubmissionOutboundPortURI,
			String[] requestNotificationIntboundPortURI, 
			String requestNotificationOutboundPortURI) throws Exception {
		
		super(rdURI, 1, 1);
		
		assert rdURI != null;
		assert requestSubmissionIntboundPortURI != null && requestSubmissionIntboundPortURI.length() > 0;
		assert requestSubmissionOutboundPortURI != null && requestSubmissionOutboundPortURI.length > 0;
		assert requestNotificationIntboundPortURI != null && requestNotificationIntboundPortURI.length > 0;
		assert requestSubmissionOutboundPortURI.length == requestNotificationIntboundPortURI.length;
		assert requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0;
		
		this.rdURI = rdURI;
		this.AVAILABLE_APPLICATION_VM = requestSubmissionOutboundPortURI.length;
		this.applicationVMCounter = 0;		
		this.rsop = new RequestSubmissionOutboundPort[this.AVAILABLE_APPLICATION_VM];
		this.rnip = new RequestNotificationInboundPort[this.AVAILABLE_APPLICATION_VM];
				
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(requestSubmissionIntboundPortURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();			
		
		this.addRequiredInterface(RequestSubmissionI.class);
		this.addOfferedInterface(RequestNotificationI.class);
		
		for (int i = 0; i < this.AVAILABLE_APPLICATION_VM; i++) {
			this.rsop[i] = new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI[i], this);
			this.addPort(this.rsop[i]);
			this.rsop[i].publishPort();
			
			this.rnip[i] = new RequestNotificationInboundPort(requestNotificationIntboundPortURI[i], this);
			this.addPort(this.rnip[i]);
			this.rnip[i].publishPort();
		}				
		
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		
		assert this.rdURI != null;
		assert this.AVAILABLE_APPLICATION_VM == requestNotificationIntboundPortURI.length;
		assert this.applicationVMCounter == 0;
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rsop != null && this.rsop[0] instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip[0] instanceof RequestNotificationI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {
			for (int i = 0; i < rsop.length; i++) {
				if (this.rsop[i].connected()) {
					this.rsop[i].doDisconnection();
				}
			}			
			if (this.rnop.connected()) {
				this.rnop.doDisconnection();
			}
		} catch (Exception e) {			
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}	
	
	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		this.rsop[getNextApplicationVM()].submitRequest(r);		
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		this.rsop[getNextApplicationVM()].submitRequestAndNotify(r);
	}
	
	public int getNextApplicationVM() {
		
		return (applicationVMCounter++ % AVAILABLE_APPLICATION_VM);		
	}
	
	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {			
			this.logMessage("Request dispatcher " + this.rdURI + " notifying request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
				
		this.rnop.notifyRequestTermination(r); //TODO notify with the exact vm name
	}
}
