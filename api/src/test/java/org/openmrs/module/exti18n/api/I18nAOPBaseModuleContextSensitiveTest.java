package org.openmrs.module.exti18n.api;

import org.aopalliance.aop.Advice;
import org.junit.After;
import org.junit.Before;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.AdvicePoint;
import org.openmrs.module.addresshierarchy.i18n.I18nBaseModuleContextSensitiveTest;

/**
 * Extend this class to run context sensitive tests with i18n enabled and with Spring AOP.
 * 
 * @see {@link I18nBaseModuleContextSensitiveTest}
 */
public abstract class I18nAOPBaseModuleContextSensitiveTest extends I18nBaseModuleContextSensitiveTest {
	
	private Advice locationServiceAdvice;
	
	private Advice patientServiceAdvice;
	
	private Advice personServiceAdvice;
	
	@Before
	public void setupAOP() throws Exception {
		// Setting up the AOP
		locationServiceAdvice = (Advice) (new AdvicePoint(LocationService.class.getCanonicalName(),
		        Context.loadClass(AddressValuesAOPInterceptor.class.getCanonicalName()))).getClassInstance();
		patientServiceAdvice = (Advice) (new AdvicePoint(PatientService.class.getCanonicalName(),
		        Context.loadClass(AddressValuesAOPInterceptor.class.getCanonicalName()))).getClassInstance();
		personServiceAdvice = (Advice) (new AdvicePoint(PersonService.class.getCanonicalName(),
		        Context.loadClass(AddressValuesAOPInterceptor.class.getCanonicalName()))).getClassInstance();
		
		Context.addAdvice(Context.loadClass(LocationService.class.getCanonicalName()), locationServiceAdvice);
		Context.addAdvice(Context.loadClass(PatientService.class.getCanonicalName()), patientServiceAdvice);
		Context.addAdvice(Context.loadClass(PersonService.class.getCanonicalName()), personServiceAdvice);
	}
	
	@After
	public void tearDownAOP() throws Exception {
		// Tearing down the AOP
		Context.removeAdvice(Context.loadClass(LocationService.class.getCanonicalName()), locationServiceAdvice);
		Context.removeAdvice(Context.loadClass(PatientService.class.getCanonicalName()), patientServiceAdvice);
		Context.removeAdvice(Context.loadClass(PersonService.class.getCanonicalName()), personServiceAdvice);
	}
}
