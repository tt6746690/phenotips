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
package org.phenotips.configuration.internal.configured;

import org.phenotips.Constants;
import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.uiextension.UIExtensionFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Implementation of {@link RecordConfiguration} that takes into account a {@link CustomConfiguration custom
 * configuration}. Its {@link #getPriority() priority} is {@code 50}.
 *
 * @version $Id$
 * @since 1.3
 */
@Named("Study")
public class StudyRecordConfigurationModule implements RecordConfigurationModule
{
    /**
     * Reference to the xclass which allows to bind a specific form customization to a patient record.
     */
    public static final EntityReference STUDY_BINDING_CLASS_REFERENCE = new EntityReference("StudyBindingClass",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    /** Provides access to the data. */
    @Inject
    private DocumentAccessBridge dab;

    /** Completes xclass references with the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<EntityReference> resolver;

    /** Provides access to the current request context. */
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /** Sorts fields by their declared order. */
    @Inject
    @Named("sortByParameter")
    private UIExtensionFilter orderFilter;

    /** Parses serialized document references into proper references. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> referenceParser;

    /** Logging helper. */
    @Inject
    private Logger logger;

    @Override
    public RecordConfiguration process(RecordConfiguration config)
    {
        CustomConfiguration configObj = getBoundConfiguration();
        List<RecordSection> resultSections = new ArrayList<>();
        RecordConfiguration updatedConfigs = new DefaultRecordConfiguration();
        final List<String> elementOverrides = configObj.getFieldsOverride();
        final List<String> sectionOverrides = configObj.getSectionsOverride();

        for (RecordSection section : config.getAllSections()) {
            if (sectionOverrides != null && !sectionOverrides.isEmpty()
                && !sectionOverrides.contains(section.getExtension().getId())) {
                continue;
            }
            // Find if elements are enabled
            List<RecordElement> updatedElements = new LinkedList<>();
            for (RecordElement element : section.getAllElements()) {
                if (elementOverrides == null || elementOverrides.isEmpty()
                    || elementOverrides.contains(element.getExtension().getId())) {
                    updatedElements.add(element);
                }
            }
            Collections.<RecordElement>sort(updatedElements, new Comparator<RecordElement>()
            {
                @Override
                public int compare(RecordElement o1, RecordElement o2)
                {
                    int i1 = elementOverrides.indexOf(o1.getExtension().getId());
                    int i2 = elementOverrides.indexOf(o2.getExtension().getId());
                    return (i2 == -1 || i1 == -1) ? (i2 - i1) : (i1 - i2);
                }
            });
            // Add list of sorted elements to section
            section.setElements(updatedElements);
            resultSections.add(section);
        }

        // Sort the list of sections
        if (sectionOverrides != null && !sectionOverrides.isEmpty()) {
            Collections.<RecordSection>sort(resultSections, new Comparator<RecordSection>()
            {
                @Override
                public int compare(RecordSection o1, RecordSection o2)
                {
                    int i1 = sectionOverrides.indexOf(o1.getExtension().getId());
                    int i2 = sectionOverrides.indexOf(o2.getExtension().getId());
                    return (i2 == -1 || i1 == -1) ? (i2 - i1) : (i1 - i2);
                }
            });
        }
        updatedConfigs.setSections(resultSections);

        return updatedConfigs;
    }

    @Override
    public int getPriority()
    {
        return 50;
    }

    @Override
    public String[] getSupportedRecordTypes()
    {
        String[] recType = { "patient" };

        return recType;
    }

    /**
     * If the current document is a patient record, and it has a valid specific study binding specified, then return
     * that configuration.
     *
     * @return a form configuration, if one is bound to the current document, or {@code null} otherwise
     */
    private CustomConfiguration getBoundConfiguration()
    {
        if (this.dab.getCurrentDocumentReference() == null) {
            // Non-interactive requests, use the default configuration
            return null;
        }
        String boundConfig = (String) this.dab.getProperty(this.dab.getCurrentDocumentReference(),
            this.resolver.resolve(STUDY_BINDING_CLASS_REFERENCE), "studyReference");
        if (StringUtils.isNotBlank(boundConfig)) {
            try {
                XWikiContext context = this.xcontextProvider.get();
                XWikiDocument doc = context.getWiki().getDocument(this.referenceParser.resolve(boundConfig), context);
                if (doc == null || doc.isNew()) {
                    // Inaccessible or deleted document, use default
                    // configuration
                    return null;
                }
                return new CustomConfiguration(doc.getXObject(RecordConfiguration.CUSTOM_PREFERENCES_CLASS));
            } catch (Exception ex) {
                this.logger.warn("Failed to read the bound configuration [{}] for [{}]: {}", boundConfig,
                    this.dab.getCurrentDocumentReference(), ex.getMessage());
            }
        }
        return null;
    }
}
