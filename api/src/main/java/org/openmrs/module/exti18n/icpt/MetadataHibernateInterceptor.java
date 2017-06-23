package org.openmrs.module.exti18n.icpt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.VisitType;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.ReverseI18nCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Ensures that metadata objects are internationalized before being saved again.
 * 
 * @see MetadataAOPInterceptor
 */
public class MetadataHibernateInterceptor extends EmptyInterceptor {
	
	private static final long serialVersionUID = 8121430714632367748L;
	
	@Autowired
	@Qualifier(ExtI18nConstants.COMPONENT_REVI18N)
	private ReverseI18nCache i18nCache;
	
	private final Set<String> targetFields = new HashSet<String>(Arrays.asList("name", "description", "formatDescription"));
	
	protected boolean isCoveredEntity(Object entity) {
		return entity instanceof Location || entity instanceof VisitType || entity instanceof PersonAttributeType
		        || entity instanceof PatientIdentifierType;
	}
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		
		if (i18nCache.isEnabled() && isCoveredEntity(entity)) {
			i18nMetadata(propertyNames, state);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
	        String[] propertyNames, Type[] types) {
		
		if (i18nCache.isEnabled() && isCoveredEntity(entity)) {
			i18nMetadata(propertyNames, currentState);
			return true;
		}
		
		return false;
	}
	
	protected void i18nMetadata(String[] fields, Object[] values) {
		for (int i = 0; i < fields.length; i++) {
			if (!targetFields.contains(fields[i])) {
				continue;
			}
			if (values[i] != null && values[i] instanceof String) {
				values[i] = i18nCache.getMessageKey((String) values[i]);
			}
		}
	}
}
