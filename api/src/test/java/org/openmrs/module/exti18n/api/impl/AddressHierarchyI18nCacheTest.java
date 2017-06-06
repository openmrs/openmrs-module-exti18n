package org.openmrs.module.exti18n.api.impl;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.exti18n.api.impl.I18nBaseModuleContextSensitiveTest;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.test.Verifies;

public class AddressHierarchyI18nCacheTest extends I18nBaseModuleContextSensitiveTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	// Reflection access to i18n's private members
	// See http://stackoverflow.com/a/34658/321797
	private Set<Locale> cachedLocales;
	
	private Map<String, String> cache;
	
	@Before
	public void setup() throws Exception {
		Field field = i18nCache.getClass().getDeclaredField("cache");
		field.setAccessible(true);
		cache = (Map<String, String>) field.get(i18nCache);
		
		field = i18nCache.getClass().getDeclaredField("fullyCachedLocales");
		field.setAccessible(true);
		cachedLocales = (Set<Locale>) field.get(i18nCache);
		
		i18nCache.reset();
	}
	
	@Test
	@Verifies(value = "should fill the cache accroding to the provided locale", method = "init(Locale locale)")
	public void init_shouldFillTheCacheAccordingToLocale() {
		
		Assert.assertTrue(cache.isEmpty());
		
		// Filling the cache in fr locale
		i18nCache.init(Locale.FRENCH);
		Assert.assertTrue(cachedLocales.contains(Locale.FRENCH));
		
		// We should now find in there fr words that we never translated directly
		Assert.assertTrue(cache.containsKey("Comté de Suffolk".toLowerCase()));
		Assert.assertTrue(cache.containsKey("Comté de la Providence".toLowerCase()));
		
		// However the corresponding en words should not be found
		Assert.assertFalse(cache.containsKey("Suffolk County".toLowerCase()));
		Assert.assertFalse(cache.containsKey("Providence County".toLowerCase()));
		
		// Filling the cache in en locale now
		Assert.assertFalse(cachedLocales.contains(Locale.ENGLISH));
		Context.getUserContext().setLocale(Locale.ENGLISH);
		i18nCache.init(Locale.ENGLISH);
		Assert.assertTrue(cachedLocales.contains(Locale.ENGLISH));
		
		// We should now find in there en words that we never translated directly
		Assert.assertTrue(cache.containsKey("Suffolk County".toLowerCase()));
		Assert.assertTrue(cache.containsKey("Providence County".toLowerCase()));
	}
	
	@Test
	@Verifies(value = "should fill the cache one call at a time", method = "getMessage(String key)")
	public void getMessage_shouldFillTheCacheAccordingToLocale() {
		
		AddressHierarchyService ahs = Context.getService(AddressHierarchyService.class);
		AddressHierarchyEntry entryPlymouthCounty = ahs.getAddressHierarchyEntry(4);
		AddressHierarchyEntry entryBeaconHill = ahs.getAddressHierarchyEntry(13);
		
		Assert.assertTrue(cache.isEmpty());
		
		// Using the cache in en locale
		Context.setLocale(Locale.ENGLISH);
		Assert.assertEquals("Plymouth County", i18nCache.getMessage(entryPlymouthCounty.getName()));
		Assert.assertEquals(1, cache.size());
		Assert.assertEquals("Beacon Hill", i18nCache.getMessage(entryBeaconHill.getName()));
		Assert.assertEquals(2, cache.size());
		Assert.assertTrue(cachedLocales.isEmpty());
		
		// Then using the cache in fr locale
		Context.setLocale(Locale.FRENCH);
		Assert.assertEquals("Comté de Plymouth", i18nCache.getMessage(entryPlymouthCounty.getName()));
		Assert.assertEquals(3, cache.size());
		Assert.assertEquals("Colline de Beacon", i18nCache.getMessage(entryBeaconHill.getName()));
		Assert.assertEquals(4, cache.size());
		Assert.assertTrue(cachedLocales.isEmpty());
	}
	
	@Test
	@Verifies(value = "should return a copy of the PersonAddress instance with address values as i18n messages keys", method = "getI18nPersonAddress(PersonAddress address)")
	public void getI18nPersonAddress_shouldReturnI18nPersonAddressAsCopy() {
		
		Assert.assertTrue(cache.isEmpty());
		
		// Using the cache in en locale
		Context.setLocale(Locale.ENGLISH);
		PersonAddress address = new PersonAddress();
		address.setCountry("United States");
		address.setStateProvince("Massachusetts");
		address.setCountyDistrict("Suffolk County");
		address.setCityVillage("Boston");
		int originalJavaId = System.identityHashCode(address);
		
		address = i18nCache.getI18nPersonAddress(address);
		Assert.assertTrue(cachedLocales.contains(Locale.ENGLISH));
		Assert.assertFalse(cachedLocales.contains(Locale.FRENCH));
		
		Assert.assertNotEquals(originalJavaId, System.identityHashCode(address));
		Assert.assertEquals("addresshierarchy.unitedStates", address.getCountry());
		Assert.assertEquals("addresshierarchy.massachusetts", address.getStateProvince());
		Assert.assertEquals("addresshierarchy.suffolkCounty", address.getCountyDistrict());
		Assert.assertEquals("addresshierarchy.boston", address.getCityVillage());
		
		// Using the cache in fr locale
		Context.setLocale(Locale.FRENCH);
		address = new PersonAddress();
		address.setCountry("Etats-Unis");
		address.setStateProvince("Massachusetts");
		address.setCountyDistrict("Comté de Suffolk");
		address.setCityVillage("Boston");
		originalJavaId = System.identityHashCode(address);
		
		address = i18nCache.getI18nPersonAddress(address);
		Assert.assertTrue(cachedLocales.contains(Locale.FRENCH));
		
		Assert.assertNotEquals(originalJavaId, System.identityHashCode(address));
		Assert.assertEquals("addresshierarchy.unitedStates", address.getCountry());
		Assert.assertEquals("addresshierarchy.massachusetts", address.getStateProvince());
		Assert.assertEquals("addresshierarchy.suffolkCounty", address.getCountyDistrict());
		Assert.assertEquals("addresshierarchy.boston", address.getCityVillage());
	}
}
