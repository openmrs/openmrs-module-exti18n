package org.openmrs.module.exti18n.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.module.exti18n.api.AddressHierarchyI18nCache;

/**
 * This is a reverse I18N cache specific to dealing with Address Hierarchy.
 */
public class AddressHierarchyI18nCacheImpl implements AddressHierarchyI18nCache {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private ReverseI18nCacheImpl i18nCache; // we just encapsulate a base cache
	
	private List<String> orderedAddressFields = new ArrayList<String>();
	
	public void init() {
		i18nCache = new ReverseI18nCacheImpl();
	}
	
	@Override
	public List<String> getOrderedAddressFields() {
		return orderedAddressFields;
	}
	
	@Override
	public void setOrderedAddressFields(List<String> orderedAddressFields) {
		this.orderedAddressFields = orderedAddressFields;
	}
	
	/**
	 * @param searchString A partial search string in the current locale.
	 * @return The list of matched possible i18n message keys.
	 */
	@Override
	public List<String> getMessageKeysByLikeName(String searchString) {
		
		if (!isEnabled() || StringUtils.isEmpty(searchString)) {
			return Collections.emptyList();
		}
		
		List<String> keys = new ArrayList<String>();
		for (Map.Entry<String, String> pair : i18nCache.getCache().entrySet()) {
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
	@Override
	public PersonAddress getI18nPersonAddress(PersonAddress address) {
		
		if (!isEnabled()) {
			return address;
		}
		
		final PersonAddress i18naddress = (PersonAddress) address.clone(); // TODO: We may drop this
		
		Map<String, String> addressAsMap = new HashMap<String, String>();
		
		for (String fieldName : getOrderedAddressFields()) {
			String fieldValue = "";
			try {
				fieldValue = BeanUtils.getProperty(i18naddress, fieldName);
				if (!StringUtils.isEmpty(fieldValue)) {
					
					addressAsMap.put(fieldName, fieldValue);
					
					if (i18nCache.getCache().containsKey(fieldValue.toLowerCase())) {
						BeanUtils.setProperty(i18naddress, fieldName, i18nCache.getCache().get(fieldValue.toLowerCase()));
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
	
	@Override
	public void reset() {
		i18nCache.reset();
		
	}
	
	@Override
	public boolean isEnabled() {
		return i18nCache.isEnabled();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		i18nCache.setEnabled(enabled);
	}
	
	@Override
	public String getMessage(String key, Locale locale) {
		return i18nCache.getMessage(key, locale);
	}
	
	@Override
	public String getMessage(String key) {
		return i18nCache.getMessage(key);
	}
	
	@Override
	public String getMessageKey(String message) {
		return i18nCache.getMessageKey(message);
	}
}
