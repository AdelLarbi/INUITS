package fr.upmc.inuits.software.requestdispatcher;

import fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI;

/**
 * classe representant les données dynamiques liées au dispatcher
 * @author evergreen
 *
 */
public class RequestDispatcherDynamicState 
	implements RequestDispatcherDynamicStateI {
	
	private static final long serialVersionUID = 1L;

	/** URI of the request dispatcher to which this dynamic state relates. TODO */
	protected final String rdURI;

	protected final double exponentialSmoothing;	

	protected final double averageExecutionTime;

	protected final int availableAVMsCount;	

	protected final int totalRequestSubmitted;

	protected final int totalRequestTerminated;
	
	/**
	 *  Contructeur permet de cree un objet avec tous les element dynamiques liee au dispatcher
	 * @param rdURI Uri du dispatcher
	 * @param exponentialSmoothing Moyenne du temps de reponse des requetes avec la technique du lissage exponentielle
	 * @param averageExecutionTime Moyenne du temps de reponse des requetes avec la technique de la moyenne mouvante.
	 * @param availableAVMsCount nombre d'AVM disponible
	 * @param totalRequestSubmitted Nombre total de requete soumise
	 * @param totalRequestTerminated Nombre total de requete terminées.
	 * @throws Exception
	 */
	public RequestDispatcherDynamicState(String rdURI, double exponentialSmoothing, double averageExecutionTime, 
			int availableAVMsCount, int totalRequestSubmitted, int totalRequestTerminated) throws Exception {
		
		super();
		this.rdURI = rdURI;
		this.exponentialSmoothing = exponentialSmoothing;
		this.averageExecutionTime = averageExecutionTime;
		this.availableAVMsCount = availableAVMsCount;
		this.totalRequestSubmitted = totalRequestSubmitted;
		this.totalRequestTerminated = totalRequestTerminated;
	}
	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getRequestDispatcherURI()
	 */
	@Override
	public String getRequestDispatcherURI() {
		
		return new String(this.rdURI);
	}
	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getCurrentExponentialSmoothing()
	 */
	@Override
	public double getCurrentExponentialSmoothing() {
		
		// copy not to provide direct access to internal data structures.
		double ret = exponentialSmoothing;
		
		return ret;
	}
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getCurrentAverageExecutionTime()
	 */
	@Override
	public double getCurrentAverageExecutionTime() {
		
		// copy not to provide direct access to internal data structures. TODO
		double ret = averageExecutionTime;
		
		return ret;
	}	
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getAvailableAVMsCount()
	 */
	@Override
	public int getAvailableAVMsCount() {

		// copy not to provide direct access to internal data structures. TODO
		int ret = availableAVMsCount;
			
		return ret;
	}
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getTotalRequestSubmittedCount()
	 */
	@Override
	public int getTotalRequestSubmittedCount() {
		
		// copy not to provide direct access to internal data structures. TODO
		int ret = totalRequestSubmitted;
			
		return ret;
	}
	/**
	 * @see fr.upmc.inuits.software.requestdispatcher.interfaces.RequestDispatcherDynamicStateI#getTotalRequestTerminatedCount()
	 */
	@Override
	public int getTotalRequestTerminatedCount() {
		
		// copy not to provide direct access to internal data structures. TODO
		int ret = totalRequestTerminated;
			
		return ret;
	}
}
