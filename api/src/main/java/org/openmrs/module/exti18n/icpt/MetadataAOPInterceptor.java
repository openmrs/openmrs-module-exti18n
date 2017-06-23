package org.openmrs.module.exti18n.icpt;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.impl.BaseLocalizer;
import org.openmrs.module.exti18n.api.impl.MetadataLocalizer;

/**
 * This intercepts the fetching of {@link BaseOpenmrsMetadata} entities in order to localize some of
 * their fields (=substitute with i18n messages values). Its purpose is to ensure that only i18n
 * messages values get returned from the database when it comes to some selected metadata.
 */
public class MetadataAOPInterceptor implements MethodInterceptor {
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		BaseLocalizer<?> l10n = Context.getRegisteredComponent(ExtI18nConstants.COMPONENT_METADATA_LOCALIZER,
		    MetadataLocalizer.class);
		
		return l10n.process(invocation.proceed());
	}
}
