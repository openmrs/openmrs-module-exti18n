package org.openmrs.module.exti18n.api.impl;

import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.VisitType;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.ReverseI18nCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MetadataLocalizer extends BaseLocalizer<ReverseI18nCache> {
	
	@Override
	protected boolean filter(Object original) {
		Class<?> clazz = original.getClass();
		return Location.class.isAssignableFrom(clazz) || VisitType.class.isAssignableFrom(clazz)
		        || PersonAttributeType.class.isAssignableFrom(clazz) || PatientIdentifierType.class.isAssignableFrom(clazz);
	}
	
	@Autowired
	@Qualifier(ExtI18nConstants.COMPONENT_REVI18N)
	private ReverseI18nCache cache;
	
	@Override
	protected ReverseI18nCache getCache() {
		return cache;
	}
	
	@Override
	protected Object process(Object original, ReverseI18nCache cache) throws Exception {
		if (PatientIdentifierType.class.isAssignableFrom(original.getClass())) {
			original = l10nPatientIdentifierType((PatientIdentifierType) original, cache);
		} else if (PersonAttributeType.class.isAssignableFrom(original.getClass())) {
			original = l10nDescription((BaseOpenmrsMetadata) original, cache);
		} else {
			original = l10nMetadata((BaseOpenmrsMetadata) original, cache);
		}
		return original;
	}
}
