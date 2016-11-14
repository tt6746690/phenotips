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
package org.phenotips.projects.groupManagers;

import org.phenotips.data.Patient;
import org.phenotips.entities.PrimaryEntityProperty;
import org.phenotips.entities.internal.AbstractPrimaryEntityProperty;
import org.phenotips.templates.data.Template;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.DefaultParameterizedType;

import java.lang.reflect.ParameterizedType;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @version $Id$
 */
@Component
@Named("Patient:Template")
@Singleton
public class TemplateInPatientManager
    extends AbstractPrimaryEntityProperty<Patient, Template>
    implements PrimaryEntityProperty<Patient, Template>
{
    /** Type instance for lookup. */
    public static final ParameterizedType TYPE = new DefaultParameterizedType(null, PrimaryEntityProperty.class,
            Patient.class, Template.class);

    /**
     * Public constructor.
     */
    public TemplateInPatientManager()
    {
        super(Patient.CLASS_REFERENCE, Template.CLASS_REFERENCE);
    }
}
