/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.exti18n;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class ExtI18nActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see #started()
	 */
	public void started() {
		
		// Ensuring that the i18n cache is not missing out on its enabling GP
		{
			String i18nEnabled = Context.getService(AdministrationService.class).getGlobalProperty(
			    ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT, new Boolean(false).toString());
			AddressHierarchyI18nCache i18nCache = Context.getRegisteredComponent("addressHierarchyI18nCache",
			    AddressHierarchyI18nCache.class);
			i18nCache.setEnabled(BooleanUtils.toBooleanObject(i18nEnabled).booleanValue());
		}
		log.info("Started " + ExtI18nConstants.MODULE_NAME);
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Stopped " + ExtI18nConstants.MODULE_NAME);
	}
	
}
