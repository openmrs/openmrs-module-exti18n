package org.openmrs.module.exti18n.api.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AddressValuesLocalizer extends BaseLocalizer<AddressHierarchyI18nCache> {
	
	@Autowired
	@Qualifier(ExtI18nConstants.COMPONENT_AH_REVI18N)
	private AddressHierarchyI18nCache cache;
	
	@Override
	protected AddressHierarchyI18nCache getCache() {
		return cache;
	}
	
	@Override
	protected Object process(Object original, AddressHierarchyI18nCache cache) throws Exception {
		Class<?> clazz = original.getClass();
		if (Location.class.isAssignableFrom(clazz) || PersonAddress.class.isAssignableFrom(clazz)) {
			return l10nAddressValues((BaseOpenmrsObject) original, cache);
		}
		if (Person.class.isAssignableFrom(clazz)) {
			return l10nPersonAddresses((Person) original, cache);
		}
		return original;
	}
	
	/**
	 * Localizes the address fields of an OpenMRS base object with address fields.
	 * 
	 * @param data A {@link Location} or {@link PersonAddress} instance.
	 * @param cache The reverse i18n cache for AH.
	 * @return The localized object.
	 */
	public static Object l10nAddressValues(BaseOpenmrsObject data, AddressHierarchyI18nCache cache)
	        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		for (String fieldName : cache.getOrderedAddressFields()) {
			String fieldValue = BeanUtils.getProperty(data, fieldName);
			if (!StringUtils.isEmpty(fieldValue)) {
				BeanUtils.setProperty(data, fieldName, Context.getMessageSourceService().getMessage(fieldValue));
			}
		}
		return data;
	}
	
	/**
	 * Localizes the addresses of a {@link Person}.
	 * 
	 * @param person
	 * @param cache The reverse i18n cache for AH.
	 * @return The {@link Person} with localized addresses.
	 */
	public static Object l10nPersonAddresses(Person person, AddressHierarchyI18nCache cache) throws IllegalAccessException,
	        InvocationTargetException, NoSuchMethodException {
		List<PersonAddress> addresses = new ArrayList<PersonAddress>();
		
		if (CollectionUtils.isEmpty(person.getAddresses())) {
			return person;
		}
		for (Iterator<PersonAddress> it = person.getAddresses().iterator(); it.hasNext();) { // http://stackoverflow.com/a/1110425/321797
			addresses.add((PersonAddress) l10nAddressValues(it.next(), cache));
			it.remove();
		}
		for (PersonAddress address : addresses) {
			person.addAddress(address);
		}
		
		return person;
	}
}
