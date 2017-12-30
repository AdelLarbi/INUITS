package fr.upmc.inuits.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.DataOfferedI;
import fr.upmc.components.interfaces.DataRequiredI;

public interface RequestDispatcherDynamicStateI 
	extends DataOfferedI.DataI, DataRequiredI.DataI {

	public String getRequestDispatcherURI();
	
	public double getCurrentAverageExecutionTime();	
}
