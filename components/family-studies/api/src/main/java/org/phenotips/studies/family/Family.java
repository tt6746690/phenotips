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
package org.phenotips.studies.family;

import org.phenotips.Constants;
import org.phenotips.data.Patient;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * TODO: refactor on top of Entities model. Split/re-arrange functionality between Family (Entity), FamilyRepository
 * (EntityManager) and EntityGroupManager.
 *
 * @version $Id$
 * @since 1.4
 */
public interface Family
{
    /** The XClass used for storing family data. */
    EntityReference CLASS_REFERENCE = new EntityReference("FamilyClass", EntityType.DOCUMENT,
        Constants.CODE_SPACE_REFERENCE);

    /** The space where family data is stored. */
    EntityReference DATA_SPACE = new EntityReference("Families", EntityType.SPACE);

    /**
     * @return family id
     */
    String getId();

    /**
     * @return the copy o family document held in memory by this Family instance.
     */
    XWikiDocument getXDocument();

    /**
     * @return reference to family document
     */
    DocumentReference getDocumentReference();

    /**
     * @return list of family members
     */
    List<Patient> getMembers();

    /**
     * @return list of family members ids
     */
    List<String> getMembersIds();

    /**
     * @return phenotipsID of the patient which is the proband of the family, or null if pedigree has no proband or
     *         proband node is not linked to a patient record
     */
    String getProbandId();

    /**
     * @param patient check if the patient belongs to this family
     * @return true if a patient is a member of the family
     */
    boolean isMember(Patient patient);

    /**
     * Generates a JSON data structure that describes the family and its members.
     *
     * @return JSON with info about the family, each member and the current user's permissions.
     */
    JSONObject toJSON();

    /**
     * Retrieves medical reports for all family members.
     *
     * @return patient ids mapped to medical reports, which in turn are maps of report name to its link
     */
    Map<String, Map<String, String>> getMedicalReports();

    /**
     * @return external id. Returns "" if id is not defined.
     */
    String getExternalId();

    /**
     * @param action to get URL for
     * @return URL
     */
    String getURL(String action);

    /**
     * Some pedigrees may contain sensitive information, which should be displayed on every edit of the pedigree. The
     * function returns a warning to display, or empty string
     *
     * @return warning message
     */
    String getWarningMessage();

    /**
     * Returns the pedigree associated with the family.
     *
     * @return Pedigree associated with the family.
     */
    Pedigree getPedigree();
}
