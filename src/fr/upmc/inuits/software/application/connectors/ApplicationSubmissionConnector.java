package fr.upmc.inuits.software.application.connectors;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;

public class ApplicationSubmissionConnector
	extends AbstractConnector
	implements ApplicationSubmissionI {

	@Override
	public void submitApplicationAndNotify() throws Exception {
		
		((ApplicationSubmissionI)this.offering).submitApplicationAndNotify();		
	}
}
