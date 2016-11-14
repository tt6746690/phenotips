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
package org.phenotips.studies.family.internal;

import org.phenotips.components.ComponentManagerRegistry;
import org.phenotips.data.Patient;
import org.phenotips.data.PatientRepository;
import org.phenotips.data.internal.PhenoTipsPatient;
import org.phenotips.studies.family.Family;
import org.phenotips.studies.family.Pedigree;
import org.phenotips.studies.family.internal.export.PhenotipsFamilyExport;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.StringProperty;

/**
 * XWiki implementation of Family.
 *
 * @version $Id$
 */
public class PhenotipsFamily implements Family
{
    /** Field name in the family document which holds the list of member patients. */
    public static final String FAMILY_MEMBERS_FIELD = "members";

    private static final String WARNING = "warning";

    private static PatientRepository patientRepository;

    private static PhenotipsFamilyExport familyExport;

    /** Logging helper object. */
    private Logger logger = LoggerFactory.getLogger(PhenoTipsPatient.class);

    private XWikiDocument familyDocument;

    static {
        try {
            PhenotipsFamily.patientRepository =
                ComponentManagerRegistry.getContextComponentManager().getInstance(PatientRepository.class);
            PhenotipsFamily.familyExport =
                ComponentManagerRegistry.getContextComponentManager().getInstance(PhenotipsFamilyExport.class);
        } catch (ComponentLookupException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param familyDocument not-null document associated with the family
     */
    public PhenotipsFamily(XWikiDocument familyDocument)
    {
        this.familyDocument = familyDocument;
    }

    @Override
    public String getId()
    {
        return this.familyDocument.getDocumentReference().getName();
    }

    @Override
    public XWikiDocument getDocument()
    {
        return this.familyDocument;
    }

    @Override
    public DocumentReference getDocumentReference()
    {
        return this.familyDocument.getDocumentReference();
    }

    @Override
    public List<String> getMembersIds()
    {
        BaseObject familyObject = this.familyDocument.getXObject(CLASS_REFERENCE);
        if (familyObject == null) {
            return new LinkedList<>();
        }

        ListProperty xwikiRelativesList;
        try {
            xwikiRelativesList = (ListProperty) familyObject.get(FAMILY_MEMBERS_FIELD);
        } catch (XWikiException e) {
            this.logger.error("error reading family members: {}", e);
            return null;
        }
        if (xwikiRelativesList == null) {
            return Collections.emptyList();
        }
        return xwikiRelativesList.getList();
    }

    @Override
    public List<Patient> getMembers()
    {
        List<String> memberIds = this.getMembersIds();
        List<Patient> members = new ArrayList<>(memberIds.size());
        for (String memberId : memberIds) {
            Patient patient = PhenotipsFamily.patientRepository.get(memberId);
            members.add(patient);
        }
        return members;
    }

    @Override
    public String getProbandId()
    {
        Pedigree pedigree = this.getPedigree();
        if (pedigree == null) {
            return null;
        }
        return pedigree.getProbandId();
    }

    @Override
    public boolean isMember(Patient patient)
    {
        List<String> members = getMembersIds();
        if (members == null) {
            return false;
        }
        String patientId = patient.getId();
        return members.contains(patientId);
    }

    @Override
    public JSONObject toJSON()
    {
        return PhenotipsFamily.familyExport.toJSON(this);
    }

    @Override
    public Map<String, Map<String, String>> getMedicalReports()
    {
        Map<String, Map<String, String>> allFamilyLinks = new HashMap<>();

        for (Patient patient : getMembers()) {
            allFamilyLinks.put(patient.getId(), PhenotipsFamily.familyExport.getMedicalReports(patient));
        }
        return allFamilyLinks;
    }

    @Override
    public String getExternalId()
    {
        BaseObject familyObject = this.familyDocument.getXObject(Family.CLASS_REFERENCE);
        StringProperty externalId = null;
        String externalIdString = "";
        try {
            externalId = (StringProperty) familyObject.get("external_id");
            if (externalId != null) {
                externalIdString = externalId.getValue();
            }
        } catch (XWikiException e) {
            this.logger.error("Error reading external id of family [{}]: [{}]", getId(), e.getMessage());
        }
        return externalIdString;
    }

    @Override
    public String getURL(String actions)
    {
        return this.familyDocument.getURL(actions, getXContext());
    }

    private XWikiContext getXContext()
    {
        Execution execution = null;
        try {
            execution = ComponentManagerRegistry.getContextComponentManager().getInstance(Execution.class);
        } catch (ComponentLookupException ex) {
            // Should not happen
            return null;
        }
        XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwikicontext");
        return context;
    }

    /*
     * Some pedigrees may contain sensitive information, which should be displayed on every edit of the pedigree. The
     * function returns a warning to display, or empty string
     */
    @Override
    public String getWarningMessage()
    {
        BaseObject familyObject = this.familyDocument.getXObject(Family.CLASS_REFERENCE);
        if (familyObject.getIntValue(WARNING) == 0) {
            return "";
        } else {
            return familyObject.getStringValue("warning_message");
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PhenotipsFamily)) {
            return false;
        }

        PhenotipsFamily xobj = (PhenotipsFamily) obj;
        return (this.getId().equals(xobj.getId()));
    }

    @Override
    public int hashCode()
    {
        return this.getId().hashCode();
    }

    @Override
    public Pedigree getPedigree()
    {
        BaseObject pedigreeObj = this.familyDocument.getXObject(Pedigree.CLASS_REFERENCE);
        if (pedigreeObj != null) {
            BaseStringProperty data = null;
            BaseStringProperty image = null;

            try {
                data = (BaseStringProperty) pedigreeObj.get(Pedigree.DATA);
                image = (BaseStringProperty) pedigreeObj.get(Pedigree.IMAGE);

                if (StringUtils.isNotBlank(data.toText())) {
                    return new DefaultPedigree(new JSONObject(data.toText()), image.toText());
                }
            } catch (XWikiException e) {
                this.logger.error("Error reading data from pedigree. {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                this.logger.error("Incorrect pedigree data. {}", e.getMessage());
            }
        }
        return null;
    }
}
