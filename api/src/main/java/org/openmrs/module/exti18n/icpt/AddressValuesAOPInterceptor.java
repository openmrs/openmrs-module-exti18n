package org.openmrs.module.exti18n.icpt;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Location;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.impl.AddressValuesLocalizer;
import org.openmrs.module.exti18n.api.impl.BaseLocalizer;

/**
 * This intercepts the fetching of {@link PersonAddress} and {@link Location} entities, in order to
 * localize the addresses values (=substitute with i18n messages values). Its purpose is to ensure
 * that only i18n messages values get returned from the database when it comes to address values.
 * Note that only the fields that correspond to the address hierarchy levels are being localized.
 */
public class AddressValuesAOPInterceptor implements MethodInterceptor {
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		BaseLocalizer<?> l10n = Context.getRegisteredComponent(ExtI18nConstants.COMPONENT_ADDRESSVALUES_LOCALIZER,
		    AddressValuesLocalizer.class);
		
		return l10n.process(invocation.proceed());
	}
}
