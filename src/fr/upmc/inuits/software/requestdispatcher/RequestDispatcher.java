package fr.upmc.inuits.software.requestdispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.upmc.components.AbstractComponent;
import fr.upmc.components.ComponentI;
import fr.upmc.components.exceptions.ComponentShutdownException;
import fr.upmc.datacenter.TimeManagement;
import fr.upmc.datacenter.interfaces.ControlledDataOfferedI;
import fr.upmc.datacenter.interfaces.PushModeControllingI;
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
import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;
import fr.upmc.inuits.software.requestdispatcher.ports.RequestDispatcherDynamicStateDataInboundPort;

public class RequestDispatcher 
	extends AbstractComponent 
	implements RequestSubmissionHandlerI, RequestNotificationHandlerI, PushModeControllingI {

	public static int DEBUG_LEVEL = 1;
	
	protected final String rdURI;
	protected final int AVAILABLE_APPLICATION_VM;
	protected int applicationVMCounter;
	
	protected RequestSubmissionInboundPort rsip;
	protected HashMap<Integer,RequestSubmissionOutboundPort> rsop;
	protected HashMap<Integer,RequestNotificationInboundPort> rnip;
	protected RequestNotificationOutboundPort rnop;
	
	protected RequestDispatcherDynamicStateDataInboundPort rddsdip;
	
	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;
	
	/** TODO */
	protected Smoothing smoothing; 
	protected double exponentialSmoothing;
	protected Average average;
	protected double calculatedAverage;
	
	/** TODO */
	protected int totalRequestSubmitted; 
	protected int totalRequestTerminated;
	
	public RequestDispatcher(
			String rdURI, 
			String requestSubmissionIntboundPortURI, 
			ArrayList<String> requestSubmissionOutboundPortURI,
			ArrayList<String> requestNotificationIntboundPortURI, 
			String requestNotificationOutboundPortURI,
			String requestDispatcherDynamicStateDataInboundPortURI) throws Exception {
		
		super(rdURI, 1, 1);
		
		assert rdURI != null;
		assert requestSubmissionIntboundPortURI != null && requestSubmissionIntboundPortURI.length() > 0;
		assert requestSubmissionOutboundPortURI != null && requestSubmissionOutboundPortURI.size() > 0;
		assert requestNotificationIntboundPortURI != null && requestNotificationIntboundPortURI.size() > 0;
		assert requestSubmissionOutboundPortURI.size() == requestNotificationIntboundPortURI.size();
		assert requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0;
		assert requestDispatcherDynamicStateDataInboundPortURI != null 
				&& requestDispatcherDynamicStateDataInboundPortURI.length() > 0;
		
		this.rdURI = rdURI;
		this.AVAILABLE_APPLICATION_VM = requestSubmissionOutboundPortURI.size();
		this.applicationVMCounter = 0;		
		this.rsop = new HashMap<>();
		this.rnip = new HashMap<>();
				
		this.addOfferedInterface(RequestSubmissionI.class);
		this.rsip = new RequestSubmissionInboundPort(requestSubmissionIntboundPortURI, this);
		this.addPort(this.rsip);
		this.rsip.publishPort();			
		
		this.addRequiredInterface(RequestSubmissionI.class);
		this.addOfferedInterface(RequestNotificationI.class);
		
		for (int i = 0; i < this.AVAILABLE_APPLICATION_VM; i++) {			
			this.rsop.put(i, new RequestSubmissionOutboundPort(requestSubmissionOutboundPortURI.get(i), this));
			this.addPort(this.rsop.get(i));
			this.rsop.get(i).publishPort();			
			
			this.rnip.put(i, new RequestNotificationInboundPort(requestNotificationIntboundPortURI.get(i), this));
			this.addPort(this.rnip.get(i));
			this.rnip.get(i).publishPort();
		}				
		
		this.addRequiredInterface(RequestNotificationI.class);
		this.rnop = new RequestNotificationOutboundPort(requestNotificationOutboundPortURI, this);
		this.addPort(this.rnop);
		this.rnop.publishPort();
		
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.rddsdip = 
				new RequestDispatcherDynamicStateDataInboundPort(requestDispatcherDynamicStateDataInboundPortURI, this);
		this.addPort(rddsdip);
		this.rddsdip.publishPort();
		
		this.smoothing = new Smoothing();
		this.average = new Average();
		this.totalRequestSubmitted = 0; 
		this.totalRequestTerminated = 0;
		
		assert this.rdURI != null;
		assert this.AVAILABLE_APPLICATION_VM == requestNotificationIntboundPortURI.size();
		assert this.applicationVMCounter == 0;
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rsop != null && this.rsop.get(0) instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip.get(0) instanceof RequestNotificationI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;		
		assert this.rddsdip != null && this.rddsdip instanceof ControlledDataOfferedI.ControlledPullI;
	}

	@Override
	public void shutdown() throws ComponentShutdownException {
		
		try {
			for (RequestSubmissionOutboundPort thisRsop : this.rsop.values()) {
				if (thisRsop.connected()) {
					thisRsop.doDisconnection();
				}
			}
			if (this.rnop.connected()) {				
				this.rnop.doDisconnection();							
			}			
			if (this.rddsdip.connected()) {
				this.rddsdip.doDisconnection();
			}
		} catch (Exception e) {			
			throw new ComponentShutdownException("Port disconnection error", e);
		}

		super.shutdown();
	}	
		
	protected HashMap<String, Long> beginningTime = new HashMap<>();
	protected HashMap<String, Long> executionTime = new HashMap<>();
	
	private class Average {
		
		private final int N = 4;
		private ArrayList<Double> observedValue;
		private int internalCounter;
		
		public Average() {
			this.observedValue = new ArrayList<>();
			this.internalCounter = 1;
		}
		
		public int calculateAverage(double newObservedValue) {
			
			double sum = 0.0;
			this.observedValue.add(newObservedValue);
			
			if (internalCounter >= N) {
				for (int i = internalCounter - N; i < observedValue.size(); i++) {					
					sum += observedValue.get(i);	
				}
			} 
						
			internalCounter++;
			
			return (int) Math.round(sum / N);
		}
	}
	
	private class Smoothing {
		
		private final double ALPHA = 0.7;		
		private double observedValue;
		private double oldSmoothedValue;		
		private int internalCounter;
		
		public Smoothing() {
			this.internalCounter = 0;
		}
		
		public int calculateExponentialSmoothing(double newMesurmentValue) {
			
			if (internalCounter > 1) {
				oldSmoothedValue = ALPHA * observedValue + (1 - ALPHA) * oldSmoothedValue;					
				
			} else if (internalCounter == 1) {
				oldSmoothedValue = (observedValue + newMesurmentValue) / 2;
				
			} else {
				oldSmoothedValue = -1;				
			}	
			
			observedValue = newMesurmentValue;
			internalCounter++;
			
			return (int) Math.round(oldSmoothedValue);
		}

		/*private double[] alpha            = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1};
		private double[] observedValue    = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		private double[] oldSmoothedValue = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		private int[] internalCounter     = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
		
		private double[] cse              = {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};*/
		
		/*public double foo(double newMesurmentValue, int i) {
			if (internalCounter[i] > 1) {
				oldSmoothedValue[i] = alpha[i] * observedValue[i] + (1 - alpha[i]) * oldSmoothedValue[i];
				
				cse[i] += (oldSmoothedValue[i] - newMesurmentValue) * (oldSmoothedValue[i] - newMesurmentValue);
				//System.out.println("								CSE of (" + alpha[i] + ") : " + cse[i]);				
				
			} else if (internalCounter[i] == 1) {
				oldSmoothedValue[i] = (observedValue[i] + newMesurmentValue) / 2;
				
			} else {
				oldSmoothedValue[i] = -1;				
			}	
			
			observedValue[i] = newMesurmentValue;
			internalCounter[i]++;
			
			return cse[i];
		}*/		
	}

	@Override
	public void acceptRequestSubmission(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
		
		beginningTime.put(r.getRequestURI(), System.currentTimeMillis());
		
		this.rsop.get(getNextApplicationVM()).submitRequest(r);		
	}	
	
	@Override
	public void acceptRequestSubmissionAndNotify(RequestI r) throws Exception {
		
		if (RequestDispatcher.DEBUG_LEVEL == 1) {
			this.logMessage(				
					"Request dispatcher " + this.rdURI + " submitting request " + r.getRequestURI() + " at " +
					TimeProcessing.toString(System.currentTimeMillis()) + " with number of instructions " + 
					r.getPredictedNumberOfInstructions());
		}
				
		beginningTime.put(r.getRequestURI(), System.currentTimeMillis());
		totalRequestSubmitted++;
		
		this.rsop.get(getNextApplicationVM()).submitRequestAndNotify(r);
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
				
		long beginning = beginningTime.get(r.getRequestURI());
		executionTime.put(r.getRequestURI(), System.currentTimeMillis() - beginning);
		
		//this.logMessage("							[Execution time of " + r.getRequestURI() + "] : " + executionTime.get(r.getRequestURI()));		
		
		long currentExecutionTime = executionTime.get(r.getRequestURI());
		
		synchronized (this) {
			exponentialSmoothing = smoothing.calculateExponentialSmoothing(currentExecutionTime);
			calculatedAverage = average.calculateAverage(currentExecutionTime);
		}
			
		this.logMessage("							[Exponential smoothing] : " + exponentialSmoothing);
		this.logMessage("							[Calculated average]    : " + calculatedAverage);
		
		totalRequestTerminated++;
		//this.logMessage("							[TOTAL] : " + totalRequestTerminated + "/" + totalRequestSubmitted);
		
		this.rnop.notifyRequestTermination(r);
	}
	
	public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
				
		//return new RequestDispatcherDynamicState(this.rdURI, exponentialSmoothing);
		return new RequestDispatcherDynamicState(this.rdURI, calculatedAverage);
	}	

	@Override
	public void startLimitedPushing(int interval, int numberOfPushes) throws Exception {
		
		assert numberOfPushes > 0;

		this.logMessage(this.rdURI + " startLimitedPushing with interval " + interval + " ms for " + numberOfPushes 
				+ " times.");

		final RequestDispatcher rd = this;
		
		this.pushingFuture =
			this.scheduleTask(
					new ComponentI.ComponentTask() {
						@Override
						public void run() {
							try {
								rd.sendDynamicState(interval, numberOfPushes);
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					},
					TimeManagement.acceleratedDelay(interval),
					TimeUnit.MILLISECONDS) ;
	}
	
	public void	sendDynamicState(final int interval, int numberOfRemainingPushes) throws Exception {
			
		this.sendDynamicState();
		final int fNumberOfRemainingPushes = numberOfRemainingPushes - 1;
		
		if (fNumberOfRemainingPushes > 0) {
			final RequestDispatcher rd = this;
			
			this.pushingFuture =
					this.scheduleTask(
							new ComponentI.ComponentTask() {
								@Override
								public void run() {
									try {
										rd.sendDynamicState(interval, fNumberOfRemainingPushes);
									} catch (Exception e) {
										throw new RuntimeException(e);
									}
								}
							},
							TimeManagement.acceleratedDelay(interval),
							TimeUnit.MILLISECONDS
					);
		}
	}
	
	@Override
	public void startUnlimitedPushing(int interval) throws Exception {		
		final RequestDispatcher rd = this;
		
		this.pushingFuture = 
				this.scheduleTaskAtFixedRate(
						new ComponentI.ComponentTask() {
							@Override
							public void run() {
								try {
									rd.sendDynamicState();
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						},
						TimeManagement.acceleratedDelay(interval),
						TimeManagement.acceleratedDelay(interval),
						TimeUnit.MILLISECONDS
				);		
	}
	
	public void sendDynamicState() throws Exception {
		
		if (this.rddsdip.connected()) {
			RequestDispatcherDynamicStateI rdds = this.getDynamicState();
			this.rddsdip.send(rdds);
		}
	}

	@Override
	public void stopPushing() throws Exception {
		
		if (this.pushingFuture != null && !(this.pushingFuture.isCancelled() || this.pushingFuture.isDone())) {
			this.pushingFuture.cancel(false);
		}		
	}
}
