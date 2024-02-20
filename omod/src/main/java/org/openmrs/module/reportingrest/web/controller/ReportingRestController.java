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
package org.openmrs.module.reportingrest.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;
import org.openmrs.module.reportingrest.web.wrapper.RunReportRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.openmrs.ui.framework.page.FileDownload;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for {@link CohortDefinition}s
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + ReportingRestController.REPORTING_REST_NAMESPACE)
public class ReportingRestController extends MainResourceController {

    public static final String REPORTING_REST_NAMESPACE = "/reportingrest";

    private static final Log LOGGER = LogFactory.getLog(ReportingRestController.class);

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return RestConstants.VERSION_1 + REPORTING_REST_NAMESPACE;
    }

    @RequestMapping(value = "/runReport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void runReport(@RequestBody RunReportRequest runReportRequest) {
        ReportDefinition reportDefinition = Context.getService(ReportDefinitionService.class)
            .getDefinitionByUuid(runReportRequest.getReportDefinitionUuid());

        ReportService reportService = getReportService();

        Map<String, Object> parameterValues = new HashMap<String, Object>();
        for (Parameter parameter : reportDefinition.getParameters()) {
            Object convertedObj =
                convertParamValueToObject(runReportRequest.getReportParameters().get(parameter.getName()), parameter.getType());
            parameterValues.put(parameter.getName(), convertedObj);
        }

        List<RenderingMode> renderingModes = reportService.getRenderingModes(reportDefinition);
        RenderingMode renderingMode = null;
        for (RenderingMode mode : renderingModes) {
            if (StringUtils.equals(mode.getArgument(), runReportRequest.getRenderModeUuid())) {
                renderingMode = mode;
                break;
            }
        }

        final ReportRequest reportRequest;
        if(StringUtils.isNotBlank(runReportRequest.getExistingRequestUuid())) {
            reportRequest = reportService.getReportRequestByUuid(runReportRequest.getExistingRequestUuid());
        } else {
            reportRequest = new ReportRequest();
        }
        reportRequest.setReportDefinition(new Mapped<ReportDefinition>(reportDefinition, parameterValues));
        reportRequest.setRenderingMode(renderingMode);
        reportRequest.setPriority(ReportRequest.Priority.NORMAL);
        reportRequest.setSchedule(runReportRequest.getSchedule());

        reportService.queueReport(reportRequest);
        reportService.processNextQueuedReports();
    }

    @RequestMapping(value = "/cancelReport", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public void cancelReport(@RequestParam String reportRequestUuid) {
        ReportService reportService = getReportService();
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);
        if (reportRequest != null) {
            reportService.purgeReportRequest(reportRequest);
        }
    }

    @RequestMapping(value = "/preserveReport", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void preserveReport(@RequestParam String reportRequestUuid) {
        ReportService reportService = getReportService();
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);
        if (ReportRequest.Status.COMPLETED.equals(reportRequest.getStatus())) {
            Report report = reportService.loadReport(reportRequest);
            reportService.saveReport(report, StringUtils.EMPTY);
        }
    }

    @RequestMapping(value = "/downloadReport", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public FileDownload downloadReport(@RequestParam String reportRequestUuid) {
        return processAndDownloadReport(reportRequestUuid, getReportService());
    }

    @RequestMapping(value = "/downloadMultipleReports", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<FileDownload> downloadMultipleReports(@RequestParam String reportRequestUuids) {
        List<FileDownload> fileDownloadList = new ArrayList<FileDownload>();
        ReportService reportService = getReportService();
        for (String reportRequestUuid : reportRequestUuids.split(",")) {
            fileDownloadList.add(processAndDownloadReport(reportRequestUuid, reportService));
        }

        return fileDownloadList;
    }

    private FileDownload processAndDownloadReport(String reportRequestUuid, ReportService reportService) {
        ReportRequest reportRequest = reportService.getReportRequestByUuid(reportRequestUuid);
        if (reportRequest == null) {
            throw new IllegalArgumentException("Report request not found");
        }

        RenderingMode renderingMode = reportRequest.getRenderingMode();
        if (renderingMode.getRenderer() instanceof WebReportRenderer) {
            throw new IllegalStateException("Web Renderers not implemented yet");
        }

        String fileName = renderingMode.getRenderer().getFilename(reportRequest).replace(" ", "_");
        String contentType = renderingMode.getRenderer().getRenderedContentType(reportRequest);
        byte[] fileContent = reportService.loadRenderedOutput(reportRequest);

        if (fileContent == null) {
            throw new IllegalStateException("Error during loading rendered output");
        } else {
            return new FileDownload(fileName, contentType, fileContent);
        }
    }

    private Object convertParamValueToObject(Object value, Class<?> type) {
        Object convertedObject = value;

        if (type.equals(Date.class)) {
            try {
                convertedObject = DateUtils.parseDate((String) value, "MM/dd/yyyy");
            } catch (ParseException e) {
                LOGGER.error("Error while parsing date");
            }
        }

        if (type.equals(Integer.class)) {
            convertedObject = Integer.valueOf((String) value);
        }

        if (type.equals(Location.class)) {
            convertedObject = Context.getLocationService().getLocationByUuid((String) value);
        }

        return convertedObject;
    }

    private ReportService getReportService() {
        return Context.getService(ReportService.class);
    }
}
