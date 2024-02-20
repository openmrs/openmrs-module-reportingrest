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
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

/**
 * {@link Resource} for {@link ReportDesign}s, supporting standard CRUD operations
 */
@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/designs",
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
    if (reportDesign != null) {
      getReportService().purgeReportDesign(reportDesign);
    }
  }

  @Override
  public DelegatingResourceDescription getCreatableProperties() {
    return new DelegatingResourceDescription();
  }

  @Override
  public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
    DelegatingResourceDescription description = null;

    if (representation instanceof RefRepresentation) {
      description = new DelegatingResourceDescription();
      description.addProperty("uuid");
      description.addProperty("name");
      description.addProperty("rendererType");
      description.addSelfLink();
    }

    return description;
  }

  private ReportService getReportService() {
    return Context.getService(ReportService.class);
  }

  private ReportDefinitionService getReportDefinitionService() {
    return Context.getService(ReportDefinitionService.class);
  }
}
