package fr.upmc.inuits.software.autonomiccontroller.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface AutonomicControllerServicesI
	extends OfferedI, RequiredI {
	
	public boolean increaseFrequency() throws Exception;
	
	public boolean decreaseFrequency() throws Exception;
	
	public boolean addCores() throws Exception;
	
	public boolean removeCores() throws Exception;
	
	public boolean addAVMs() throws Exception;
	
	public boolean removeAVMs() throws Exception;	
}
