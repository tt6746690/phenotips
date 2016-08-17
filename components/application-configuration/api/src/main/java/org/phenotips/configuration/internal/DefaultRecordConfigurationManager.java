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

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationManager;
import org.phenotips.configuration.RecordConfigurationModule;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;

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
    /** Logging helper. */
    @Inject
    private Logger logger;

    @Inject
    @Named("wiki")
    private ComponentManager cm;

    public RecordConfiguration getConfiguration(String recordType)
    {
        RecordConfiguration config = null;
        for (RecordConfigurationModule service: get()) {
    	    try {
    		    config = service.process(config);
    		    //what gets called
    		    this.logger.debug(service.getClass().toString());
    	    } catch (Exception ex) {
    		    this.logger.warn("Failed to read the record configuration: {}", ex.getMessage());
    		}    		
    	}
        return config;
    }

    public List<RecordConfigurationModule> get()
    {
        try {
    	    List<RecordConfigurationModule> modules = new LinkedList<>();
    	    modules.addAll(this.cm.<RecordConfigurationModule>getInstanceList(RecordConfigurationModule.class));
    	    Collections.sort(modules, RecordSectionComparator.INSTANCE);
    	    //Loop through each module
    	    for(RecordConfigurationModule module : modules)
    	    	this.logger.error(module.getClass().toString());
    	    return modules;
        } catch (ComponentLookupException ex) {
    	    this.logger.warn("Failed to create the list: {}", ex.getMessage());
        }
        return Collections.emptyList();
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
    	return getConfiguration("");
    }
}
