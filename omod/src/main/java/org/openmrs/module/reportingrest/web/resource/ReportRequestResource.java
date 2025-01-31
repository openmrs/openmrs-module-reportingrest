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
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
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
import java.util.HashMap;
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
		return getReportService().getReportRequestByUuid(uuid);
	}


	@Override
	protected PageableResult doSearch(RequestContext context) {
		List<ReportRequest.Status> statuses = findAppropriateStatuses(context);
		ReportDefinition reportDefinition = findReportDefinition(context);

		ReportService reportService = getReportService();
		Integer pageNumber = context.getStartIndex();
		Integer pageSize = context.getLimit();
		List<ReportRequest> reportRequests =
				reportService.getReportRequests(reportDefinition, null, null, (pageNumber - 1) * pageSize, pageSize,
						statuses.toArray(new ReportRequest.Status[0]));
		long reportRequestsTotalCount =
				reportService.getReportRequestsCount(reportDefinition, null, null, statuses.toArray(new ReportRequest.Status[0]));

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

		return new AlreadyPaged<ReportRequest>(context, reportRequests, reportRequestsTotalCount > (long) pageNumber * pageSize, reportRequestsTotalCount);
	}

	private List<ReportRequest.Status> findAppropriateStatuses(RequestContext context) {
		List<ReportRequest.Status> reportRequestStatusList = new ArrayList<ReportRequest.Status>();
		String commaSeparatedStatusesParam = context.getParameter("status");
		if (StringUtils.isBlank(commaSeparatedStatusesParam)) {
			return reportRequestStatusList;
		}

		String[] splitStatuses = commaSeparatedStatusesParam.split(",");
		for (String status : splitStatuses) {
			String trimmed = status.trim().toUpperCase();
			reportRequestStatusList.add(ReportRequest.Status.valueOf(trimmed));
		}

		return reportRequestStatusList;
	}

	private ReportDefinition findReportDefinition(RequestContext context) {
		ReportDefinition reportDefinition = null;
		String reportDefinitionParam = context.getParameter("reportDefinition");
		if (reportDefinitionParam != null) {
			reportDefinition = DefinitionContext.getDefinitionByUuid(ReportDefinition.class, reportDefinitionParam);
		}

		return reportDefinition;
	}

	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceHandler#save(java.lang.Object)
	 */
	@Override
	public ReportRequest save(ReportRequest reportRequestParam) {
		ReportDefinition reportDefinition = Context.getService(ReportDefinitionService.class)
				.getDefinitionByUuid(reportRequestParam.getReportDefinition().getParameterizable().getUuid());

		ReportService reportService = getReportService();
		Map<String, Object> parameterValues = buildParametersMap(reportDefinition, reportRequestParam);
		RenderingMode renderingMode = findRenderingMode(reportDefinition, reportRequestParam, reportService);

		ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestParam.getUuid());
		if (reportRequest == null) {
			reportRequest = new ReportRequest();
		}

		reportRequest.setReportDefinition(new Mapped<ReportDefinition>(reportDefinition, parameterValues));
		reportRequest.setBaseCohort(reportRequestParam.getBaseCohort());
		reportRequest.setRenderingMode(renderingMode);
		reportRequest.setPriority(ReportRequest.Priority.NORMAL);
		reportRequest.setSchedule(reportRequestParam.getSchedule());

		reportService.queueReport(reportRequest);
		reportService.processNextQueuedReports();

		return reportRequest;
	}

	private Map<String, Object> buildParametersMap(ReportDefinition reportDefinition, ReportRequest reportRequest) {
		Map<String, Object> parameterValues = new HashMap<String, Object>();
		for (Parameter parameter : reportDefinition.getParameters()) {
			Object convertedObj =
					ConversionUtil.convert(reportRequest.getReportDefinition().getParameterMappings().get(parameter.getName()), parameter.getType());
			if (parameter.isRequired() && convertedObj == null) {
				throw new IllegalArgumentException("Parameter " + parameter.getName() + " is required");
			}
			parameterValues.put(parameter.getName(), convertedObj);
		}

		return parameterValues;
	}

	private RenderingMode findRenderingMode(ReportDefinition reportDefinition, ReportRequest reportRequest,
																					ReportService reportService) {
		List<RenderingMode> renderingModes = reportService.getRenderingModes(reportDefinition);
		RenderingMode renderingMode = null;
		for (RenderingMode mode : renderingModes) {
			if (StringUtils.equals(mode.getArgument(), reportRequest.getRenderingMode().getArgument())) {
				renderingMode = mode;
				break;
			}
		}

		return renderingMode;
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
			getReportService().purgeReportRequest(reportRequest);
		}
	}

	/**
	 * @see BaseDelegatingResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof CustomRepresentation) {
			return null;
		}

		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("uuid");
		description.addProperty("parameterizable", "reportDefinition.parameterizable",
				Representation.DEFAULT);
		description.addProperty("parameterMappings", "reportDefinition.parameterMappings",
				Representation.DEFAULT);
		description.addProperty("renderingMode");
		description.addProperty("priority");
		description.addProperty("schedule");
		description.addProperty("requestDate");
		description.addProperty("status");
		description.addProperty("evaluateStartDatetime");
		description.addProperty("evaluateCompleteDatetime");
		description.addProperty("renderCompleteDatetime");
		description.addProperty("description");
		description.addSelfLink();

		if (rep instanceof DefaultRepresentation) {
			description.addProperty("requestedBy", Representation.REF);
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
		} else if (rep instanceof FullRepresentation) {
			description.addProperty("requestedBy", Representation.DEFAULT);
		} else if (rep instanceof RefRepresentation) {
			description.addProperty("requestedBy", Representation.DEFAULT);
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
		Class<?> definitionClassType;
		try {
			if (propertyName.equals("reportDefinition")) {
				definitionClassType = ReportDefinition.class;
			} else if (propertyName.equals("baseCohort")) {
				definitionClassType = CohortDefinition.class;
			}  else if (propertyName.equals("renderingMode")) {
				Map<String, Object> renderingModeMap = (Map) value;
				String rendererUuid = (String) renderingModeMap.get("argument");
				RenderingMode rm = new RenderingMode();
				rm.setArgument(rendererUuid);
				PropertyUtils.setProperty(instance, propertyName, rm);
				return;
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
	private ReportService getReportService() {
		return Context.getService(ReportService.class);
	}
}
