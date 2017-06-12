package org.openmrs.module.exti18n.icpt;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsMetadata;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;

public class MetadataAOPInterceptor implements MethodInterceptor {
	
	protected static final Log log = LogFactory.getLog(MetadataAOPInterceptor.class);
	
	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		
		final Object original = invocation.proceed();
		if (original == null) {
			return original;
		}
		
		Set<String> targetMethods = new HashSet<String>();
		
		targetMethods.add("saveLocation");
		targetMethods.add("getLocation");
		targetMethods.add("getDefaultLocation");
		targetMethods.add("getLocationByUuid");
		targetMethods.add("retireLocation");
		targetMethods.add("unretireLocation");
		
		if (invocation.getThis() instanceof LocationService) {
			boolean methodTargeted = targetMethods.contains(invocation.getMethod().getName());
			if (methodTargeted && Location.class.isAssignableFrom(original.getClass())) {
				return l10nMetadata((BaseOpenmrsMetadata) original);
			}
		}
		
		return original;
	}
	
	/**
	 * Localizes a {@link BaseOpenmrsMetadata}
	 * 
	 * @param metadata
	 * @return The localized metadata instance.
	 */
	public static BaseOpenmrsMetadata l10nMetadata(BaseOpenmrsMetadata metadata) {

		Context.evictFromSession(metadata); // so that the settings below never get flushed
		
		metadata.setName(Context.getMessageSourceService().getMessage(metadata.getName()));
		metadata.setDescription(Context.getMessageSourceService().getMessage(metadata.getDescription()));
		
		return metadata;
	}
}
