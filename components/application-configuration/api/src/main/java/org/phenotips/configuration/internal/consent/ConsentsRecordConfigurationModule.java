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
package org.phenotips.configuration.internal.consent;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.phenotips.configuration.RecordConfiguration;
import org.phenotips.configuration.RecordConfigurationModule;
import org.phenotips.configuration.RecordElement;
import org.phenotips.configuration.RecordSection;
import org.phenotips.configuration.internal.DefaultRecordConfiguration;
import org.phenotips.configuration.internal.configured.CustomConfiguration;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.uiextension.UIExtensionFilter;
import org.xwiki.uiextension.UIExtensionManager;

import com.xpn.xwiki.XWikiContext;

/**
 * Implementation of {@link RecordConfiguration} that takes into account a {@link DefaultConsentAuthorizer custom
 * configuration}.
 *
 * @version $Id$
 * @since 1.3
 */
@Named("Consents")
public class ConsentsRecordConfigurationModule extends DefaultConsentAuthorizer implements RecordConfigurationModule 
{
	@Inject
	private PatientRepository patients;
	
	@Inject
	private DocumentAccessBridge dab;
	
    /** Provides access to the current request context. */
    protected Provider<XWikiContext> xcontextProvider;

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
    	Patient patient = this.patients.getPatientById(dab.getCurrentDocumentReference().toString());
    	if (patient == null) {
    		return config;
    	}
        RecordConfiguration updatedConfigs = new DefaultRecordConfiguration();
        List<RecordElement> elementList = new LinkedList<>();
        List<RecordSection> sectionList = new LinkedList<>();  	
        
        if (config == null) {
        	return config;
        }
    	    
	    for (RecordSection section : config.getAllSections()) {
		    // Filters elements by consents
		    elementList = filterForm(section.getAllElements(), patient);
            if (section.isEnabled()) {
		        section.setElements(elementList);
		        sectionList.add(section);
		    }
        }
        updatedConfigs.setSections(sectionList);
		
        return updatedConfigs;
    }

    @Override
    public int getPriority() 
    {
	    return 300;
    }

    @Override
    public String[] getSupportedRecordTypes()
    {
        String[] recType = {"patient"};
	    return recType;
	}

}
