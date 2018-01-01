package fr.upmc.inuits.software.requestdispatcher;

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
	protected RequestSubmissionOutboundPort[] rsop;
	protected RequestNotificationInboundPort[] rnip;
	protected RequestNotificationOutboundPort rnop;
	
	protected RequestDispatcherDynamicStateDataInboundPort rddsdip;
	
	/** future of the task scheduled to push dynamic data. */
	protected ScheduledFuture<?> pushingFuture;
	
	/** TODO */
	protected Smoothing smoothing; 
	protected double exponentialSmoothing;
	
	public RequestDispatcher(
			String rdURI, 
			String requestSubmissionIntboundPortURI, 
			String[] requestSubmissionOutboundPortURI,
			String[] requestNotificationIntboundPortURI, 
			String requestNotificationOutboundPortURI,
			String requestDispatcherDynamicStateDataInboundPortURI) throws Exception {
		
		super(rdURI, 1, 1);
		
		assert rdURI != null;
		assert requestSubmissionIntboundPortURI != null && requestSubmissionIntboundPortURI.length() > 0;
		assert requestSubmissionOutboundPortURI != null && requestSubmissionOutboundPortURI.length > 0;
		assert requestNotificationIntboundPortURI != null && requestNotificationIntboundPortURI.length > 0;
		assert requestSubmissionOutboundPortURI.length == requestNotificationIntboundPortURI.length;
		assert requestNotificationOutboundPortURI != null && requestNotificationOutboundPortURI.length() > 0;
		assert requestDispatcherDynamicStateDataInboundPortURI != null 
				&& requestDispatcherDynamicStateDataInboundPortURI.length() > 0;
		
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
		
		this.addOfferedInterface(ControlledDataOfferedI.ControlledPullI.class);
		this.rddsdip = 
				new RequestDispatcherDynamicStateDataInboundPort(requestDispatcherDynamicStateDataInboundPortURI, this);
		this.addPort(rddsdip);
		this.rddsdip.publishPort();
		
		this.smoothing = new Smoothing();
		
		assert this.rdURI != null;
		assert this.AVAILABLE_APPLICATION_VM == requestNotificationIntboundPortURI.length;
		assert this.applicationVMCounter == 0;
		assert this.rsip != null && this.rsip instanceof RequestSubmissionI;
		assert this.rsop != null && this.rsop[0] instanceof RequestSubmissionI;
		assert this.rnip != null && this.rnip[0] instanceof RequestNotificationI;
		assert this.rnop != null && this.rnop instanceof RequestNotificationI;		
		assert this.rddsdip != null && this.rddsdip instanceof ControlledDataOfferedI.ControlledPullI;
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
	protected double[] exponentialSmoothing_3 = new double[100];
	/*protected double[] exponentialSmoothing_5 = new double[100];
	protected double[] exponentialSmoothing_7 = new double[100];
	protected double[] exponentialSmoothing_9 = new double[100];*/
	protected int index = 0;
	protected final double ALPHA_3 = 0.5;
	/*protected final double ALPHA_5 = 0.99999;
	protected final double ALPHA_7 = 0.999999;
	protected final double ALPHA_9 = 1;*/
	
	protected double t_3 = 0;
	/*protected double t_5 = 0;
	protected double t_7 = 0;
	protected double t_9 = 0;*/
	
	private class Smoothing {
	
		private final double ALPHA = 0.5;		
		private double oldMesurmentValue;
		
		public Smoothing() {
			this.oldMesurmentValue = -1;
		}
		
		public synchronized double getExponentialSmoothing(double newMesurmentValue) {
			
			if (oldMesurmentValue != -1) {
				return oldMesurmentValue = ALPHA * newMesurmentValue + (1 - ALPHA) * oldMesurmentValue;	
			}
			
			return oldMesurmentValue = newMesurmentValue;
		}		
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
				
		beginningTime.put(r.getRequestURI(), System.currentTimeMillis());
		
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
				
		long beginning = beginningTime.get(r.getRequestURI());
		executionTime.put(r.getRequestURI(), System.currentTimeMillis() - beginning);
		this.logMessage("							[Execution time of " + r.getRequestURI() + "] : " + executionTime.get(r.getRequestURI()));
		
		long currentExecutionTime = executionTime.get(r.getRequestURI());
		
		if (index > 0) {
			exponentialSmoothing_3[index] = ALPHA_3 * currentExecutionTime + (1- ALPHA_3) * exponentialSmoothing_3[index - 1];
			/*exponentialSmoothing_5[index] = ALPHA_5 * currentExecutionTime + (1- ALPHA_5) * exponentialSmoothing_5[index - 1];
			exponentialSmoothing_7[index] = ALPHA_7 * currentExecutionTime + (1- ALPHA_7) * exponentialSmoothing_7[index - 1];
			exponentialSmoothing_9[index] = ALPHA_9 * currentExecutionTime + (1- ALPHA_7) * exponentialSmoothing_9[index - 1];*/
		} else {
			// initial value
			exponentialSmoothing_3[0] = currentExecutionTime;
			/*exponentialSmoothing_5[0] = currentExecutionTime;
			exponentialSmoothing_7[0] = currentExecutionTime;
			exponentialSmoothing_9[0] = currentExecutionTime;*/
		}				
		t_3 += (exponentialSmoothing_3[index] - currentExecutionTime) * (exponentialSmoothing_3[index] - currentExecutionTime);
		/*t_5 += (exponentialSmoothing_5[index] - currentExecutionTime) * (exponentialSmoothing_5[index] - currentExecutionTime);
		t_7 += (exponentialSmoothing_7[index] - currentExecutionTime) * (exponentialSmoothing_7[index] - currentExecutionTime);
		t_9 += (exponentialSmoothing_9[index] - currentExecutionTime) * (exponentialSmoothing_9[index] - currentExecutionTime);*/
		
		this.logMessage("							[Exponential smoothing curr] : " + currentExecutionTime);
		this.logMessage("							[Exponential smoothing nimp] : " + exponentialSmoothing_3[index]);
		/*this.logMessage("							[Exponential smoothing .998] : " + t_5);
		this.logMessage("							[Exponential smoothing .999] : " + t_7);
		this.logMessage("							[Exponential smoothing 1.00] : " + t_9);*/
		
		exponentialSmoothing = smoothing.getExponentialSmoothing(currentExecutionTime);
		this.logMessage("							[Exponential smoothing clas] : " + exponentialSmoothing);
		
		index++;
		
		this.rnop.notifyRequestTermination(r);
	}
	
	public RequestDispatcherDynamicStateI getDynamicState() throws Exception {
		
		final double averageExecutionTime = calculateAverageExecutionTime();
		
		//TODO keep or remove this
		if (averageExecutionTime == -1) {
			return null;
		}
		
		return new RequestDispatcherDynamicState(this.rdURI, averageExecutionTime);
	}
	
	public double calculateAverageExecutionTime() {
		// TODO Auto-generated method stub
		// y=filter(1-a, [1 -a],u);
		return 99;
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
