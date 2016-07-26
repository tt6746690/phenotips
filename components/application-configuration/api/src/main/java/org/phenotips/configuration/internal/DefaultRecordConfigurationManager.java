/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 */
package org.phenotips.configuration.internal;

import org.phenotips.Constants;
import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationManager;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.configured.ConfiguredRecordConfiguration;
import org.phenotips.configuration.internal.configured.CustomConfiguration;
import org.phenotips.configuration.internal.global.GlobalRecordConfiguration;
import org.phenotips.configuration.internal.global.GlobalRecordConfigurationModule;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Default implementation for the {@link RecordConfigurationManager} component.
 *
 * @version $Id$
 * @since 1.0M9
 */
@Component
@Singleton
public class DefaultRecordConfigurationManager implements RecordConfigurationManager
{
    /** Reference to the xclass which allows to bind a specific form customization to a patient record. */
    public static final EntityReference STUDY_BINDING_CLASS_REFERENCE = new EntityReference("StudyBindingClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    /** Logging helper. */
    @Inject
    private Logger logger;

    /** Provides access to the current request context. */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /** Lists the patient form sections and fields. */
    @Inject
    private UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    @Inject
    @Named("sortByParameter")
    private UIExtensionFilter orderFilter;

    /** Provides access to the data. */
    @Inject
    private DocumentAccessBridge dab;

    /** Completes xclass references with the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<EntityReference> resolver;

    /** Parses serialized document references into proper references. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> referenceParser;
    
    @Inject
    @Named("wiki")
    private ComponentManager cm;
    
    @Inject
    private RecordConfigurationModule managers;
    
    /* New method
     * Gets all config modules
     * Orders by priority
     * Lists sections from a new empty list
     * */
    public RecordConfiguration getConfiguration(String recordType)
    {
        RecordConfiguration config = null;   	
    	for (RecordConfigurationModule service: get()) {
    		try {
    			config = service.process(config);
    			if (config != null) {
    				return config;
    			}
    		} catch (Exception ex) {
    			this.logger.warn("*Display error message*");
    		}    		
    	}
        return null;
    }

    public List<RecordConfigurationModule> get()
    {
    	try{
    		List<RecordConfigurationModule> sections = new LinkedList<>();
    		sections.addAll(this.cm.<RecordConfigurationModule>getInstanceList(RecordConfigurationModule.class));
    		Collections.sort(sections, RecordSectionComparator.INSTANCE);
    		return sections;
    	} catch (ComponentLookupException ex) {
    		this.logger.warn("*Display error message*");
    	}
    	return null;
    }
    
    private static final class RecordSectionComparator implements Comparator<RecordConfigurationModule>
    {
    	private static final RecordSectionComparator INSTANCE = new RecordSectionComparator();
    			
		@Override
		public int compare(RecordConfigurationModule o1, RecordConfigurationModule o2) {
			int result = o2.getPriority() - o1.getPriority();
			//If they happen to have the same priority, order alphabetically by their name
			if(result == 0) {
				result = o1.getClass().getSimpleName().compareToIgnoreCase(o2.getClass().getSimpleName());
			}
			return result;
		}
    	
    }

    @Deprecated
    @Override
    public RecordConfiguration getActiveConfiguration()
    {
        RecordConfiguration boundConfig = getBoundConfiguration();
        if (boundConfig != null) {
            return boundConfig;
        }
        return new GlobalRecordConfiguration(this.xcontextProvider, this.uixManager, this.orderFilter);
    }

    /**
     * If the current document is a patient record, and it has a valid specific study binding specified, then return
     * that configuration.
     *
     * @return a form configuration, if one is bound to the current document, or {@code null} otherwise
     */
   
    private RecordConfiguration getBoundConfiguration()
    {
        if (this.dab.getCurrentDocumentReference() == null) {
            // Non-interactive requests, use the default configuration
            return null;
        }
        String boundConfig =
            (String) this.dab.getProperty(this.dab.getCurrentDocumentReference(),
                this.resolver.resolve(STUDY_BINDING_CLASS_REFERENCE), "studyReference");
        if (StringUtils.isNotBlank(boundConfig)) {
            try {
                XWikiContext context = this.xcontextProvider.get();
                XWikiDocument doc = context.getWiki().getDocument(this.referenceParser.resolve(boundConfig), context);
                if (doc == null || doc.isNew()) {
                    // Inaccessible or deleted document, use default configuration
                    return null;
                }
                CustomConfiguration configuration =
                    new CustomConfiguration(doc.getXObject(RecordConfiguration.CUSTOM_PREFERENCES_CLASS));
                return new ConfiguredRecordConfiguration(configuration, this.xcontextProvider, this.uixManager,
                    this.orderFilter);
            } catch (Exception ex) {
                this.logger.warn("Failed to read the bound configuration [{}] for [{}]: {}", boundConfig,
                    this.dab.getCurrentDocumentReference(), ex.getMessage());
            }
        }
        return null;
    }
}
