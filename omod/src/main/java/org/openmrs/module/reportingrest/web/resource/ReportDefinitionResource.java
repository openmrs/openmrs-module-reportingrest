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

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Resource} for {@link ReportDefinition}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportDefinition",
        supportedClass = ReportDefinition.class, supportedOpenmrsVersions = {"1.8.* - 9.9.*"})
public class ReportDefinitionResource extends BaseDefinitionResource<ReportDefinition> {
	
	/**
	 * @return the definition type that this resource wraps
	 */
	public Class<ReportDefinition> getDefinitionType() {
		return ReportDefinition.class;
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		if (description != null) {
			description.addProperty("dataSetDefinitions");
			description.addProperty("reportDesigns");
		}
		return description;
	}

	@PropertyGetter("dataSetDefinitions")
	public Object getDataSetDefinitions(ReportDefinition delegate) {
		List<SimpleObject> result = new ArrayList<>();
		for (String key : delegate.getDataSetDefinitions().keySet()) {
			SimpleObject mappedDsd = new SimpleObject();
			mappedDsd.put("key", key);
			mappedDsd.put("display", Context.getMessageSourceService().getMessage(key));
			mappedDsd.put("value", delegate.getDataSetDefinitions().get(key));
			result.add(mappedDsd);
		}
		return result;
	}

	@PropertyGetter("reportDesigns")
	public Object getReportDesigns(ReportDefinition delegate) {
		return Context.getService(ReportService.class).getReportDesigns(delegate, null, false);
	}
}
