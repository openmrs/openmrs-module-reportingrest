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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import javax.persistence.EntityNotFoundException;
import java.util.List;

/**
 * {@link Resource} for {@link ReportDesign}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/reportDesign",
    supportedClass = ReportDesign.class, supportedOpenmrsVersions = {"1.8.* - 9.9.*"})
public class ReportDesignResource extends DelegatingCrudResource<ReportDesign> {

  @Override
  public ReportDesign newDelegate() {
    return new ReportDesign();
  }

  @Override
  public ReportDesign getByUniqueId(String uuid) {
    return getReportService().getReportDesignByUuid(uuid);
  }

  @Override
  protected PageableResult doSearch(RequestContext context) {
    String reportDefinitionUuid = context.getParameter("reportDefinitionUuid");
    if (StringUtils.isBlank(reportDefinitionUuid)) {
      throw new IllegalArgumentException("reportDefinitionUuid parameter is required");
    }

    ReportDefinition reportDefinition = getReportDefinitionService().getDefinitionByUuid(reportDefinitionUuid);
    if (reportDefinition == null) {
      throw new EntityNotFoundException("ReportDefinition not found with uuid: " + reportDefinitionUuid);
    }

    List<ReportDesign> reportDesigns = getReportService().getReportDesigns(reportDefinition, null, false);
    return new NeedsPaging<ReportDesign>(reportDesigns, context);
  }

  @Override
  protected void delete(ReportDesign reportDesign, String reason, RequestContext requestContext) throws ResponseException {
    purge(reportDesign, requestContext);
  }

  @Override
  public ReportDesign save(ReportDesign reportDesign) {
    return getReportService().saveReportDesign(reportDesign);
  }

  @Override
  public void purge(ReportDesign reportDesign, RequestContext requestContext) throws ResponseException {
      getReportService().purgeReportDesign(reportDesign);
  }

  @Override
  public DelegatingResourceDescription getCreatableProperties() {
    return new DelegatingResourceDescription();
  }

  @Override
  public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
    if (representation instanceof CustomRepresentation) {
      return null;
    }

    DelegatingResourceDescription description = new DelegatingResourceDescription();
    description.addProperty("uuid");
    description.addProperty("name");
    description.addProperty("rendererType");
    description.addSelfLink();

    return description;
  }

  private ReportService getReportService() {
    return Context.getService(ReportService.class);
  }

  private ReportDefinitionService getReportDefinitionService() {
    return Context.getService(ReportDefinitionService.class);
  }
}
