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
package org.phenotips.projects.internal;

import org.phenotips.data.permissions.Collaborator;
import org.phenotips.entities.PrimaryEntityManager;
import org.phenotips.entities.internal.AbstractPrimaryEntityManager;
import org.phenotips.projects.access.ProjectAccessLevel;
import org.phenotips.projects.data.Project;
import org.phenotips.projects.data.ProjectRepository;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
@Component(roles = {PrimaryEntityManager.class, ProjectRepository.class})
@Named("Project")
@Singleton
public class DefaultProjectRepository
    extends AbstractPrimaryEntityManager<Project> implements ProjectRepository
{
    @Inject
    private UserManager userManager;

    @Inject
    @Named("leader")
    private ProjectAccessLevel leaderAccessLevel;

    @Inject
    @Named("contributor")
    private ProjectAccessLevel contributorAccessLevel;

    @Override
    public Collection<Project> getAll(Collection<ProjectAccessLevel> accessLevels)
    {
        Collection<Project> projects = new LinkedList<>();
        User currentUser = this.userManager.getCurrentUser();

        Iterator<Project> projectsIterator = this.getAll();
        while (projectsIterator.hasNext()) {
            Project p = projectsIterator.next();

            Collection<Collaborator> collaborators = p.getCollaborators();
            for (Collaborator collaborator : collaborators) {
                if (accessLevels.contains(collaborator.getAccessLevel())
                    && collaborator.isUserIncluded(currentUser)) {
                    projects.add(p);
                    break;
                }
            }
        }

        return projects;
    }

    @Override
    public Collection<Project> getProjectsCurrentUserCanContributeTo()
    {
        Set<ProjectAccessLevel> accessLevels = new HashSet<>();
        accessLevels.add(contributorAccessLevel);
        accessLevels.add(leaderAccessLevel);
        Collection<Project> projects = this.getAll(accessLevels);

        for (Project p : this.getAllProjectsOpenForContribution()) {
            if (!projects.contains(p)) {
                projects.add(p);
            }
        }
        return projects;
    }

    @Override
    public Collection<Project> getProjectsWithLeadingRights()
    {
        Set<ProjectAccessLevel> accessLevels = new HashSet<>();
        accessLevels.add(leaderAccessLevel);
        return this.getAll(accessLevels);
    }

    @Override
    public Collection<Project> getAllProjectsOpenForContribution()
    {
        Collection<Project> projects = new LinkedList<>();

        Iterator<Project> projectIterator = this.getAll();
        while (projectIterator.hasNext()) {
            Project project = projectIterator.next();
            if (project.isProjectOpenForContribution()) {
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public Collection<Project> getFromString(String projectsString)
    {
        Collection<Project> projects = new LinkedList<>();
        if (StringUtils.isEmpty(projectsString)) {
            return projects;
        }

        for (String projectId : projectsString.split(",")) {
            Project project = this.get(projectId);
            if (project != null) {
                projects.add(project);
            }
        }
        return projects;
    }

    @Override
    public EntityReference getDataSpace()
    {
        return Project.DEFAULT_DATA_SPACE;
    }

    @Override
    protected Class<? extends Project> getEntityClass()
    {
        return DefaultProject.class;
    }
}
