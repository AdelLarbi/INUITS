package fr.upmc.inuits.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;

/**
 * L' interface <code>RequestDispatcherDynamicStateI</code> permet la gestion des données dynamique associées au dispatcher. 
 *
 */
public interface RequestDispatcherDynamicStateI 
	extends DataOfferedI.DataI, DataRequiredI.DataI {

	/**
	 * Getter qui permet de recuperer l'URI du dispatcher.
	 */
	public String getRequestDispatcherURI();
	
	/**
	 * Getter qui permet de recuperer la moyenne du temps de reponse des requetes avec la technique du lissage exponentielle.
	 */
	public double getCurrentExponentialSmoothing();
	
	/**
	 * Getter qui permet de recuperer la moyenne du temps de reponse des requetes avec la technique de la moyenne mouvante.
	 */
	public double getCurrentAverageExecutionTime();
	
	/**
	 * Getter qui permet de recuperer le nombre total d'AVM disponibles.
	 */
	public int getAvailableAVMsCount();
	
	/**
	 * Getter qui permet de recuperer le nombre total de rêquetes soumises
	 */
	public int getTotalRequestSubmittedCount();
	
	/**
	 * Getter qui permet de recuperer le nombre total de rêquetes terminées.
	 */
	public int getTotalRequestTerminatedCount();
}
