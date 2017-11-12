package fr.upmc.inuits.utils;

import java.lang.reflect.Method;

import fr.upmc.components.connectors.AbstractConnector;
import fr.upmc.datacenter.software.interfaces.RequestNotificationI;
import fr.upmc.datacenter.software.interfaces.RequestSubmissionI;
import fr.upmc.inuits.software.application.interfaces.ApplicationManagementI;
import fr.upmc.inuits.software.application.interfaces.ApplicationNotificationI;
import fr.upmc.inuits.software.application.interfaces.ApplicationServicesI;
import fr.upmc.inuits.software.application.interfaces.ApplicationSubmissionI;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public abstract class Javassist {
	
	protected final static Class<?> CONNECTOR_SUPERCLASS = AbstractConnector.class;
	protected final static String PACKAGE_NAME = "fr.upmc.inuits.utils.";	
	
	private static Class<?> applicationManagementConnector;	
	private static Class<?> applicationNotificationConnector;
	private static Class<?> applicationServicesConnector;
	private static Class<?> applicationSubmissionConnector;
	private static Class<?> requestNotificationConnector;
	private static Class<?> requestSubmissionConnector;	

	public static String getApplicationManagementConnectorClassName() throws Exception {
		
		return getConnectorClassName(
				applicationManagementConnector,
				"ApplicationManagementConnector",
				ApplicationManagementI.class);
	}
	
	public static String getApplicationNotificationConnectorClassName() throws Exception {
		
		return getConnectorClassName(
				applicationNotificationConnector,
				"ApplicationNotificationConnector",
				ApplicationNotificationI.class);	
	}
	
	public static String getApplicationServicesConnectorClassName() throws Exception {
		
		return getConnectorClassName(
				applicationServicesConnector,
				"ApplicationServicesConnector",
				ApplicationServicesI.class);
	}
	
	public static String getApplicationSubmissionConnectorClassName() throws Exception {
		
		return getConnectorClassName(
				applicationSubmissionConnector,
				"ApplicationSubmissionConnector",
				ApplicationSubmissionI.class);
	}

	public static String getRequestNotificationConnectorClassName() throws Exception {
		
		return getConnectorClassName(
				requestNotificationConnector,
				"RequestNotificationConnector",
				RequestNotificationI.class);		
	}

	public static String getRequestSubmissionConnectorClassName() throws Exception {
	
		return getConnectorClassName(
				requestSubmissionConnector,
				"RequestSubmissionConnector",
				RequestSubmissionI.class);
	}	
	
	private static String getConnectorClassName(
			Class<?> connectorClass, 
			String connectorClassName, 
			Class<?> connectorImplementedInterface) throws Exception {
		
		if (connectorClass == null) {
			connectorClass = makeConnectorClass(connectorClassName, connectorImplementedInterface);
		}
		
		return connectorClass.getCanonicalName();
	}
	
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
			System.out.println(source);
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