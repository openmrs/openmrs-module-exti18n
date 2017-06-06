package org.openmrs.module.exti18n.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.impl.AddressHierarchyI18nCache;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Extend this class to run context sensitive tests with i18n enabled, except for Spring AOP.
 * 
 * @see {@link I18nAOPBaseModuleContextSensitiveTest}
 */
public abstract class I18nBaseModuleContextSensitiveTest extends BaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-i18n-dataset.xml";
	
	protected static final String MODULES_TO_LOAD = "org/openmrs/module/addresshierarchy/include/exti18n.omod";
	
	@Autowired
	protected AddressHierarchyI18nCache i18nCache;
	
	@Before
	public void setupI18n() throws Exception {
		
		runtimeProperties.setProperty(ModuleConstants.RUNTIMEPROPERTY_MODULE_LIST_TO_LOAD, MODULES_TO_LOAD);
		ModuleUtil.startup(runtimeProperties);
		Assert.assertTrue(ModuleFactory.isModuleStarted("exti18n"));
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT, "true"));
		
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);
	}
	
	@After
	public void tearDownI18n() {
		ModuleFactory.stopModule(ModuleFactory.getModuleById("exti18n"));
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_AH_I18N_SUPPORT, "false"));
	}
}
