package org.openmrs.module.exti18n.api;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Location;
import org.openmrs.PersonAddress;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This intercepts the saving and updating of {@link PersonAddress} and {@link Location} entities,
 * in order to internationalize the addresses values (=substitute with i18n messages keys). Its
 * purpose is to ensure that only i18n messages keys get saved into the database when it comes to
 * address values. Note that only the fields that correspond to the address hierarchy levels are
 * being internationalized.
 */
public class AddressValuesHibernateInterceptor extends EmptyInterceptor {
	
	private static final long serialVersionUID = 442693627741326089L;
	
	@Autowired
	private AddressHierarchyI18nCache i18nCache;
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		
		if (i18nCache.isEnabled() && (entity instanceof PersonAddress || entity instanceof Location)) {
			i18nAddressValues(propertyNames, state);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		
		if (i18nCache.isEnabled() && (entity instanceof PersonAddress || entity instanceof Location)) {
			i18nAddressValues(propertyNames, currentState);
			return true;
		}
		
		return false;
	}
	
	/*
	 * Only for the address fields of the AH
	 */
	protected void i18nAddressValues(String[] fields, Object[] values) {
		final Set<String> i18nFields = new HashSet<String>(i18nCache.getOrderedAddressFields());
		for (int i = 0; i < fields.length; i++) {
			if (!i18nFields.contains(fields[i])) {
				continue;
			}
			if (values[i] != null && values[i] instanceof String) {
				values[i] = i18nCache.getMessageKey((String) values[i], true);
			}
		}
	}
}
