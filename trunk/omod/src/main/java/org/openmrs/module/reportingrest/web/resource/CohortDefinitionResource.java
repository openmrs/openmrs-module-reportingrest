/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reportingrest.web.resource;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

/**
 * {@link Resource} for {@link CohortDefinition}s, supporting standard CRUD operations
 */
@Resource("cohortDefinition")
@Handler(supports = CohortDefinition.class, order = 1)
public class CohortDefinitionResource extends BaseDefinitionResource<CohortDefinition> {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public Class<CohortDefinition> getDefinitionType() {
		return CohortDefinition.class;
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		// TODO, add link to evaluated cohort url
		// TODO, property for list of available params (see parent class)
		
		return super.getRepresentationDescription(rep); 
	}
}
