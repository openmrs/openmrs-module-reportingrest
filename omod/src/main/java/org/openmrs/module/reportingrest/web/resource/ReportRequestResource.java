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
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

/**
 * {@link Resource} for {@link ReportRequest}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + "/reportingrest/reportRequest", supportedClass = ReportRequest.class, supportedOpenmrsVersions = {"1.8.*", "1.9.*"})
public class ReportRequestResource extends DelegatingCrudResource<ReportRequest> {

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#newDelegate()
	 */
	@Override
    public ReportRequest newDelegate() {
		return new ReportRequest();
	}

	/**
	 * @see BaseDelegatingResource#getByUniqueId(String)
	 */
	@Override
	public ReportRequest getByUniqueId(String uuid) {
		return getService().getReportRequestByUuid(uuid);
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
    public ReportRequest save(ReportRequest reportRequest) {
		return getService().saveReportRequest(reportRequest);
	}

	/**
	 * @see BaseDelegatingResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(ReportRequest reportRequest, String reason, RequestContext context) throws ResponseException {
		purge(reportRequest, context);
	}

	/**
	 * @see BaseDelegatingResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(ReportRequest reportRequest, RequestContext context) throws ResponseException {
		if (reportRequest != null) {
			getService().purgeReportRequest(reportRequest);
		}
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = null;
		if (rep instanceof DefaultRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			//description.addProperty("baseCohort", Representation.DEFAULT);  TODO: Figure out how to support this
			//description.addProperty("reportDefinition", Representation.DEFAULT);  TODO: Figure out how to support this
			description.addProperty("renderingMode");
			description.addProperty("priority");
			description.addProperty("requestedBy", Representation.REF);
			description.addProperty("requestDate");
			description.addProperty("status");
			description.addProperty("evaluateStartDatetime");
			description.addProperty("evaluateCompleteDatetime");
			description.addProperty("renderCompleteDatetime");
			description.addProperty("description");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		}
		else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			//description.addProperty("baseCohort", Representation.DEFAULT);  TODO: Figure out how to support this
			//description.addProperty("reportDefinition", Representation.DEFAULT);  TODO: Figure out how to support this
			description.addProperty("renderingMode");
			description.addProperty("priority");
			description.addProperty("requestedBy", Representation.DEFAULT);
			description.addProperty("requestDate");
			description.addProperty("status");
			description.addProperty("evaluateStartDatetime");
			description.addProperty("evaluateCompleteDatetime");
			description.addProperty("renderCompleteDatetime");
			description.addProperty("description");
			description.addSelfLink();
		}
		return description;
	}

	/**
	 * @return the ReportService
	 */
	private ReportService getService() {
		return Context.getService(ReportService.class);
	}
}
