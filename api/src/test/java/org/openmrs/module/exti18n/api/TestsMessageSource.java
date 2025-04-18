/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.exti18n.api;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.messagesource.MutableMessageSource;
import org.openmrs.messagesource.PresentationMessage;
import org.openmrs.messagesource.PresentationMessageMap;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;

/**
 * Registers the custom message source service
 * 
 * @see https 
 *      ://github.com/openmrs/openmrs-module-reporting/blob/037c74949f0e01f5a5cb04c5467912654d808765
 *      /api-tests/src/test/java/org/openmrs/module/reporting/test/CustomMessageSource.java
 * @see https://talk.openmrs.org/t/address-hierarchy-support-for-i18n/10415/19?u=mksd
 */
public class TestsMessageSource extends AbstractMessageSource implements MutableMessageSource, ApplicationContextAware {
	
	protected static final Log log = LogFactory.getLog(TestsMessageSource.class);
	
	private Map<Locale, PresentationMessageMap> cache;
	
	private boolean showMessageCode = false;
	
	public static final String GLOBAL_PROPERTY_SHOW_MESSAGE_CODES = "custommessage.showMessageCodes";
	
	protected Map<String, Locale> messageProperties = new LinkedHashMap<String, Locale>(); // resources to be cached
	
	private Set<String> cachedProperties = new HashSet<String>(); // flags the resources already cached
	
	/**
	 * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		MessageSourceService svc = (MessageSourceService) context.getBean("messageSourceServiceTarget");
		MessageSource activeSource = svc.getActiveMessageSource();
		setParentMessageSource(activeSource);
		svc.setActiveMessageSource(this);
	}
	
	/**
	 * @return the cached messages, merged from the custom source and the parent source
	 */
	public synchronized Map<Locale, PresentationMessageMap> getCachedMessages() {
		if (cache == null) {
			refreshCache();
		}
		return cache;
	}
	
	/**
	 * @return all message codes defined in the system
	 */
	public Set<String> getAllMessageCodes() {
		return getAllMessagesByCode().keySet();
	}
	
	/**
	 * @return a Map from code to Map of Locale string to message
	 */
	public Map<String, Map<Locale, PresentationMessage>> getAllMessagesByCode() {
		Map<String, Map<Locale, PresentationMessage>> ret = new TreeMap<String, Map<Locale, PresentationMessage>>();
		Map<Locale, PresentationMessageMap> m = getCachedMessages();
		for (Locale locale : m.keySet()) {
			PresentationMessageMap pmm = m.get(locale);
			for (String code : pmm.keySet()) {
				Map<Locale, PresentationMessage> messagesForCode = ret.get(code);
				if (messagesForCode == null) {
					messagesForCode = new LinkedHashMap<Locale, PresentationMessage>();
					ret.put(code, messagesForCode);
				}
				messagesForCode.put(locale, pmm.get(code));
			}
		}
		return ret;
	}
	
	/**
	 * @param pm the presentation message to add to the cache
	 * @param override if true, should override any existing message
	 */
	public void addPresentationMessageToCache(PresentationMessage pm, boolean override) {
		PresentationMessageMap pmm = getCachedMessages().get(pm.getLocale());
		if (pmm == null) {
			pmm = new PresentationMessageMap(pm.getLocale());
			getCachedMessages().put(pm.getLocale(), pmm);
		}
		if (pmm.get(pm.getCode()) == null || override) {
			pmm.put(pm.getCode(), pm);
		}
	}
	
	/**
	 * Adds a message properties file to be added to the cache. The locale is parsed out of the file
	 * name. Eg 'messages_fr.properties' --> 'fr'.
	 * 
	 * @param resourcePath The path to the message properties file.
	 */
	public void addMessageProperties(String resourcePath) {
		String fileName = Paths.get(resourcePath).getFileName().toString();
		String[] parts = FilenameUtils.getBaseName(fileName).split("_");
		Locale locale = Locale.ENGLISH;
		if (parts.length == 2) {
			locale = LocaleUtils.toLocale(parts[1]);
		}
		messageProperties.put(resourcePath, locale);
	}
	
	/**
	 * Refreshes the cache, merged from the custom source and the parent source
	 */
	public synchronized void refreshCache() {
		
		if (MapUtils.isEmpty(cache)) {
			cache = new HashMap<Locale, PresentationMessageMap>();
		}
		for (Map.Entry<String, Locale> entry : messageProperties.entrySet()) {
			if (cachedProperties.contains(entry.getKey())) {
				continue;
			}
			PresentationMessageMap pmm = new PresentationMessageMap(entry.getValue());
			Properties messages = loadPropertiesFromClasspath(entry.getKey());
			for (String code : messages.stringPropertyNames()) {
				String message = messages.getProperty(code);
				message = message.replace("{{", "'{{'");
				message = message.replace("}}", "'}}'");
				pmm.put(code, new PresentationMessage(code, entry.getValue(), message, null));
			}
			cache.put(entry.getValue(), pmm);
			cachedProperties.add(entry.getKey());
		}
	}
	
