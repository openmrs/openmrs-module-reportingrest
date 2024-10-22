/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportingrest.web.resource;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

/**
 * {@link Resource} for {@link ReportRequest}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportRequest",
        supportedClass = ReportRequest.class, supportedOpenmrsVersions = {"1.8.* - 9.9.*"})
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
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		String reportDefinitionParam = context.getParameter("reportDefinition");
		if (StringUtils.isEmpty(reportDefinitionParam)) {
			throw new IllegalArgumentException("reportDefinition is required");
		}
		ReportDefinition reportDefinition = DefinitionContext.getDefinitionByUuid(ReportDefinition.class, reportDefinitionParam);
		if (reportDefinition == null) {
			throw new NullPointerException("Cannot find reportDefinition=" + reportDefinitionParam);
		}
		List<ReportRequest> reportRequests = getService().getReportRequests(reportDefinition, null, null);
		return new NeedsPaging<ReportRequest>(reportRequests, context);
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

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription delegatingResourceDescription = new DelegatingResourceDescription();
		delegatingResourceDescription.addProperty("status");
		delegatingResourceDescription.addProperty("reportDefinition");
		delegatingResourceDescription.addProperty("baseCohort");
		delegatingResourceDescription.addProperty("renderingMode");
		delegatingResourceDescription.addProperty("priority");
		delegatingResourceDescription.addProperty("schedule");
		return delegatingResourceDescription;
	}

	@Override
	public Schema<?> getCREATESchema(Representation rep) {
		Schema<?> modelImpl = super.getGETSchema(rep);
		modelImpl.addProperty("status", new StringSchema())
				.addProperty("reportDefinition", new StringSchema())
				.addProperty("baseCohort", new StringSchema())
				.addProperty("renderingMode", new StringSchema())
				.addProperty("priority", new StringSchema())
				.addProperty("schedule", new StringSchema());
		return modelImpl;
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
			description.addProperty("schedule");
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
			description.addProperty("schedule");
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

	@Override
	public Schema<?> getGETSchema(Representation rep) {
		Schema<?> modelImpl = super.getGETSchema(rep);
		if (rep instanceof DefaultRepresentation) {
			modelImpl.addProperty("uuid", new StringSchema())
					.addProperty("renderingMode", new StringSchema())
					.addProperty("priority", new StringSchema())
					.addProperty("schedule", new StringSchema())
					.addProperty("requestedBy", new StringSchema())
					.addProperty("requestDate", new DateTimeSchema())
					.addProperty("status", new StringSchema())
					.addProperty("evaluateStartDatetime", new DateTimeSchema())
					.addProperty("evaluateCompleteDatetime", new DateTimeSchema())
					.addProperty("renderCompleteDatetime", new DateTimeSchema())
					.addProperty("description", new StringSchema());
		}
		if (rep instanceof FullRepresentation) {
			modelImpl.addProperty("uuid", new StringSchema())
					.addProperty("renderingMode", new StringSchema())
					.addProperty("priority", new StringSchema())
					.addProperty("schedule", new StringSchema())
					.addProperty("requestedBy", new StringSchema())
					.addProperty("requestDate", new DateTimeSchema())
					.addProperty("status", new StringSchema())
					.addProperty("evaluateStartDatetime", new DateTimeSchema())
					.addProperty("evaluateCompleteDatetime", new DateTimeSchema())
					.addProperty("renderCompleteDatetime", new DateTimeSchema())
					.addProperty("description", new StringSchema());
		}
		return modelImpl;
	}

	/**
	 * @see BaseDelegatingResource#setProperty(Object, String, Object)
	 */
	@Override
	public void setProperty(Object instance, String propertyName, Object value) throws ConversionException {
		Class<?> definitionClassType = null;
		try {
			if (propertyName.equals("reportDefinition")) {
				definitionClassType = ReportDefinition.class;
			} else if (propertyName.equals("baseCohort")) {
				definitionClassType = CohortDefinition.class;
			} else {
				super.setProperty(instance, propertyName, value);
				return;
			}

			Map parametrizableMap = (Map) ((Map) value).get("parameterizable");
			Map<String, Object> parameterMappings = (Map) ((Map) value).get("parameterMappings");

			if (parametrizableMap == null) {
				throw new ConversionException("Missing parameterizable");
			}

			Parameterizable parameterizable = (Parameterizable) ConversionUtil.convert(parametrizableMap.get("uuid"), definitionClassType);
			Mapped mappedInstance = new Mapped(parameterizable, parameterMappings);
			PropertyUtils.setProperty(instance, propertyName, mappedInstance);
		} catch (Exception ex) {
			throw new ConversionException(propertyName, ex);
		}
	}

	/**
	 * @return the ReportService
	 */
	private ReportService getService() {
		return Context.getService(ReportService.class);
	}
}
