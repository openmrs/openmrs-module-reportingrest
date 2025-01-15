/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.reportingrest.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingrest.web.ReportFile;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for {@link CohortDefinition}s
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE)
public class ReportingRestController extends MainResourceController {

    public static final String REPORTING_REST_NAMESPACE = "/reportingrest";

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return RestConstants.VERSION_1 + REPORTING_REST_NAMESPACE;
    }

    @RequestMapping(value = "/saveReport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> saveReport(@RequestParam String reportRequestUuid) {
        ReportService reportService = getReportService();
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);

        if (!ReportRequest.Status.COMPLETED.equals(reportRequest.getStatus())) {
            return new ResponseEntity<String>("Cannot save report because status is different than completed",
                HttpStatus.BAD_REQUEST);
        }

        Report report = reportService.loadReport(reportRequest);
        reportService.saveReport(report, StringUtils.EMPTY);

        return new ResponseEntity<String>("Report saved successfully", HttpStatus.OK);
    }

    @RequestMapping(value = "/downloadReport", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ReportFile downloadReport(@RequestParam String reportRequestUuid) {
        return processAndDownloadReport(reportRequestUuid, getReportService());
    }

    @RequestMapping(value = "/downloadMultipleReports", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<ReportFile> downloadMultipleReports(@RequestParam String reportRequestUuids) {
        List<ReportFile> fileDownloadList = new ArrayList<ReportFile>();
        ReportService reportService = getReportService();
        for (String reportRequestUuid : reportRequestUuids.split(",")) {
            fileDownloadList.add(processAndDownloadReport(reportRequestUuid, reportService));
        }

        return fileDownloadList;
    }

    private ReportFile processAndDownloadReport(String reportRequestUuid, ReportService reportService) {
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);
        if (reportRequest == null) {
            throw new IllegalArgumentException("Report request not found for UUID: " + reportRequestUuid);
        }

        RenderingMode renderingMode = reportRequest.getRenderingMode();
        String fileName = renderingMode.getRenderer().getFilename(reportRequest).replace(" ", "_");
        String contentType = renderingMode.getRenderer().getRenderedContentType(reportRequest);
        byte[] fileContent = reportService.loadRenderedOutput(reportRequest);

        if (fileContent == null) {
            throw new IllegalStateException("Error during loading rendered output");
        } else {
            return new ReportFile(fileName, contentType, fileContent);
        }
    }

    private ReportService getReportService() {
        return Context.getService(ReportService.class);
    }
}
