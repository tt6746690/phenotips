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
package org.phenotips.entities;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.stability.Unstable;

import org.json.JSONObject;

/**
 * An XDocument containing a primary XObject, which gives it a special meaning, such as a User, a Patient Record, or a
 * Project.
 *
 * @version $Id$
 * @since 1.3M2
 */
@Unstable("New API introduced in 1.3")
public interface PrimaryEntity
{
    /** The key used in the JSON serialization for the {@link #getId() identifier}. */
    String JSON_KEY_ID = "id";

    /** The key used in the JSON serialization for the {@link #getName() name}. */
    String JSON_KEY_NAME = "name";

    /** The key used in the JSON serialization for the {@link #getDescription() description}. */
    String JSON_KEY_DESCRIPTION = "description";

    /**
     * @return a reference to the main XClass associated with this entity
     */
    @Unstable("A ClassReference should be used instead of EntityReference")
    EntityReference getType();

    /**
     * Returns a reference to the document where the entity is stored.
     *
     * @return a valid document reference
     */
    DocumentReference getDocumentReference();

    // TODO:
    // 1. rename getDocument()->getDocumentReference()
    // 2. add getDocument() to return the xDocument
    // 3. change every call to old getDocument() that used DocumentReference to obtain xDocument.

    /**
     * Returns the internal identifier of the entity.
     *
     * @return a short identifier
     */
    String getId();

    /**
     * Returns a user-friendly name for the entity, if any.
     *
     * @return a string, may be the same as {@link #getId() the identifier} if no other name is used for this type of
     *         entity
     */
    String getName();

    /**
     * Returns the full name of the entity.
     *
     * @return a string
     */
    String getFullName();

    /**
     * Returns a longer, user-friendly description for the entity, if any.
     *
     * @return a string, may be empty
     */
    String getDescription();

    /**
     * Serializes this entity in a JSON format.
     *
     * @return the serialized entity, using the org.json classes
     */
    JSONObject toJSON();

    /**
     * Updates the entity using the provided data (in JSON format). Properties present in the JSON will have its values
     * updated. Properties that aren't in the JSON will be left as is.
     *
     * @param json JSON object containing the new entity data, in the same format as generated by the {@link #toJSON()}
     *            method
     */
    void updateFromJSON(JSONObject json);
}
