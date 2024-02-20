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

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequestDTO;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


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
		String statusesGroup = context.getParameter("statusesGroup");
		if (StringUtils.isNotBlank(statusesGroup)) {
			List<ReportRequest.Status> statuses = new ArrayList<ReportRequest.Status>();
			if (StringUtils.equalsIgnoreCase(statusesGroup, "ran")) {
				Collections.addAll(
						statuses,
						ReportRequest.Status.REQUESTED,
						ReportRequest.Status.COMPLETED,
						ReportRequest.Status.SAVED,
						ReportRequest.Status.FAILED,
						ReportRequest.Status.PROCESSING);
			} else if (StringUtils.equalsIgnoreCase(statusesGroup, "processing")) {
				Collections.addAll(statuses, ReportRequest.Status.REQUESTED, ReportRequest.Status.PROCESSING);
			} else if (StringUtils.equalsIgnoreCase(statusesGroup, "scheduled")) {
				Collections.addAll(
						statuses,
						ReportRequest.Status.SCHEDULED,
						ReportRequest.Status.SCHEDULE_COMPLETED);
			}
			ReportService reportService = getService();
			Integer pageNumber = context.getStartIndex();
			Integer pageSize = context.getLimit();
			ReportRequestDTO reportRequestsDTO = reportService.getReportsWithPagination(
					null, null, null, pageNumber, pageSize, statuses.toArray(new ReportRequest.Status[0]));
			List<ReportRequest> reportRequests = reportRequestsDTO.getReportRequests();

			String sortByParameter = context.getParameter("sortBy");
			if (StringUtils.isBlank(sortByParameter) || "priority".equals(sortByParameter)) {
				Collections.sort(reportRequests, new ReportRequest.PriorityComparator());
				Collections.reverse(reportRequests);
			} else if("name".equals(sortByParameter)) {
				Collections.sort(reportRequests, new Comparator<ReportRequest>() {
					@Override
					public int compare(ReportRequest left, ReportRequest right) {
						return left
								.getReportDefinition()
								.getParameterizable()
								.getName()
								.compareTo(right.getReportDefinition().getParameterizable().getName());
					}
				});
			}

			for (ReportRequest reportRequest : reportRequests) {
				for (RenderingMode mode :
						reportService.getRenderingModes(
								reportRequest.getReportDefinition().getParameterizable())) {
					if (OpenmrsUtil.nullSafeEquals(mode, reportRequest.getRenderingMode())) {
						reportRequest.setRenderingMode(mode);
					}
				}
			}

			return new AlreadyPaged<ReportRequest>(context, reportRequests, reportRequestsDTO.getReportRequestCount() > (long) pageNumber * pageSize, reportRequestsDTO.getReportRequestCount());
		}

		String reportDefinitionParam = context.getParameter("reportDefinition");
		if (StringUtils.isEmpty(reportDefinitionParam)) {
			throw new IllegalArgumentException("reportDefinition is required");
		}

		ReportDefinition reportDefinition = DefinitionContext.getDefinitionByUuid(ReportDefinition.class, reportDefinitionParam);
		if (reportDefinition == null) {
			throw new NullPointerException("Cannot find reportDefinition=" + reportDefinitionParam);
		}

		List<ReportRequest> reportRequests = getService().getReportRequests(reportDefinition, null, null);

		return new AlreadyPaged<ReportRequest>(context, reportRequests, false);
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
	public Model getCREATEModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep));
		modelImpl.property("status", new StringProperty())
				.property("reportDefinition", new StringProperty())
				.property("baseCohort", new StringProperty())
				.property("renderingMode", new StringProperty())
				.property("priority", new StringProperty())
				.property("schedule", new StringProperty());
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
			description.addProperty("parameterizable", "reportDefinition.parameterizable",
					Representation.DEFAULT);
			description.addProperty("parameterMappings", "reportDefinition.parameterMappings",
					Representation.DEFAULT);
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
		} else if (rep instanceof FullRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			//description.addProperty("baseCohort", Representation.DEFAULT);  TODO: Figure out how to support this
			description.addProperty("parameterizable", "reportDefinition.parameterizable",
					Representation.DEFAULT);
			description.addProperty("parameterMappings", "reportDefinition.parameterMappings",
					Representation.DEFAULT);
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
		} else if (rep instanceof RefRepresentation) {
			description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			//description.addProperty("baseCohort", Representation.DEFAULT);  TODO: Figure out how to support this
			description.addProperty("parameterizable", "reportDefinition.parameterizable",
					Representation.DEFAULT);
			description.addProperty("parameterMappings", "reportDefinition.parameterMappings",
					Representation.DEFAULT);
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
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		}
		return description;
	}

	@Override
	public Model getGETModel(Representation rep) {
		ModelImpl modelImpl = ((ModelImpl) super.getGETModel(rep));
		if (rep instanceof DefaultRepresentation) {
			modelImpl.property("uuid", new StringProperty())
					.property("renderingMode", new StringProperty())
					.property("priority", new StringProperty())
					.property("schedule", new StringProperty())
					.property("requestedBy", new StringProperty())
					.property("requestDate", new DateProperty())
					.property("status", new StringProperty())
					.property("evaluateStartDatetime", new DateProperty())
					.property("evaluateCompleteDatetime", new DateProperty())
					.property("renderCompleteDatetime", new DateProperty())
					.property("description", new StringProperty());
		}
		if (rep instanceof FullRepresentation) {
			modelImpl.property("uuid", new StringProperty())
					.property("renderingMode", new StringProperty())
					.property("priority", new StringProperty())
					.property("schedule", new StringProperty())
					.property("requestedBy", new StringProperty())
					.property("requestDate", new DateProperty())
					.property("status", new StringProperty())
					.property("evaluateStartDatetime", new DateProperty())
					.property("evaluateCompleteDatetime", new DateProperty())
					.property("renderCompleteDatetime", new DateProperty())
					.property("description", new StringProperty());
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
