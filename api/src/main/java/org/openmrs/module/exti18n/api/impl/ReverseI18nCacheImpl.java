package org.openmrs.module.exti18n.api.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.exti18n.api.ReverseI18nCache;

/**
 * The reverse translation cache that allows to go back from translated words in the current locale
 * to i18n messages keys. This class also exposes various utility i18n and l10n methods.
 */
public class ReverseI18nCacheImpl implements ReverseI18nCache {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	// The cache of translated words to i18n message keys
	protected Map<String, String> cache = new HashMap<String, String>();
	
	protected boolean enabled = true;
	
	/**
	 * Accessor to the internal map. This method is not part of the API.
	 */
	public Map<String, String> getCache() {
		return cache;
	}
	
	/**
	 * Resets the reverse translation cache.
	 */
	@Override
	public void reset() {
		cache.clear();
	}
	
	/**
	 * @return A boolean indicating whether the reverse translation caching is enabled.
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * Enables/disables the reverse translation caching. Setting enabled to false also resets the
	 * cache.
	 * 
	 * @param enabled
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (enabled == false) {
			reset();
		}
		this.enabled = enabled;
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
	@Override
	public String getMessage(String key, Locale locale) {
		
		if (!isEnabled() || StringUtils.isEmpty(key)) {
			return key;
		}
		
		String translation = Context.getMessageSourceService().getMessage(key, null, locale);
		if (!StringUtils.equals(translation, key)) {
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
	@Override
	public String getMessage(String key) {
		return getMessage(key, Context.getLocale());
	}
	
	/**
	 * @param message A translated expression in the current locale.
	 * @return The i18n message key.
	 */
	@Override
	public String getMessageKey(String message) {
		
		if (!isEnabled() || StringUtils.isEmpty(message)) {
			return message;
		}
		
		String key = message;
		if (cache.containsKey(message.toLowerCase())) {
			key = cache.get(message.toLowerCase());
		}
		return key;
	}
}
