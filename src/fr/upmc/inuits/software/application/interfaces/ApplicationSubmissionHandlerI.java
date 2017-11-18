package fr.upmc.inuits.software.application.interfaces;

public interface ApplicationSubmissionHandlerI {
	
	public void	acceptApplicationSubmissionAndNotify(String appUri, int appIndex, int mustHaveCores) throws Exception;
}
