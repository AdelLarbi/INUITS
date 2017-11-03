package fr.upmc.inuits.utils;

import java.lang.reflect.Method;
import java.util.HashMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Utils {
	
	/**
	 * 
	 * @param connectorCanonicalClassName
	 * @param connectorSuperclass
	 * @param connectorImplementedInterface
	 * @param offeredInterface
	 * @param methodNamesMap
	 * @return
	 * @throws Exception
	 */
	public static Class<?> makeConnectorClassJavassist(
			String connectorCanonicalClassName,
			Class<?> connectorSuperclass,
			Class<?> connectorImplementedInterface,
			Class<?> offeredInterface,
			HashMap<String,String> methodNamesMap) throws Exception {
		
		ClassPool pool = ClassPool.getDefault();
		CtClass cs = pool.get(connectorSuperclass.getCanonicalName());
		CtClass cii = pool.get(connectorImplementedInterface.getCanonicalName());		
		CtClass connectorCtClass = pool.makeClass(connectorCanonicalClassName);
		
		System.out.println("cs = " + cs.getName());
		System.out.println("cii = " + cii.getName());		
		
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
						source += "," ;
					}
				}
			}
			source += " {\n\n	return ((" ;
			source += offeredInterface.getCanonicalName() + ")this.offering).";
			source += methodNamesMap.get(methodsToImplement[i].getName());
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
