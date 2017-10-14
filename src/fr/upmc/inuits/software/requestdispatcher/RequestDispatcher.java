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

public class RequestDispatcher extends AbstractComponent implements RequestSubmissionHandlerI, RequestNotificationHandlerI {

	public static int DEBUG_LEVEL = 1;
	
	protected final String rdURI;
	
	protected RequestSubmissionInboundPort rsip;
	protected RequestSubmissionOutboundPort rsop;
	protected RequestNotificationInboundPort rnip;
	protected RequestNotificationOutboundPort rnop;
	
	public RequestDispatcher(
			String rdURI, 
			String RequestSubmissionIntboundPortURI, 
			String RequestSubmissionOutboundPortURI,
			String RequestNotificationIntboundPortURI, 
			String RequestNotificationOutboundPortURI) throws Exception {
		
		super(1, 1);
		
		assert rdURI != null;
		assert RequestSubmissionIntboundPortURI != null;
		assert RequestSubmissionOutboundPortURI != null;
		assert RequestNotificationIntboundPortURI != null;
		assert RequestNotificationOutboundPortURI != null;
		
		this.rdURI = rdURI;

		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(RequestSubmissionIntboundPortURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();
		
		this.addRequiredInterface(RequestSubmissionI.class);
		this.rsop = new RequestSubmissionOutboundPort(RequestSubmissionOutboundPortURI, this);
		this.addPort(this.rsop);
		this.rsop.publishPort();
		
		this.addOfferedInterface(RequestNotificationI.class);
		this.rnip = new RequestNotificationInboundPort(RequestNotificationIntboundPortURI, this);
		this.addPort(this.rnip);
		this.rnip.publishPort();
		
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(RequestNotificationOutboundPortURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		
		assert this.rdURI != null;
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rsop != null && this.rsop instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip instanceof RequestNotificationI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {
			/* Error : Attempt to disconnect a server component port rdrsip from a client component port 
			 * but from the server side; should be done from the client side!
			if (this.rsip.connected()) {
				this.rsip.doDisconnection();
			}
			*/
			if (this.rsop.connected()) {
				this.rsop.doDisconnection();
			}
			/* Error: Attempt to disconnect a server component port rdrnip from a client component port 
			 * but from the server side; should be done from the client side!
			if (this.rnip.connected()) {
				this.rnip.doDisconnection();
			}
			*/
			if (this.rnop.connected()) {
				this.rnop.doDisconnection();
			}
		} catch (Exception e) {
			throw new ComponentShutdownException(e);
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
		
		this.rsop.submitRequest(r);		
	}

	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		this.rsop.submitRequestAndNotify(r);
	}

	@Override
	public void acceptRequestTerminationNotification(RequestI r) throws Exception {
		// TODO Question au prof : pourquoi on à besoin d'une liste de tasksToNotify?
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {			
			this.logMessage("Request dispatcher " + this.rdURI + " notifying request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
				
		this.rnop.notifyRequestTermination(r);
	}
}