	/**
	 * @see MutableMessageSource#getLocales()
	 */
	@Override
	public Collection<Locale> getLocales() {
		MutableMessageSource m = getMutableParentSource();
		Set<Locale> s = new HashSet<Locale>(m.getLocales());
		s.addAll(cache.keySet());
		return s;
	}
	
	/**
	 * @see MutableMessageSource#publishProperties(Properties, String, String, String, String)
	 */
	@SuppressWarnings("deprecation")
	public void publishProperties(Properties props, String locale, String namespace, String name, String version) {
		try {
			Class c = getMutableParentSource().getClass();
			Method m = c.getMethod("publishProperties", Properties.class, String.class, String.class, String.class,
			    String.class);
			m.invoke(getMutableParentSource(), props, locale, namespace, name, version);
		}
		catch (Exception e) {
			// DO NOTHING
		}
	}
	
	/**
	 * @see MutableMessageSource#getPresentations()
	 */
	@Override
	public Collection<PresentationMessage> getPresentations() {
		Collection<PresentationMessage> ret = new ArrayList<PresentationMessage>();
		for (PresentationMessageMap pmm : getCachedMessages().values()) {
			ret.addAll(pmm.values());
		}
		return ret;
	}
	
	/**
	 * @see MutableMessageSource#getPresentationsInLocale(Locale)
	 */
	@Override
	public Collection<PresentationMessage> getPresentationsInLocale(Locale locale) {
		PresentationMessageMap pmm = getCachedMessages().get(locale);
		if (pmm == null) {
			return new HashSet<PresentationMessage>();
		}
		return pmm.values();
	}
	
	/**
	 * @see MutableMessageSource#addPresentation(PresentationMessage)
	 */
	@Override
	public void addPresentation(PresentationMessage message) {
		addPresentationMessageToCache(message, true);
	}
	
	/**
	 * @see MutableMessageSource#getPresentation(String, Locale)
	 */
	@Override
	public PresentationMessage getPresentation(String code, Locale locale) {
		PresentationMessageMap pmm = getCachedMessages().get(locale);
		if (pmm == null) {
			return null;
		}
		return pmm.get(code);
	}
	
	/**
	 * @see MutableMessageSource#removePresentation(PresentationMessage)
	 */
	@Override
	public void removePresentation(PresentationMessage message) {
		PresentationMessageMap pmm = getCachedMessages().get(message.getLocale());
		if (pmm != null) {
			pmm.remove(message.getCode());
		}
		getMutableParentSource().removePresentation(message);
	}
	
	/**
	 * @see MutableMessageSource#merge(MutableMessageSource, boolean)
	 */
	@Override
	public void merge(MutableMessageSource fromSource, boolean overwrite) {
		getMutableParentSource().merge(fromSource, overwrite);
	}
	
	/**
	 * @see AbstractMessageSource#resolveCode(String, Locale)
	 */
	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		if (showMessageCode) {
			return new MessageFormat(code);
		}
		PresentationMessage pm = getPresentation(code, locale); // Check exact match
		if (pm == null) {
			if (locale.getVariant() != null) {
				pm = getPresentation(code, new Locale(locale.getLanguage(), locale.getCountry())); // Try to match language and country
				if (pm == null) {
					pm = getPresentation(code, new Locale(locale.getLanguage())); // Try to match language only
				}
			}
		}
		if (pm != null) {
			return new MessageFormat(pm.getMessage());
		}
		return null;
	}
	
	/**
	 * For some reason, this is needed to get the default text option in message tags working
	 * properly
	 * 
	 * @see AbstractMessageSource#getMessageInternal(String, Object[], Locale)
	 */
	@Override
	protected String getMessageInternal(String code, Object[] args, Locale locale) {
		String s = super.getMessageInternal(code, args, locale);
		if (s == null || s.equals(code)) {
			return null;
		}
		return s;
	}
	
	/**
	 * Convenience method to get the parent message source as a MutableMessageSource
	 */
	public MutableMessageSource getMutableParentSource() {
		return (MutableMessageSource) getParentMessageSource();
	}
	
	public static Properties loadPropertiesFromClasspath(String location) {
		Properties ret = new Properties();
		InputStream is = null;
		try {
			is = OpenmrsClassLoader.getInstance().getResourceAsStream(location);
			ret.load(new InputStreamReader(is, "UTF-8"));
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to load properties from classpath at " + location, e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
		return ret;
	}
}
