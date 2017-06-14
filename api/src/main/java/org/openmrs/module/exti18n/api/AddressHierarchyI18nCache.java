package org.openmrs.module.exti18n.api;

import java.util.List;

import org.openmrs.PersonAddress;

public interface AddressHierarchyI18nCache extends ReverseI18nCache {
	
	/**
	 * @return The ordered list of address fields that are subject to i18n.
	 */
	public List<String> getOrderedAddressFields();
	
	/**
	 * To initialize the cache.
	 */
	public void setOrderedAddressFields(List<String> orderedAddressFields);
	
	/**
	 * @param searchString
	 * @return Mathing i18n message keys matching the search string.
	 */
	public List<String> getMessageKeysByLikeName(String searchString);
	
	/**
	 * Reverse translates each field of a {@link PersonAddress} based on what is in the reverse
	 * translation cache.
	 * 
	 * @param address An address where some or all address fields are translated expressions in the
	 *            current locale.
	 * @return An address where all address fields are replaced with i18n messages keys, when
	 *         possible.
	 */
	public PersonAddress getI18nPersonAddress(PersonAddress address);
}
