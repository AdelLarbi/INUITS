package fr.upmc.inuits.utils;

import java.lang.reflect.Method;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import fr.upmc.inuits.software.autonomiccontroller.interfaces.AutonomicControllerManagementI;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/** 
 * Classe qui permet de générer dynamiquement une classe de connexion entre ports avec Javassit  
 */
public abstract class Javassist {
	
	protected final static Class<?> CONNECTOR_SUPERCLASS = AbstractConnector.class;
	protected final static String PACKAGE_NAME = "fr.upmc.inuits.utils.";	
	
	/** Les classes qu'on peut générer */
	private static Class<?> applicationManagementConnector;	
	private static Class<?> applicationNotificationConnector;
	private static Class<?> applicationServicesConnector;
	private static Class<?> applicationSubmissionConnector;
	private static Class<?> requestNotificationConnector;
	private static Class<?> requestSubmissionConnector;
	private static Class<?> autonomicControllerManagementConnector;


	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : ApplicationManagementConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getApplicationManagementConnectorClassName() throws Exception {
		
		if (applicationManagementConnector == null) {			
			applicationManagementConnector = makeConnectorClass("ApplicationManagementConnector", ApplicationManagementI.class);
		}
		
		return applicationManagementConnector.getCanonicalName();		
	}
	
	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : ApplicationNotificationConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getApplicationNotificationConnectorClassName() throws Exception {
		
		if (applicationNotificationConnector == null) {			
			applicationNotificationConnector = makeConnectorClass("ApplicationNotificationConnector", ApplicationNotificationI.class);
		}
		
		return applicationNotificationConnector.getCanonicalName();		
	}
	
	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : ApplicationServicesConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getApplicationServicesConnectorClassName() throws Exception {
		
		if (applicationServicesConnector == null) {			
			applicationServicesConnector = makeConnectorClass("ApplicationServicesConnector", ApplicationServicesI.class);
		}
		
		return applicationServicesConnector.getCanonicalName();		
	}
	
	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : ApplicationSubmissionConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getApplicationSubmissionConnectorClassName() throws Exception {
		
		if (applicationSubmissionConnector == null) {			
			applicationSubmissionConnector = makeConnectorClass("ApplicationSubmissionConnector", ApplicationSubmissionI.class);
		}
		
		return applicationSubmissionConnector.getCanonicalName();
	}

	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : RequestNotificationConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getRequestNotificationConnectorClassName() throws Exception {
		
		if (requestNotificationConnector == null) {			
			requestNotificationConnector = makeConnectorClass("RequestNotificationConnector", RequestNotificationI.class);
		}
		
		return requestNotificationConnector.getCanonicalName();			
	}

	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : requestSubmissionConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getRequestSubmissionConnectorClassName() throws Exception {
	
		if (requestSubmissionConnector == null) {			
			requestSubmissionConnector = makeConnectorClass("requestSubmissionConnector", RequestSubmissionI.class);
		}
		
		return requestSubmissionConnector.getCanonicalName();					
	}	
	
	/**
	 * Getters : Créer une nouvelle classe si c'est la premiére demande, sinon renvoie une classe existante
	 * @return Le nom de la nouvelle classe : AutonomicControllerManagementConnector
	 * @throws Exception si le nom existe déja
	 */
	public synchronized static String getAutonomicControllerManagementConnectorClassName() throws Exception {
		
		if (autonomicControllerManagementConnector == null) {			
			autonomicControllerManagementConnector = makeConnectorClass("AutonomicControllerManagementConnector", AutonomicControllerManagementI.class);
		}
		
		return autonomicControllerManagementConnector.getCanonicalName();		
	}

	/**
	 * Générer dynamiquement une classe de connexion entre ports avec Javassit
	 * @param className
	 * @param connectorImplementedInterface
	 * @return the created class
	 * @throws Exception
	 */
	private static Class<?> makeConnectorClass(
			String className,
			Class<?> connectorImplementedInterface) throws Exception {
		
		ClassPool pool = ClassPool.getDefault();
		CtClass cs = pool.get(CONNECTOR_SUPERCLASS.getCanonicalName());
		CtClass cii = pool.get(connectorImplementedInterface.getCanonicalName());		
		CtClass connectorCtClass = pool.makeClass(PACKAGE_NAME + className);
				
		connectorCtClass.setSuperclass(cs);
		
		Method[] methodsToImplement = connectorImplementedInterface.getDeclaredMethods();			
		
		for (int i = 0; i < methodsToImplement.length; i++) {
			
			String source = "public ";			
			source += methodsToImplement[i].getReturnType().getTypeName() + " ";			
			source += methodsToImplement[i].getName() + "(";					
			
			Class<?>[] pt = methodsToImplement[i].getParameterTypes();
			String callParam = "";
			
			for (int j = 0; j < pt.length; j++) {
				String pName = "arg" + j;
				source += pt[j].getCanonicalName() + " " + pName;
				callParam += pName;
				if (j < pt.length - 1) {
					source += ", ";
					callParam += ", ";
				}
			}
			source += ")";
			Class<?>[] et = methodsToImplement[i].getExceptionTypes();
			if (et != null && et.length > 0) {
				source += " throws ";

				for (int z = 0; z < et.length; z++) {
					source += et[z].getCanonicalName();
					if (z < et.length - 1) {
						source += ",";
					}
				}
			}
			source += " {\n\n	return ((";
			source += connectorImplementedInterface.getCanonicalName() + ")this.offering).";			
			source += methodsToImplement[i].getName();
			source += "(" + callParam + ");\n}";
			//System.out.println(source);
			CtMethod theCtMethod = CtMethod.make(source, connectorCtClass);
			connectorCtClass.addMethod(theCtMethod);
		}
		
		connectorCtClass.setInterfaces(new CtClass[]{cii});
		cii.detach(); 
		cs.detach();		
		Class<?> ret = connectorCtClass.toClass();
		connectorCtClass.detach();
					
		return ret;
	}
}
