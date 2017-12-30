package fr.upmc.inuits.software.requestdispatcher;

import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

public class RequestDispatcherDynamicState 
	implements RequestDispatcherDynamicStateI {
	
	private static final long serialVersionUID = 1L;

	/** URI of the request dispatcher to which this dynamic state relates. */
	protected final String rdURI;
	/** TODO.		*/
	protected final double averageExecutionTime;
	
	public RequestDispatcherDynamicState(String rdURI, double averageExecutionTime) throws Exception {
		
		super();
		this.rdURI = rdURI;
		this.averageExecutionTime = averageExecutionTime;
	}
	
	@Override
	public double getCurrentAverageExecutionTime() {
		
		// copy not to provide direct access to internal data structures.
		double ret = averageExecutionTime;
		
		return ret;
	}

	@Override
	public String getRequestDispatcherURI() {
		
		return new String(this.rdURI);
	}
}
