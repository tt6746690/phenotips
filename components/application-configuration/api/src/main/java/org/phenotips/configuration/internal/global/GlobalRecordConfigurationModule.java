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
package org.phenotips.configuration.internal.global;

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.data.Patient;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Default (global) implementation of the {@link RecordConfiguration} role.
 *
 * @version $Id$
 * @since 1.0M9
 */
public class GlobalRecordConfigurationModule implements RecordConfigurationModule
{
    /** The location where preferences are stored. */
    private static final EntityReference PREFERENCES_LOCATION = new EntityReference("WebHome", EntityType.DOCUMENT,
        new EntityReference("data", EntityType.SPACE));

    /** The name of the UIX parameter used for specifying the order of fields and sections. */
    private static final String SORT_PARAMETER_NAME = "order";

    /** Provides access to the current request context. */
    protected Provider<XWikiContext> xcontextProvider;

    /** Lists the patient form sections and fields. */
    protected UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    protected UIExtensionFilter orderFilter;

    /** Logging helper object. */
    private Logger logger = LoggerFactory.getLogger(GlobalRecordConfigurationModule.class);

    /**
     * Simple constructor passing all the needed components.
     *
     * @param xcontextProvider provides access to the current request context
     * @param uixManager the UIExtension manager
     * @param orderFilter UIExtension filter for ordering sections and elements
     */
    //Don't need this yet?
    public GlobalRecordConfigurationModule(Provider<XWikiContext> xcontextProvider, UIExtensionManager uixManager,
        UIExtensionFilter orderFilter)
    {
        this.xcontextProvider = xcontextProvider;
        this.uixManager = uixManager;
        this.orderFilter = orderFilter;
    }

	@Override
	public RecordConfiguration process(RecordConfiguration config)
	{			
        List<RecordSection> result = new LinkedList<RecordSection>();
        List<UIExtension> sections = this.uixManager.get("org.phenotips.patientSheet.content");
        sections = this.orderFilter.filter(sections, SORT_PARAMETER_NAME);
        for (UIExtension sectionExtension : sections) {
            RecordSection section = new DefaultRecordSection(sectionExtension, this.uixManager, this.orderFilter);
            result.add(section);
        }
        config.setSections(result);
        return config;
	}

	@Override
	public int getPriority()
	{
	    return 0;
	}

	@Override
	public String[] getSupportedRecordTypes()
	{
	    String[] recType = {"patient"};
	    
	    return recType;
	}

   
}
