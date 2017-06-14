package org.openmrs.module.exti18n.api;

import java.util.Locale;

import org.openmrs.messagesource.MessageSourceService;

public interface ReverseI18nCache {
	
	/**
	 * Resets the cache.
	 */
	public void reset();
	
	/**
	 * @return A boolean indicating whether the reverse translation caching is enabled.
	 */
	public boolean isEnabled();
	
	/**
	 * Enables/disables the reverse translation caching. Setting enabled to false also resets the
	 * cache.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);
	
	/**
	 * This method translates a i18n message key based on the provided locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String, Object[], Locale)}, however
	 * while doing so it also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @param locale The locale to use for the translation.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key, Locale locale);
	
	/**
	 * This method translates a i18n message key based on the context's locale. From the outside if
	 * does the same as {@link MessageSourceService#getMessage(String)}, however while doing so it
	 * also caches the reverse translation.
	 * 
	 * @param key The i18n message key.
	 * @return The translation of the i18n message key.
	 */
	public String getMessage(String key);
	
	/**
	 * @param message A translated expression in the current locale.
	 * @return The i18n message key.
	 */
	public String getMessageKey(String message);
}
