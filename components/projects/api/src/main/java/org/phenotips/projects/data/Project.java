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
package org.phenotips.projects.data;

import org.phenotips.Constants;
import org.phenotips.data.Patient;
import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.Collaborator;
import org.phenotips.entities.PrimaryEntity;
import org.phenotips.entities.PrimaryEntityGroup;
import org.phenotips.templates.data.Template;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;

import java.util.Collection;

/**
 * @version $Id$
 */
public interface Project extends Comparable<Project>, PrimaryEntityGroup<PrimaryEntity>
{
    /** The XClass used for storing project data. */
    EntityReference CLASS_REFERENCE = new EntityReference("ProjectClass", EntityType.DOCUMENT,
        Constants.CODE_SPACE_REFERENCE);

    /** The default space where patient data is stored. */
    EntityReference DEFAULT_DATA_SPACE = new EntityReference("Projects", EntityType.SPACE);

    /** project template document. */
    EntityReference TEMPLATE =
        new EntityReference("ProjectTemplate", EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    /**
     * Returns the full name of project.
     *
     * @return the full name of the project
     */
    String getFullName();

    /**
     * Returns the logo image link of the project.
     *
     * @return the image URL of the project
     */
    String getImage();

    /**
     * Returns the number of users collaborating in the project. That is, number of contributor and leader users plus
     * the number of all the users in contributor and leader groups.
     *
     * @return total number of users who are collaborators in the project
     */
    int getNumberOfCollaboratorsUsers();

    /**
     * @return a collection of all patients who are assigned to the project.
     */
    Collection<Patient> getAllPatients();

    /**
     * Returns the number of patients that are assigned to the project.
     *
     * @return number of patients that are assigned to the project.
     */
    int getNumberOfPatients();

    /**
     * Returns a collection of project collaborators: leaders, contributors and observers together.
     *
     * @return a collection of collaborators
     */
    Collection<Collaborator> getCollaborators();

    /**
     * Sets the list of project collaborators.
     *
     * @param observers collection of observers
     * @param contributors collection of contributors
     * @param leaders collection of project leaders
     * @return true if successful
     */
    boolean setCollaborators(Collection<EntityReference> observers, Collection<EntityReference> contributors,
        Collection<EntityReference> leaders);

    /**
     * Sets the list of project collaborators.
     *
     * @param collaborators collection of contributors
     * @return true if successful
     */
    boolean setCollaborators(Collection<Collaborator> collaborators);

    /**
     * Returns a collection templates available for the project.
     *
     * @return a collection of templates
     */
    Collection<Template> getTemplates();

    /**
     * Sets the list of templates available for the project.
     *
     * @param templateIds collection of template ids
     * @return true if successful
     */
    boolean setTemplates(Collection<String> templateIds);

    /**
     * Returns the highest access level the current user has.
     *
     * @return highest access level of current user.
     */
    AccessLevel getCurrentUserAccessLevel();

    /**
     * Return true if the project is open for contribution by all users.
     *
     * @return true if the project is open for contribution by all users.
     */
    boolean isProjectOpenForContribution();
}
