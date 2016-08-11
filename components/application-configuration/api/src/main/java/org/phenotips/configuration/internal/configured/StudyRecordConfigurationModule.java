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

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.global.DefaultRecordSection;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Provider;


import com.xpn.xwiki.XWikiContext;

/**
 * Implementation of {@link RecordConfiguration} that takes into account a {@link CustomConfiguration custom
 * configuration}.
 *
 * @version $Id$
 * @since 1.0M9
 */
public class StudyRecordConfigurationModule implements RecordConfigurationModule
{   
    /** The custom configuration defining this patient record configuration. */
    protected CustomConfiguration configuration;

    /** Provides access to the current request context. */
    protected Provider<XWikiContext> xcontextProvider;

    /** Lists the patient form sections and fields. */
    protected UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    protected UIExtensionFilter orderFilter;

    @Override
    public RecordConfiguration process(RecordConfiguration config)
	{
        List<RecordSection> result = new ArrayList<RecordSection>();
        List<RecordSection> allSections = config.getAllSections();
        final List<String> overrides = this.configuration.getSectionsOverride();
        
        for (RecordSection section : allSections) {
            result.add(new ConfiguredRecordSection(this.configuration, section.getExtension(), this.uixManager,
                this.orderFilter));
        }
        if (overrides != null && !overrides.isEmpty()) {
            Collections.<RecordSection>sort(result, new Comparator<RecordSection>()
            {
                @Override
                public int compare(RecordSection o1, RecordSection o2)
                {
                    int i1 = overrides.indexOf(o1.getExtension().getId());
                    int i2 = overrides.indexOf(o2.getExtension().getId());
                    return (i2 == -1 || i1 == -1) ? (i2 - i1) : (i1 - i2);
                }
            });
        }
        config.setSections(result);
        return config;
	}

    @Override
    public int getPriority()
	{
	    return 100;
	}

    @Override
    public String[] getSupportedRecordTypes()
	{
        String[] recType = {"patient"};
	    
	    return recType;
	}

    
}
