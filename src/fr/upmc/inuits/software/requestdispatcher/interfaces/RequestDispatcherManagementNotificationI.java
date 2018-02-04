package fr.upmc.inuits.software.requestdispatcher.interfaces;

import fr.upmc.components.interfaces.OfferedI;
import fr.upmc.components.interfaces.RequiredI;

public interface RequestDispatcherManagementNotificationI
	extends OfferedI, RequiredI {
	

	public void notifyCreateRequestSubmissionAndNotificationPorts(String appUri, String rdUri) throws Exception;
}
