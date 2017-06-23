package org.openmrs.module.exti18n.icpt;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.ReverseI18nCache;
import org.openmrs.module.exti18n.api.TestsMessageSource;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class MetadataHibernateInterceptorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier(ExtI18nConstants.COMPONENT_REVI18N)
	private ReverseI18nCache i18nCache;
	
	@Before
	public void setup() {
		TestsMessageSource source = (TestsMessageSource) Context.getMessageSourceService().getActiveMessageSource();
		source.addMessageProperties("org/openmrs/module/exti18n/include/metadata.properties");
		source.refreshCache();
		
		// filling the cache a little
		i18nCache.getMessage("metadata.healthCenter", Locale.ENGLISH);
		i18nCache.getMessage("metadata.healthCenter.desc", Locale.ENGLISH);
	}
	
	protected void assertI18nLocation(Location location) {
		Assert.assertEquals("metadata.healthCenter", location.getName());
		Assert.assertEquals("metadata.healthCenter.desc", location.getDescription());
	}
	
	@Test
	public void onSaveonFlushDirty_shouldI18nLocations() {
		
		// Setup
		LocationService ls = Context.getLocationService();
		String uuid = "b984a88e-371b-4cf1-8738-22b3441b71af";
		Location location = new Location();
		String name = "metadata.healthCenter";
		location.setUuid(uuid);
		location.setName(name);
		location.setDescription("This is the description of a health centre.");
		
		// Replay
		Context.setLocale(Locale.ENGLISH);
		location = ls.saveLocation(location);
		
		// Verif i18n
		assertI18nLocation(location);
		assertI18nLocation(ls.getLocationByUuid(uuid));
		assertI18nLocation(ls.getLocation(name));
		
		// Verif that object can be resaved
		location.setDescription("Some other phrase.");
		location = ls.saveLocation(location);
		Assert.assertEquals("Some other phrase.", location.getDescription());
		
		// And yet the original key can be used for fetching
		Assert.assertNotNull(ls.getLocation(name));
	}
}
