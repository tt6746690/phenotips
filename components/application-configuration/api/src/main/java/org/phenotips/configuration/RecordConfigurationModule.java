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
package org.phenotips.configuration;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

@Unstable
@Role
public interface RecordConfigurationModule {
	
	/**
	 * Configures how the patient record appears.
	 * 
	 * @param documents the previous configuration
	 * @return {@link RecordConfiguration} Patient record is configured to the user's preference
	 */
    RecordConfiguration process(RecordConfiguration config);
    
    /**
     * @return a positive number
     */
    int getPriority();
    
    /**
     * @return the record type.
     */
    String[] getSupportedRecordTypes();
}
