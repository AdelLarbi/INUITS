package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerServicesI
	extends OfferedI, RequiredI {
	
	public void changeFrequency() throws Exception;
	
	public void addCores() throws Exception;
	
	public void removeCores() throws Exception;
	
	public void addAVM() throws Exception;
	
	public void removeAVM() throws Exception;	
}
