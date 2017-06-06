package org.openmrs.module.exti18n.icpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.api.impl.AddressHierarchyI18nCache;

/**
 * This intercepts the fetching of {@link PersonAddress} and {@link Location} entities, in order to
 * localize the addresses values (=substitute with i18n messages values). Its purpose is to ensure
 * that only i18n messages values get returned from the database when it comes to address values.
 * Note that only the fields that correspond to the address hierarchy levels are being localized.
 */
public class AddressValuesAOPInterceptor implements MethodInterceptor {
	
	protected static final Log log = LogFactory.getLog(AddressValuesAOPInterceptor.class);
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		AddressHierarchyI18nCache i18nCache = Context.getRegisteredComponent("addressHierarchyI18nCache",
		    AddressHierarchyI18nCache.class);
		final Object original = invocation.proceed();
		if (original == null || !i18nCache.isEnabled()) {
			return original;
		}
		
		Set<String> targetMethods = new HashSet<String>();
		
		targetMethods.add("savePatient");
		targetMethods.add("getPatient");
		targetMethods.add("getPatientOrPromotePerson");
		targetMethods.add("getPatientByUuid");
		targetMethods.add("voidPatient");
		targetMethods.add("unvoidPatient");
		targetMethods.add("getPatientByExample");
		targetMethods.add("getPatientOrPromotePerson");
		
		targetMethods.add("voidPerson");
		targetMethods.add("unvoidPerson");
		targetMethods.add("savePerson");
		targetMethods.add("getPersonByUuid");
		targetMethods.add("getPerson");
		
		targetMethods.add("voidPersonAddress");
		targetMethods.add("unvoidPersonAddress");
		targetMethods.add("savePersonAddress");
		targetMethods.add("getPersonAddressByUuid");
		
		targetMethods.add("saveLocation");
		targetMethods.add("getLocation");
		targetMethods.add("getDefaultLocation");
		targetMethods.add("getLocationByUuid");
		targetMethods.add("retireLocation");
		targetMethods.add("unretireLocation");
		
		if (invocation.getThis() instanceof PersonService) {
			boolean methodTargeted = targetMethods.contains(invocation.getMethod().getName());
			if (methodTargeted && Person.class.isAssignableFrom(original.getClass())) {
				return l10nPerson((Person) original, i18nCache.getOrderedAddressFields());
			}
			if (methodTargeted && PersonAddress.class.isAssignableFrom(original.getClass())) {
				return l10nAddressValues((PersonAddress) original, i18nCache.getOrderedAddressFields());
			}
		}
		if (invocation.getThis() instanceof PatientService && Patient.class.isAssignableFrom(original.getClass())) {
			if (targetMethods.contains(invocation.getMethod().getName())) {
				return l10nPatient((Patient) original, i18nCache.getOrderedAddressFields());
			}
		}
		if (invocation.getThis() instanceof LocationService && Location.class.isAssignableFrom(original.getClass())) {
			if (targetMethods.contains(invocation.getMethod().getName())) {
				return l10nAddressValues((Location) original, i18nCache.getOrderedAddressFields());
			}
		}
		
		return original;
	}
	
	/**
	 * Localizes the addresses of a patient.
	 * 
	 * @param patient
	 * @param targetFields The target address fields that should be localized.
	 * @return The instance of the patient with the localized addresses.
	 */
	public static Patient l10nPatient(Patient patient, List<String> targetFields) {
		return (Patient) l10nPerson(patient, targetFields);
	}
	
	/**
	 * Localizes the addresses of a person.
	 * 
	 * @param person
	 * @param targetFields The target address fields that should be localized.
	 * @return The instance of the person with the localized addresses.
	 */
	public static Person l10nPerson(Person person, List<String> targetFields) {
		List<PersonAddress> l10nAddresses = new ArrayList<PersonAddress>();
		if (CollectionUtils.isEmpty(person.getAddresses())) {
			return person;
		}
		for (Iterator<PersonAddress> it = person.getAddresses().iterator(); it.hasNext();) { // http://stackoverflow.com/a/1110425/321797
			l10nAddresses.add((PersonAddress) l10nAddressValues(it.next(), targetFields));
			it.remove();
		}
		for (PersonAddress l10Address : l10nAddresses) {
			person.addAddress(l10Address);
		}
		
		return person;
	}
	
	/**
	 * Localize the address fields of an OpenMRS base object.
	 * 
	 * @param data An instance of {@link BaseOpenmrsObject}.
	 * @param targetFields The target address fields that should be localized.
	 * @return The instance of the base OpenMRS object with the localized address fields.
	 */
	public static BaseOpenmrsObject l10nAddressValues(BaseOpenmrsObject data, List<String> targetFields) {
		
		for (String fieldName : targetFields) {
			String fieldValue = "";
			try {
				fieldValue = BeanUtils.getProperty(data, fieldName);
				if (!StringUtils.isEmpty(fieldValue)) {
					BeanUtils.setProperty(data, fieldName, Context.getMessageSourceService().getMessage(fieldValue));
				}
			}
			catch (Exception e) {
				log.error("'" + fieldName + "' could not be localised in " + BaseOpenmrsData.class.toString() + " bean: "
				        + data.toString(), e);
				break;
			}
		}
		
		return data;
	}
}
