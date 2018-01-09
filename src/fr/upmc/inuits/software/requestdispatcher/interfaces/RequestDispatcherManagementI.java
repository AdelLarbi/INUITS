package fr.upmc.inuits.software.requestdispatcher.interfaces;

public interface RequestDispatcherManagementI {
	
	public void createRequestSubmissionAndNotificationPorts(String requestSubmissionOutboundPortUri, 
			String requestNotificationIntboundPortUri) throws Exception;
}
