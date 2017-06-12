package org.openmrs.module.exti18n.icpt;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.exti18n.api.AOPModuleContextSensitiveTest;
import org.openmrs.module.exti18n.api.TestWithAOP;
import org.openmrs.module.exti18n.api.TestsMessageSource;

public class MetadataAOPInterceptorTest extends AOPModuleContextSensitiveTest {
	
	@Override
	protected void setInterceptorAndServices(TestWithAOP testCase) {
		testCase.setInterceptor(MetadataAOPInterceptor.class);
		testCase.addService(LocationService.class);
	}
	
	@Before
	public void setup() {
		TestsMessageSource source = (TestsMessageSource) Context.getMessageSourceService().getActiveMessageSource();
		source.addMessageProperties("org/openmrs/module/exti18n/include/metadata.properties");
		source.refreshCache();
	}
	
	protected void assertL10nLocation(Location location) {
		Assert.assertEquals("Health center", location.getName());
		Assert.assertEquals("This is a health centre location.", location.getDescription());
	}
	
	@Test
	public void invoke_shouldLocalizeLocations() {

		// Setup
		LocationService ls = Context.getLocationService();
		String uuid = "b984a88e-371b-4cf1-8738-22b3441b71af";
		Location location = new Location();
		String name = "metadata.location.healthcenter.name";
		location.setUuid(uuid);
		location.setName(name);
		location.setDescription("metadata.location.healthcenter.description");
		
		// Replay
		Context.setLocale(Locale.ENGLISH);
		location = ls.saveLocation(location);

		// Verif l10n
		assertL10nLocation(location);
		location.setName("some random name that should not be persisted");
		assertL10nLocation(ls.getLocationByUuid(uuid));
		assertL10nLocation(ls.getLocation(name));
		
		// Verif that object can still be used
		Visit visit = Context.getVisitService().getVisit(1);
		visit.setLocation(location);
		visit = Context.getVisitService().saveVisit(visit);
		Obs obs = Context.getObsService().getObs(7);
		obs.setLocation(location);
		Context.getObsService().saveObs(obs, "test");
		
		// Verif that object can be resaved
		location.setName("some.other.key");
		location = ls.saveLocation(location);
		Assert.assertEquals("some.other.key", location.getName());
	}
}
