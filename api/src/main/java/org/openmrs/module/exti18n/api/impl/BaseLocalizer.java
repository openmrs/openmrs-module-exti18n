package org.openmrs.module.exti18n.api.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;
import org.openmrs.module.exti18n.api.ReverseI18nCache;

abstract public class BaseLocalizer<C extends ReverseI18nCache> {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	abstract protected C getCache();
	
	/*
	 * This is the main method to be implemented by children classes.
	 * It localizes the original object based on the i18n data that is cached.
	 */
	abstract protected Object process(Object original, C cache) throws Exception;
	
	/*
	 * Override this to opt in or out of the localizer based on the original returned object.
	 */
	protected boolean filter(Object original) {
		return true;
	}
	
	private Object l10n(Object original, C cache) throws Exception {
		if (filter(original)) {
			return process(original, cache);
		} else {
			return original;
		}
	}
	
	/**
	 * Localizes an instance or a collection of objects.
	 * 
	 * @param original A instance or a collection of instances to be localised.
	 * @return
	 */
	public Object process(Object original) {
		
		C cache = getCache();
		if (original == null || cache == null || !cache.isEnabled()) {
			return original;
		}
		
		boolean isCollection = false;
		if (original instanceof Collection<?>) {
			isCollection = true;
		}
		
		Object result = original;
		if (!isCollection) {
			try {
				result = l10n(original, cache);
			}
			catch (Exception e) {
				log.warn("There was an error while localizing '" + original.toString()
				        + "', the original unchanged object was returned.", e);
				return original;
			}
			
		} else {
			List<Object> results = new ArrayList<Object>();
			try {
				@SuppressWarnings("unchecked")
				Collection<Object> objects = (Collection<Object>) original;
				for (Object obj : objects) {
					results.add(l10n(obj, cache));
				}
			}
			catch (Exception e) {
				log.warn("There was an error while localizing '" + original.toString()
				        + "', the original unchanged object was returned.", e);
				return original;
			}
			result = results;
		}
		
		return result;
	}
	
	/**
	 * Localizes only the description member of an {@link BaseOpenmrsMetadata} based on a reverse
	 * i18n cache.
	 * 
	 * @return The metadata instance with a localized description.
	 */
	public static BaseOpenmrsMetadata l10nDescription(BaseOpenmrsMetadata metadata, ReverseI18nCache cache) {
		metadata.setDescription(cache.getMessage(metadata.getDescription()));
		return metadata;
	}
	
	/**
	 * Localizes a {@link BaseOpenmrsMetadata} based on a reverse i18n cache.
	 * 
	 * @param metadata
	 * @return The localized metadata instance.
	 */
	public static Object l10nMetadata(BaseOpenmrsMetadata metadata, ReverseI18nCache cache) {
		metadata = l10nDescription(metadata, cache);
		metadata.setName(cache.getMessage(metadata.getName()));
		return metadata;
	}
	
	/**
	 * Localizes a {@link PatientIdentifierType} based on a reverse i18n cache.
	 * 
	 * @return The localized patient identifier type.
	 */
	public static Object l10nPatientIdentifierType(PatientIdentifierType pit, ReverseI18nCache cache) {
		pit.setFormatDescription(cache.getMessage(pit.getFormatDescription()));
		return l10nMetadata((BaseOpenmrsMetadata) pit, cache);
	}
	
	/**
	 * Localizes the addresses of a person.
	 * 
	 * @param person
	 * @return The instance of the person with the localized addresses.
	 */
	public static Object l10nPersonAddresses(Person person, AddressHierarchyI18nCache cache) throws IllegalAccessException,
	        InvocationTargetException, NoSuchMethodException {
		
		List<PersonAddress> l10nAddresses = new ArrayList<PersonAddress>();
		if (CollectionUtils.isEmpty(person.getAddresses())) {
			return person;
		}
		for (Iterator<PersonAddress> it = person.getAddresses().iterator(); it.hasNext();) { // http://stackoverflow.com/a/1110425/321797
			l10nAddresses.add((PersonAddress) l10nAddressValues(it.next(), cache));
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
	 * @return The instance of the base OpenMRS object with the localized address fields.
	 */
	public static Object l10nAddressValues(BaseOpenmrsObject data, AddressHierarchyI18nCache cache)
	        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		for (String fieldName : cache.getOrderedAddressFields()) {
			String fieldValue = BeanUtils.getProperty(data, fieldName);
			if (!StringUtils.isEmpty(fieldValue)) {
				BeanUtils.setProperty(data, fieldName, cache.getMessage(fieldValue));
			}
		}
		return data;
	}
}
