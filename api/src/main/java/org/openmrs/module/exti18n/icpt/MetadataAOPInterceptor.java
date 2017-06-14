package org.openmrs.module.exti18n.icpt;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.ReverseI18nCache;

public class MetadataAOPInterceptor implements MethodInterceptor {
	
	protected static final Log log = LogFactory.getLog(MetadataAOPInterceptor.class);
	
	protected boolean isCoveredClass(Class<?> clazz) {
		return Location.class.isAssignableFrom(clazz) || VisitType.class.isAssignableFrom(clazz)
		        || PersonAttributeType.class.isAssignableFrom(clazz) || PatientIdentifierType.class.isAssignableFrom(clazz);
	}
	
	protected boolean isTargetedService(Object service) {
		return service instanceof LocationService || service instanceof VisitService || service instanceof PersonService
		        || service instanceof PatientService;
	}
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		ReverseI18nCache i18nCache = Context.getRegisteredComponent(ExtI18nConstants.COMPONENT_REVI18N,
		    ReverseI18nCache.class);
		final Object original = invocation.proceed();
		if (original == null || !i18nCache.isEnabled()) {
			return original;
		}
		
		Set<String> targetMethods = new HashSet<String>();
		
		targetMethods.add("saveLocation");
		targetMethods.add("getLocation");
		targetMethods.add("getDefaultLocation");
		targetMethods.add("getLocationByUuid");
		targetMethods.add("retireLocation");
		targetMethods.add("unretireLocation");
		
		targetMethods.add("getVisitType");
		targetMethods.add("getVisitTypeByUuid");
		targetMethods.add("saveVisitType");
		targetMethods.add("retireVisitType");
		targetMethods.add("unretireVisitType");
		
		targetMethods.add("savePersonAttributeType");
		targetMethods.add("retirePersonAttributeType");
		targetMethods.add("getPersonAttributeType");
		targetMethods.add("getPersonAttributeTypeByUuid");
		targetMethods.add("getPersonAttributeTypeByName");
		
		targetMethods.add("savePatientIdentifierType");
		targetMethods.add("getPatientIdentifierType");
		targetMethods.add("getPatientIdentifierTypeByUuid");
		targetMethods.add("getPatientIdentifierTypeByName");
		targetMethods.add("retirePatientIdentifierType");
		targetMethods.add("unretirePatientIdentifierType");
		
		Object result = original;
		if (isTargetedService(invocation.getThis())) {
			boolean methodTargeted = targetMethods.contains(invocation.getMethod().getName());
			if (methodTargeted && isCoveredClass(original.getClass())) {
				result = l10nMetadata((BaseOpenmrsMetadata) result, i18nCache);
			}
			if (methodTargeted && PatientIdentifierType.class.isAssignableFrom(original.getClass())) {
				result = l10nPatientIdentifierType((PatientIdentifierType) result, i18nCache);
			}
		}
		
		return result;
	}
	
	/**
	 * Localizes a {@link BaseOpenmrsMetadata}
	 * 
	 * @param metadata
	 * @return The localized metadata instance.
	 */
	public static BaseOpenmrsMetadata l10nMetadata(BaseOpenmrsMetadata metadata, ReverseI18nCache i18nCache) {
		
		metadata.setName(i18nCache.getMessage(metadata.getName()));
		metadata.setDescription(i18nCache.getMessage(metadata.getDescription()));
		
		return metadata;
	}
	
	public static PatientIdentifierType l10nPatientIdentifierType(PatientIdentifierType type, ReverseI18nCache i18nCache) {
		
		type.setFormatDescription(i18nCache.getMessage(type.getFormatDescription()));
		
		return type;
	}
}
