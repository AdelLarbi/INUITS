package fr.upmc.inuits.software.requestdispatcher;

import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

public class RequestDispatcherDynamicState 
	implements RequestDispatcherDynamicStateI {
	
	private static final long serialVersionUID = 1L;

	/** URI of the request dispatcher to which this dynamic state relates. */
	protected final String rdURI;
	/** TODO.		*/
	protected final double exponentialSmoothing;	
	/** TODO.		*/
	protected final double averageExecutionTime;
	/** TODO.		*/
	protected final int availableAVMsCount;
	
	public RequestDispatcherDynamicState(String rdURI, double exponentialSmoothing, double averageExecutionTime, 
			int availableAVMsCount) throws Exception {
		
		super();
		this.rdURI = rdURI;
		this.exponentialSmoothing = exponentialSmoothing;
		this.averageExecutionTime = averageExecutionTime;
		this.availableAVMsCount = availableAVMsCount;
	}
	
	@Override
	public String getRequestDispatcherURI() {
		
		return new String(this.rdURI);
	}
	
	@Override
	public double getCurrentExponentialSmoothing() {
		
		// copy not to provide direct access to internal data structures.
		double ret = exponentialSmoothing;
		
		return ret;
	}
	
	@Override
	public double getCurrentAverageExecutionTime() {
		
		// copy not to provide direct access to internal data structures.
		double ret = averageExecutionTime;
		
		return ret;
	}	

	@Override
	public int getAvailableAVMsCount() {

		// copy not to provide direct access to internal data structures.
		int ret = availableAVMsCount;
			
		return ret;
	}
}
