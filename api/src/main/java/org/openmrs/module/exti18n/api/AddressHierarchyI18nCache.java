package org.openmrs.module.exti18n.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

/**
 * The reverse translation cache that allows to go back from translated words in the current locale
 * to i18n messages keys. This class also exposes various utility i18n and l10n methods.
 */
public class AddressHierarchyI18nCache {
	
	protected static final Log log = LogFactory.getLog(AddressHierarchyI18nCache.class);
	
	private List<String> orderedAddressFields = new ArrayList<String>();
	
	private Set<Locale> fullyCachedLocales = new HashSet<Locale>();
	
	// The cache of translated words to i18n message keys
	protected Map<String, String> cache = new HashMap<String, String>();
	
	protected boolean enabled = true;
	
	/**
	 * Resets the reverse translation cache.
	 */
	public void reset() {
		cache.clear();
		fullyCachedLocales.clear();
	}
	
	/**
	 * @return A boolean indicating whether the reverse translation caching is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Enables/disables the reverse translation caching. Setting enabled to false also resets the
	 * cache.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		if (enabled == false) {
			reset();
		}
		this.enabled = enabled;
	}
	
	/**
	 * @return The ordered list of address fields that are subject to i18n.
	 */
	public List<String> getOrderedAddressFields() {
		return orderedAddressFields;
	}
	
	/**
	 * Initializes the reverse translation cache for a given locale.
	 * 
	 * @param locale
	 */
	public void init(Locale locale) {
		
		if (!isEnabled() || fullyCachedLocales.contains(locale)) {
			return;
		}
		
		AddressHierarchyService ahService = Context.getService(AddressHierarchyService.class);
		List<AddressHierarchyLevel> orderedLevels = ahService.getOrderedAddressHierarchyLevels(false);
		
		if (CollectionUtils.isEmpty(orderedLevels)) {
			log.warn("The Address Hierarchy reverse translation cache was not initialized because there are no mapped address hierarchy levels.");
			return;
		}
		
		for (AddressHierarchyLevel level : orderedLevels) {
			orderedAddressFields.add(level.getAddressField().getName());
		}
		
		ListIterator<AddressHierarchyLevel> iterator = orderedLevels.listIterator(orderedLevels.size());
		// Iterate in reverse.
		while (iterator.hasPrevious()) {
			for (AddressHierarchyEntry entry : ahService.getAddressHierarchyEntriesByLevel(iterator.previous())) {
				getMessage(entry.getName(), locale); // this fills the cache down from the entry level up to the top
			}
		}
		
		fullyCachedLocales.add(locale);
	}
	
	/**
	 * This method translates a i18n message key based on the provided locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String, Object[], Locale)}, however
	 * while doing so it also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @param locale The locale to use for the translation.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key, Locale locale) {
		
		if (!isEnabled() || StringUtils.isEmpty(key)) {
			return key;
		}
		
		String translation = Context.getMessageSourceService().getMessage(key, null, locale);
		if (!fullyCachedLocales.contains(locale) && !StringUtils.equals(translation, key)) {
			cache.put(translation.toLowerCase(), key);
		}
		return translation;
	}
	
	/**
	 * This method translates a i18n message key based on the context's locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String)}, however while doing so it
	 * also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key) {
		return getMessage(key, Context.getLocale());
	}
	
	/**
	 * @param message A translated expression in the current locale.
	 * @param readOnly Set this to true to use the cache as it is.
	 * @return The i18n message key.
	 */
	public String getMessageKey(String message, boolean readOnly) {
		
		if (!isEnabled() || StringUtils.isEmpty(message)) {
			return message;
		}
		
		String key = message;
		if (!readOnly && !fullyCachedLocales.contains(Context.getLocale()) && !cache.containsKey(message.toLowerCase())) {
			init(Context.getLocale());
		}
		if (cache.containsKey(message.toLowerCase())) {
			key = cache.get(message.toLowerCase());
		}
		return key;
	}
	
	/**
	 * @param message A translated expression in the current locale.
	 * @return The i18n message key.
	 */
	public String getMessageKey(String message) {
		return getMessageKey(message, false);
	}
	
	/**
	 * @param searchString A partial search string in the current locale.
	 * @return The list of matched possible i18n message keys.
	 */
	public List<String> getMessageKeysByLikeName(String searchString) {
		
		if (!isEnabled() || StringUtils.isEmpty(searchString)) {
			return Collections.emptyList();
		}
		
		init(Context.getLocale());
		
		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, String> pair : cache.entrySet()) {
			String word = pair.getKey();
			if (StringUtils.containsIgnoreCase(word, searchString)) {
				keys.add(pair.getValue());
			}
		}
		
		return keys;
	}
	
	/**
	 * Reverse translates each field of a {@link PersonAddress} based on what is in the reverse
	 * translation cache.
	 * 
	 * @param address An address where some or all address fields are translated expressions in the
	 *            current locale.
	 * @return An address where all address fields are replaced with i18n messages keys, when
	 *         possible.
	 * @implNote This returns the same object instance as the input {@link PersonAddress} but
	 *           modified (internationalized).
	 */
	public PersonAddress getI18nPersonAddress(PersonAddress address) {
		
		if (!isEnabled()) {
			return address;
		}
		
		final PersonAddress i18naddress = (PersonAddress) address.clone(); // TODO: We may drop this
		init(Context.getLocale());
		
		Map<String, String> addressAsMap = new HashMap<String, String>();
		
		for (String fieldName : getOrderedAddressFields()) {
			String fieldValue = "";
			try {
				fieldValue = BeanUtils.getProperty(i18naddress, fieldName);
				if (!StringUtils.isEmpty(fieldValue)) {
					
					addressAsMap.put(fieldName, fieldValue);
					
					if (cache.containsKey(fieldValue.toLowerCase())) {
						BeanUtils.setProperty(i18naddress, fieldName, cache.get(fieldValue.toLowerCase()));
					}
				}
			}
			catch (Exception e) {
				log.error("'" + fieldName + "' could not be internationalized in " + PersonAddress.class.toString()
				        + " bean: " + i18naddress.toString(), e);
				break;
			}
		}
		
		return i18naddress;
	}
}
