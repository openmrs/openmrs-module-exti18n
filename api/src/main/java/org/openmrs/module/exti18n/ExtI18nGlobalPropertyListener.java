package org.openmrs.module.exti18n;

import org.apache.commons.lang.BooleanUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.exti18n.api.impl.AddressHierarchyI18nCacheImpl;
import org.openmrs.module.exti18n.api.impl.ReverseI18nCacheImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class ExtI18nGlobalPropertyListener implements GlobalPropertyListener {
	
	@Autowired
	protected AddressHierarchyI18nCacheImpl ahI18nCache;
	
	@Autowired
	protected ReverseI18nCacheImpl revI18nCache;
	
	public void globalPropertyChanged(GlobalProperty globalProperty) {
		if (ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT.equalsIgnoreCase(globalProperty.getProperty())) {
			boolean enable = BooleanUtils.toBooleanObject(globalProperty.getPropertyValue()).booleanValue();
			ahI18nCache.setEnabled(enable);
			revI18nCache.setEnabled(enable);
		}
	}
	
	public void globalPropertyDeleted(String propertyName) {
		if (ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT.equalsIgnoreCase(propertyName)) {
			ahI18nCache.setEnabled(false);
			revI18nCache.setEnabled(false);
		}
	}
	
	public boolean supportsPropertyName(String propertyName) {
		return ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT.equalsIgnoreCase(propertyName);
	}
}
