package fr.upmc.inuits.software.requestdispatcher.interfaces;

public interface RequestDispatcherStateDataConsumerI {

	public void	acceptRequestDispatcherDynamicData(String rdURI, RequestDispatcherDynamicStateI	currentDynamicState) 
			throws Exception;
}
