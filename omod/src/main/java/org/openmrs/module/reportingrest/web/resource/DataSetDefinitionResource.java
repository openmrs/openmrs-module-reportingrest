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
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

/**
 * {@link Resource} for {@link DataSetDefinition}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/dataSetDefinition", supportedClass = DataSetDefinition.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class DataSetDefinitionResource extends BaseDefinitionResource<DataSetDefinition> {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public Class<DataSetDefinition> getDefinitionType() {
		return DataSetDefinition.class;
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		return super.getRepresentationDescription(rep);
	}
	
	
}
