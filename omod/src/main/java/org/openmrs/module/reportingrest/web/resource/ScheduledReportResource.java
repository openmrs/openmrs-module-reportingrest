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
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.controller.ReportingRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE + "/scheduledReport",
    supportedClass = SimpleObject.class, supportedOpenmrsVersions = {"1.8.* - 9.9.*"})
public class ScheduledReportResource extends DelegatingCrudResource<SimpleObject> {

  @Override
  public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
    final DelegatingResourceDescription description = new DelegatingResourceDescription();
    description.addProperty("reportDefinition", Representation.FULL);
    description.addProperty("reportScheduleRequest", Representation.FULL);
    return description;
  }

  @Override
  public SimpleObject newDelegate() {
    return new SimpleObject();
  }

  @Override
  public SimpleObject save(SimpleObject scheduledReport) {
    throw new ResourceDoesNotSupportOperationException();
  }

  @Override
  public void purge(SimpleObject scheduledReport, RequestContext requestContext) throws ResponseException {
    throw new ResourceDoesNotSupportOperationException();
  }

  @Override
  protected void delete(SimpleObject scheduledReport, String s, RequestContext requestContext) throws ResponseException {
    throw new ResourceDoesNotSupportOperationException();
  }

  @Override
  public SimpleObject getByUniqueId(String s) {
    throw new ResourceDoesNotSupportOperationException();
  }

  @Override
  protected PageableResult doGetAll(RequestContext context) throws ResponseException {
    final List<ReportDefinition> reportDefinitions = getSortedReportDefinitions(context);
    List<SimpleObject> scheduledReports = mergeScheduledReports(reportDefinitions);

    return new AlreadyPaged<SimpleObject>(context, scheduledReports, false);
  }

  @Override
  protected PageableResult doSearch(RequestContext context) {
    return doGetAll(context);
  }

  private List<ReportDefinition> getSortedReportDefinitions(RequestContext context) {
    final List<ReportDefinition> reportDefinitions =
        Context.getService(ReportDefinitionService.class).getAllDefinitions(false);

    Collections.sort(reportDefinitions, getComparatorForRequestSorting(context));
    return reportDefinitions;
  }

  private List<SimpleObject> mergeScheduledReports(List<ReportDefinition> reportDefinitions) {
    List<SimpleObject> scheduledReports = new ArrayList<SimpleObject>();

    for (ReportDefinition reportDefinition : reportDefinitions) {
      List<ReportRequest> reportRequests = Context
          .getService(ReportService.class)
          .getReportRequests(reportDefinition, null, null, ReportRequest.Status.SCHEDULED,
              ReportRequest.Status.SCHEDULE_COMPLETED);

      if (reportRequests.isEmpty()) {
        scheduledReports.add(new SimpleObject()
            .add("reportDefinition", reportDefinition)
            .add("reportScheduleRequest", null));
      } else {
        for (ReportRequest reportRequest : reportRequests) {
          scheduledReports.add(new SimpleObject()
              .add("reportDefinition", reportDefinition)
              .add("reportScheduleRequest", reportRequest));
        }
      }
    }

    return scheduledReports;
  }

  private Comparator<ReportDefinition> getComparatorForRequestSorting(RequestContext context) {
    final String sortBy = context.getParameter("sortBy");

    if (StringUtils.isBlank(sortBy) || "id".equals(sortBy)) {
      return new ById();
    } else if ("name".equals(sortBy)) {
      return new ByName();
    } else {
      throw new UnsupportedOperationException("Unsupported sorBy value: " + sortBy);
    }
  }

  private static class ById implements Comparator<ReportDefinition> {

    @Override
    public int compare(ReportDefinition reportDefinition1, ReportDefinition reportDefinition2) {
      return reportDefinition1.getId().compareTo(reportDefinition2.getId());
    }
  }

  private static class ByName implements Comparator<ReportDefinition> {

    @Override
    public int compare(ReportDefinition reportDefinition1, ReportDefinition reportDefinition2) {
      return reportDefinition1.getName().compareTo(reportDefinition2.getName());
    }
  }
}
