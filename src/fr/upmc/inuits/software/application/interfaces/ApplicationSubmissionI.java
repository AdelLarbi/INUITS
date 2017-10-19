package fr.upmc.inuits.software.application.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface ApplicationSubmissionI 
extends OfferedI, RequiredI {

	public void submitApplication() throws Exception;
	public void submitApplicationAndNotify() throws Exception;
}
