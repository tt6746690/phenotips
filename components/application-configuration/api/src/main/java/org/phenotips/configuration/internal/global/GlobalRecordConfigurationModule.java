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

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.xwiki.uiextension.UIExtension;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Default (global) implementation of the {@link RecordConfiguration} role.
 *
 * @version $Id$
 * @since 1.0M9
 */
@Named("Default")
public class GlobalRecordConfigurationModule implements RecordConfigurationModule
{
    /** The name of the UIX parameter used for specifying the order of fields and sections. */
    private static final String SORT_PARAMETER_NAME = "order";

    /** Lists the patient form sections and fields. */
    @Inject
    protected UIExtensionManager uixManager;

    /** Sorts fields by their declared order. */
    @Inject
    @Named("sortByParameter")
    protected UIExtensionFilter orderFilter;

    @Override
	public RecordConfiguration process(RecordConfiguration config)
	{			
        List<RecordSection> result = new LinkedList<RecordSection>();
        RecordConfiguration updatedConfig = new DefaultRecordConfiguration();
        List<UIExtension> sections = this.uixManager.get("org.phenotips.patientSheet.content");
        sections = this.orderFilter.filter(sections, SORT_PARAMETER_NAME);
        for (UIExtension sectionExtension : sections) {
            RecordSection section = new DefaultRecordSection(sectionExtension, this.uixManager, this.orderFilter);
            result.add(section);
        }
        updatedConfig.setSections(result);
        return updatedConfig;
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
