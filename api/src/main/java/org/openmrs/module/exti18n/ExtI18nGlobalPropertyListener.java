package org.openmrs.module.exti18n;

import org.apache.commons.lang.BooleanUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.module.exti18n.api.impl.AddressHierarchyI18nCache;
import org.springframework.beans.factory.annotation.Autowired;

public class ExtI18nGlobalPropertyListener implements GlobalPropertyListener {
	
	@Autowired
	protected AddressHierarchyI18nCache i18n;
	
	public void globalPropertyChanged(GlobalProperty globalProperty) {
		if (ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT.equalsIgnoreCase(globalProperty.getProperty())) {
			boolean enable = BooleanUtils.toBooleanObject(globalProperty.getPropertyValue()).booleanValue();
			i18n.setEnabled(enable);
		}
	}
	
	public void globalPropertyDeleted(String propertyName) {
		if (ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT.equalsIgnoreCase(propertyName)) {
			i18n.setEnabled(false);
		}
	}
	
	public boolean supportsPropertyName(String propertyName) {
		return ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT.equalsIgnoreCase(propertyName);
	}
}
