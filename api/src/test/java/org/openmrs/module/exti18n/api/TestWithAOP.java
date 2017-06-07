package org.openmrs.module.exti18n.api;

import org.aopalliance.intercept.MethodInterceptor;
import org.openmrs.api.OpenmrsService;

public interface TestWithAOP {
	
	/**
	 * Sets an AOP method interceptor class to be active on the test case.
	 * 
	 * @param interceptorClass Eg. "MyInterceptor.class"
	 */
	void setInterceptor(Class<? extends MethodInterceptor> interceptorClass);
	
	/**
	 * Hooks a
	 * 
	 * @param serviceClass
	 */
	void addService(Class<? extends OpenmrsService> serviceClass);
}
